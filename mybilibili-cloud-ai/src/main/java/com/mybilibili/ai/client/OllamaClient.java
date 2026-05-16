package com.mybilibili.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybilibili.ai.config.AiProperties;
import com.mybilibili.ai.constants.AiConstants;
import com.mybilibili.base.exception.BusinessException;
import jakarta.annotation.Resource;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Ollama 向量模型客户端。
 *
 * <p>当前只保留本地 bge 向量能力。对话模型已经迁移到 OpenAI 兼容接口，避免把本地聊天模型
 * 的调用继续揉在业务链路里。</p>
 */
@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AiProperties aiProperties;

    private volatile OkHttpClient okHttpClient;

    public List<Double> embed(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getOllama().getEmbeddingModel());
        body.put("input", text);
        body.put("keep_alive", aiProperties.getOllama().getKeepAlive());

        try {
            return parseEmbeddingResponse(postJson(AiConstants.OLLAMA_EMBED_API_PATH, body));
        } catch (OllamaHttpException e) {
            if (e.getStatusCode() != 404) {
                throw buildEmbeddingException(e);
            }
            return embedWithLegacyApi(text);
        } catch (Exception e) {
            throw buildEmbeddingException(e);
        }
    }

    private List<Double> embedWithLegacyApi(String text) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getOllama().getEmbeddingModel());
        body.put("prompt", text);
        body.put("keep_alive", aiProperties.getOllama().getKeepAlive());
        try {
            return parseEmbeddingResponse(postJson(AiConstants.OLLAMA_LEGACY_EMBED_API_PATH, body));
        } catch (Exception e) {
            throw buildEmbeddingException(e);
        }
    }

    private BusinessException buildEmbeddingException(Exception e) {
        log.error("Ollama 向量请求失败, model={}", aiProperties.getOllama().getEmbeddingModel(), e);
        return new BusinessException("AI 向量模型暂时不可用，请稍后再试", e);
    }

    private List<Double> parseEmbeddingResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode vectorNode = root.path("embeddings");
        if (vectorNode.isArray() && !vectorNode.isEmpty()) {
            vectorNode = vectorNode.get(0);
        } else {
            vectorNode = root.path("embedding");
        }
        if (!vectorNode.isArray() || vectorNode.isEmpty()) {
            throw new BusinessException("AI 向量模型没有返回结果");
        }
        List<Double> vector = new ArrayList<>(vectorNode.size());
        for (JsonNode valueNode : vectorNode) {
            vector.add(valueNode.asDouble());
        }
        return vector;
    }

    private String postJson(String path, Map<String, Object> body) throws Exception {
        Request request = buildJsonRequest(path, body);
        try (Response response = getClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw readHttpException(response);
            }
            ResponseBody responseBody = response.body();
            return responseBody == null ? "" : responseBody.string();
        }
    }

    private Request buildJsonRequest(String path, Map<String, Object> body) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new BusinessException("AI 请求序列化失败", e);
        }
        return new Request.Builder()
                .url(buildUrl(path))
                .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                .build();
    }

    private String buildUrl(String path) {
        String baseUrl = aiProperties.getOllama().getBaseUrl();
        return StringUtils.removeEnd(baseUrl, "/") + path;
    }

    private OllamaHttpException readHttpException(Response response) throws Exception {
        ResponseBody responseBody = response.body();
        String errorBody = responseBody == null ? "" : responseBody.string();
        return new OllamaHttpException(response.code(), errorBody);
    }

    private OkHttpClient getClient() {
        OkHttpClient client = okHttpClient;
        if (client != null) {
            return client;
        }
        synchronized (this) {
            if (okHttpClient == null) {
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(aiProperties.getOllama().getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                        .readTimeout(aiProperties.getOllama().getReadTimeoutSeconds(), TimeUnit.SECONDS)
                        .writeTimeout(aiProperties.getOllama().getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                        .build();
            }
            return okHttpClient;
        }
    }

    private static class OllamaHttpException extends RuntimeException {

        private final int statusCode;

        private OllamaHttpException(int statusCode, String body) {
            super("HTTP " + statusCode + " - " + body);
            this.statusCode = statusCode;
        }

        private int getStatusCode() {
            return statusCode;
        }
    }
}
