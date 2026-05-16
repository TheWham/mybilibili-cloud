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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * OpenAI 兼容协议对话客户端。
 *
 * <p>只实现项目当前用到的 chat completions 能力，避免为了一个接口升级整个 Spring Boot
 * 版本体系。API Key 通过 Nacos 或环境变量注入，代码里不放任何真实密钥。</p>
 */
@Component
public class OpenAiCompatibleChatClient implements AiChatModelClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleChatClient.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String STREAM_DATA_PREFIX = "data:";
    private static final String STREAM_DONE = "[DONE]";

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AiProperties aiProperties;

    private volatile OkHttpClient okHttpClient;

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        Map<String, Object> body = buildChatRequest(systemPrompt, userPrompt, false);
        try {
            JsonNode root = objectMapper.readTree(postJsonWithRetry(body));
            String answer = root.path("choices").path(0).path("message").path("content").asText("");
            if (StringUtils.isBlank(answer)) {
                throw new BusinessException("AI 模型没有返回回答");
            }
            return answer.trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI 兼容对话请求失败, model={}", aiProperties.getChatProvider().getModel(), e);
            throw new BusinessException("AI 问答模型暂时不可用，请稍后再试", e);
        }
    }

    @Override
    public String streamChat(String systemPrompt, String userPrompt, Consumer<String> deltaConsumer) {
        Map<String, Object> body = buildChatRequest(systemPrompt, userPrompt, true);
        StringBuilder answer = new StringBuilder();
        try (Response response = openStreamResponseWithRetry(body)) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new BusinessException("AI 模型没有返回回答");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (handleStreamLine(line, answer, deltaConsumer)) {
                        break;
                    }
                }
            }
            if (answer.length() == 0) {
                throw new BusinessException("AI 模型没有返回回答");
            }
            return answer.toString().trim();
        } catch (BusinessException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new BusinessException("AI 问答请求超时，请稍后再试", e);
        } catch (IOException e) {
            throw new BusinessException("AI 问答服务网络异常，请稍后再试", e);
        } catch (Exception e) {
            log.error("OpenAI 兼容流式对话请求失败, model={}", aiProperties.getChatProvider().getModel(), e);
            throw new BusinessException("AI 问答模型暂时不可用，请稍后再试", e);
        }
    }

    private Map<String, Object> buildChatRequest(String systemPrompt, String userPrompt, boolean stream) {
        List<Map<String, String>> messages = new ArrayList<>(2);
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getChatProvider().getModel());
        body.put("messages", messages);
        body.put("stream", stream);
        return body;
    }

    private String postJsonWithRetry(Map<String, Object> body) {
        ChatProviderHttpException lastHttpException = null;
        int maxAttempts = getMaxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Response response = getClient().newCall(buildJsonRequest(body)).execute()) {
                if (!response.isSuccessful()) {
                    throw readHttpException(response);
                }
                ResponseBody responseBody = response.body();
                return responseBody == null ? "" : responseBody.string();
            } catch (ChatProviderHttpException e) {
                lastHttpException = e;
                if (!canRetry(e, attempt, maxAttempts)) {
                    throw toBusinessException(e);
                }
                log.warn("OpenAI 兼容对话请求准备重试, model={}, status={}, attempt={}",
                        aiProperties.getChatProvider().getModel(), e.getStatusCode(), attempt);
                sleepQuietly(aiProperties.getChatProvider().getRetryIntervalMs());
            } catch (SocketTimeoutException e) {
                throw new BusinessException("AI 问答请求超时，请稍后再试", e);
            } catch (IOException e) {
                throw new BusinessException("AI 问答服务网络异常，请稍后再试", e);
            }
        }
        throw toBusinessException(lastHttpException);
    }

    private Response openStreamResponseWithRetry(Map<String, Object> body) throws IOException {
        ChatProviderHttpException lastHttpException = null;
        int maxAttempts = getMaxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Response response = getClient().newCall(buildJsonRequest(body)).execute();
            if (response.isSuccessful()) {
                return response;
            }
            try {
                throw readHttpException(response);
            } catch (ChatProviderHttpException e) {
                lastHttpException = e;
                if (!canRetry(e, attempt, maxAttempts)) {
                    throw toBusinessException(e);
                }
                log.warn("OpenAI 兼容流式对话请求准备重试, model={}, status={}, attempt={}",
                        aiProperties.getChatProvider().getModel(), e.getStatusCode(), attempt);
                sleepQuietly(aiProperties.getChatProvider().getRetryIntervalMs());
            } finally {
                response.close();
            }
        }
        throw toBusinessException(lastHttpException);
    }

    private boolean handleStreamLine(String line, StringBuilder answer, Consumer<String> deltaConsumer) throws Exception {
        String data = StringUtils.trimToEmpty(line);
        if (StringUtils.isBlank(data) || !StringUtils.startsWith(data, STREAM_DATA_PREFIX)) {
            return false;
        }
        data = StringUtils.trimToEmpty(StringUtils.removeStart(data, STREAM_DATA_PREFIX));
        if (STREAM_DONE.equals(data)) {
            return true;
        }

        JsonNode root = objectMapper.readTree(data);
        if (root.hasNonNull("error")) {
            throw new BusinessException("AI 问答服务暂时不可用，请稍后再试");
        }
        JsonNode choice = root.path("choices").path(0);
        String delta = choice.path("delta").path("content").asText("");
        if (StringUtils.isNotEmpty(delta)) {
            answer.append(delta);
            if (deltaConsumer != null) {
                deltaConsumer.accept(delta);
            }
        }
        return StringUtils.isNotBlank(choice.path("finish_reason").asText(""));
    }

    private Request buildJsonRequest(Map<String, Object> body) {
        String apiKey = StringUtils.trimToEmpty(aiProperties.getChatProvider().getApiKey());
        if (StringUtils.isBlank(apiKey)) {
            throw new BusinessException("AI 问答 API Key 未配置");
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new BusinessException("AI 请求序列化失败", e);
        }
        return new Request.Builder()
                .url(buildUrl())
                .addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + apiKey)
                .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                .build();
    }

    private String buildUrl() {
        AiProperties.ChatProvider chatProvider = aiProperties.getChatProvider();
        String baseUrl = StringUtils.removeEnd(StringUtils.trimToEmpty(chatProvider.getBaseUrl()), "/");
        String path = StringUtils.prependIfMissing(StringUtils.trimToEmpty(chatProvider.getChatCompletionsPath()), "/");
        return baseUrl + path;
    }

    private ChatProviderHttpException readHttpException(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            responseBody.string();
        }
        // 供应商错误体可能带请求详情或内部链路信息，这里只保留状态码，日志和异常都不透传原文。
        return new ChatProviderHttpException(response.code());
    }

    private boolean canRetry(ChatProviderHttpException e, int attempt, int maxAttempts) {
        return attempt < maxAttempts && (e.getStatusCode() == 429 || e.getStatusCode() >= 500);
    }

    private BusinessException toBusinessException(ChatProviderHttpException e) {
        if (e == null) {
            return new BusinessException("AI 问答模型暂时不可用，请稍后再试");
        }
        int statusCode = e.getStatusCode();
        if (statusCode == 401 || statusCode == 403) {
            return new BusinessException("AI 问答接口鉴权失败，请检查 API Key 配置", e);
        }
        if (statusCode == 429) {
            return new BusinessException("AI 问答接口调用过于频繁，请稍后再试", e);
        }
        if (statusCode >= 500) {
            return new BusinessException("AI 问答服务暂时不可用，请稍后再试", e);
        }
        return new BusinessException("AI 问答请求失败，请稍后再试", e);
    }

    private int getMaxAttempts() {
        Integer maxRetries = aiProperties.getChatProvider().getMaxRetries();
        return Math.max(1, maxRetries == null ? 1 : maxRetries + 1);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private OkHttpClient getClient() {
        OkHttpClient client = okHttpClient;
        if (client != null) {
            return client;
        }
        synchronized (this) {
            if (okHttpClient == null) {
                AiProperties.ChatProvider chatProvider = aiProperties.getChatProvider();
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(chatProvider.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                        .readTimeout(chatProvider.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                        .writeTimeout(chatProvider.getWriteTimeoutSeconds(), TimeUnit.SECONDS)
                        .build();
            }
            return okHttpClient;
        }
    }

    private static class ChatProviderHttpException extends RuntimeException {

        private final int statusCode;

        private ChatProviderHttpException(int statusCode) {
            super("HTTP " + statusCode);
            this.statusCode = statusCode;
        }

        private int getStatusCode() {
            return statusCode;
        }
    }
}
