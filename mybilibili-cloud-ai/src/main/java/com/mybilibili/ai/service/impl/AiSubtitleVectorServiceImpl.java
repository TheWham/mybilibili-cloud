package com.mybilibili.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybilibili.ai.config.AiProperties;
import com.mybilibili.ai.constants.AiConstants;
import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;
import com.mybilibili.ai.service.AiSubtitleVectorService;
import com.mybilibili.base.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("aiSubtitleVectorService")
public class AiSubtitleVectorServiceImpl implements AiSubtitleVectorService {

    private static final String HTTP_METHOD_POST = "POST";
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final List<String> SOURCE_FIELDS = List.of(
            "videoId", "videoName", "videoCover", "content", "startTime", "endTime"
    );

    @Resource
    private RestClient restClient;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AiProperties aiProperties;

    @Override
    public List<AiMatchedVideoVO> search(List<Double> queryVector, Integer topK, Double minScore) {
        return search(null, queryVector, topK, minScore);
    }

    @Override
    public List<AiMatchedVideoVO> search(String keyword, List<Double> queryVector, Integer topK, Double minScore) {
        int limit = topK == null || topK <= 0 ? aiProperties.getRag().getDefaultTopK() : topK;
        List<AiMatchedVideoVO> vectorMatches = searchByVector(queryVector, limit, minScore);
        if (!StringUtils.hasText(keyword)) {
            return vectorMatches;
        }

        /*
         * 短关键词（例如 ai、java）用向量检索很容易命中一些语义很散的字幕。
         * 关键词精确命中的字幕更适合直接进入 RAG，标题命中只作为兜底展示。
         */
        List<AiMatchedVideoVO> subtitleKeywordMatches = searchBySubtitleKeyword(keyword, limit);
        List<AiMatchedVideoVO> titleMatches = searchByTitleKeyword(keyword, limit);
        if (subtitleKeywordMatches.isEmpty()) {
            return mergeMatches(titleMatches, vectorMatches, limit);
        }

        List<AiMatchedVideoVO> mergedMatches = mergeMatches(subtitleKeywordMatches, vectorMatches, limit);
        if (mergedMatches.size() >= limit) {
            return mergedMatches;
        }

        return mergeMatches(mergedMatches, titleMatches, limit);
    }

    private List<AiMatchedVideoVO> searchByVector(List<Double> queryVector, int limit, Double minScore) {
        if (queryVector == null || queryVector.isEmpty()) {
            return new ArrayList<>();
        }
        int candidateSize = resolveCandidateSize(limit);

        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_SEARCH_PATH));
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(buildSearchBody(queryVector, candidateSize)), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return parseSearchResponse(responseJson, limit, minScore == null ? aiProperties.getRag().getMinScore() : minScore);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                log.warn("字幕向量索引不存在, index={}", aiProperties.getEs().getSubtitleVectorIndexName());
                return new ArrayList<>();
            }
            log.error("字幕向量检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        } catch (Exception e) {
            log.error("字幕向量检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        }
    }

    private List<AiMatchedVideoVO> searchBySubtitleKeyword(String keyword, int limit) {
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_SEARCH_PATH));
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(buildSubtitleKeywordSearchBody(keyword, resolveCandidateSize(limit))), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return parseKeywordSearchResponse(responseJson, limit);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                log.warn("字幕向量索引不存在, index={}", aiProperties.getEs().getSubtitleVectorIndexName());
                return new ArrayList<>();
            }
            log.error("字幕关键词检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        } catch (Exception e) {
            log.error("字幕关键词检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        }
    }

    private List<AiMatchedVideoVO> searchByTitleKeyword(String keyword, int limit) {
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_SEARCH_PATH));
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(buildTitleKeywordSearchBody(keyword, resolveCandidateSize(limit))), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return parseTitleSearchResponse(responseJson, limit);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                log.warn("字幕向量索引不存在, index={}", aiProperties.getEs().getSubtitleVectorIndexName());
                return new ArrayList<>();
            }
            log.error("视频标题关键词检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        } catch (Exception e) {
            log.error("视频标题关键词检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        }
    }

    @Override
    public void deleteByVideoId(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return;
        }
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_DELETE_BY_QUERY_PATH));
            request.addParameter(AiConstants.ES_CONFLICTS_PARAM, AiConstants.ES_CONFLICTS_PROCEED);
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(buildDeleteBody(videoId)), ContentType.APPLICATION_JSON));
            restClient.performRequest(request);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                return;
            }
            throw new BusinessException("删除视频字幕向量失败", e);
        } catch (Exception e) {
            throw new BusinessException("删除视频字幕向量失败", e);
        }
    }

    private Map<String, Object> buildSearchBody(List<Double> queryVector, int candidateSize) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("queryVector", queryVector);

        Map<String, Object> script = new LinkedHashMap<>();
        // ES 的 script_score 不接受负分，所以把 cosine 分数整体 +1，解析结果时再减回来。
        script.put("source", "cosineSimilarity(params.queryVector, '" + AiConstants.ES_CONTENT_VECTOR_FIELD
                + "') + " + AiConstants.ES_COSINE_SCORE_OFFSET);
        script.put("params", params);

        Map<String, Object> scriptScore = new LinkedHashMap<>();
        scriptScore.put("query", Map.of("match_all", Map.of()));
        scriptScore.put("script", script);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", candidateSize);
        body.put("_source", SOURCE_FIELDS);
        body.put("query", Map.of("script_score", scriptScore));
        return body;
    }

    private Map<String, Object> buildSubtitleKeywordSearchBody(String keyword, int candidateSize) {
        List<Map<String, Object>> shouldQueries = new ArrayList<>();
        shouldQueries.add(Map.of(
                "match_phrase", Map.of(
                        "content", Map.of("query", keyword, "boost", aiProperties.getSearch().getSubtitlePhraseBoost())
                )
        ));
        shouldQueries.add(Map.of(
                "match", Map.of(
                        "content", Map.of("query", keyword, "operator", "and", "boost", aiProperties.getSearch().getSubtitleAndBoost())
                )
        ));
        shouldQueries.add(Map.of(
                "match", Map.of(
                        "content", Map.of("query", keyword, "operator", "or")
                )
        ));

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("should", shouldQueries);
        bool.put("minimum_should_match", 1);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", candidateSize);
        body.put("_source", SOURCE_FIELDS);
        // 关键词兜底只查字幕正文。标题命中不能直接当成“命中字幕”，否则会出现标题相关、
        // 但展示的字幕片段完全无关的问题；标题检索应交给普通搜索或单独的视频召回链路。
        body.put("query", Map.of("bool", bool));
        return body;
    }

    private Map<String, Object> buildTitleKeywordSearchBody(String keyword, int candidateSize) {
        List<Map<String, Object>> shouldQueries = new ArrayList<>();
        shouldQueries.add(Map.of(
                "match_phrase", Map.of(
                        "videoName", Map.of("query", keyword, "boost", aiProperties.getSearch().getTitlePhraseBoost())
                )
        ));
        shouldQueries.add(Map.of(
                "match", Map.of(
                        "videoName", Map.of("query", keyword, "operator", "and", "boost", aiProperties.getSearch().getTitleAndBoost())
                )
        ));

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("should", shouldQueries);
        bool.put("minimum_should_match", 1);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", candidateSize);
        body.put("_source", SOURCE_FIELDS);
        // 标题召回只用于展示相关视频，不能作为字幕证据喂给大模型回答。
        body.put("query", Map.of("bool", bool));
        return body;
    }

    private Map<String, Object> buildDeleteBody(String videoId) {
        return Map.of(
                "query", Map.of(
                        "term", Map.of(
                                "videoId", Map.of("value", videoId)
                        )
                )
        );
    }

    private List<AiMatchedVideoVO> parseSearchResponse(String responseJson, int limit, double minScore) throws Exception {
        JsonNode hits = objectMapper.readTree(responseJson).path("hits").path("hits");
        Map<String, AiMatchedVideoVO> videoMap = new LinkedHashMap<>();
        for (JsonNode hit : hits) {
            double cosineScore = hit.path("_score").asDouble(0D) - AiConstants.ES_COSINE_SCORE_OFFSET;
            if (cosineScore < minScore) {
                continue;
            }
            JsonNode source = hit.path("_source");
            String videoId = source.path("videoId").asText("");
            if (videoId.isEmpty() || videoMap.containsKey(videoId)) {
                continue;
            }

            AiMatchedVideoVO matchedVideo = new AiMatchedVideoVO();
            matchedVideo.setVideoId(videoId);
            matchedVideo.setVideoName(source.path("videoName").asText(""));
            matchedVideo.setVideoCover(source.path("videoCover").asText(""));
            matchedVideo.setMatchedText(source.path("content").asText(""));
            matchedVideo.setStartTime(source.path("startTime").asDouble(0D));
            matchedVideo.setEndTime(source.path("endTime").asDouble(0D));
            matchedVideo.setScore(roundScore(cosineScore));
            matchedVideo.setMatchType(AiConstants.MATCH_TYPE_SUBTITLE);
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(videoMap.values()));
    }

    private List<AiMatchedVideoVO> parseKeywordSearchResponse(String responseJson, int limit) throws Exception {
        JsonNode hits = objectMapper.readTree(responseJson).path("hits").path("hits");
        Map<String, AiMatchedVideoVO> videoMap = new LinkedHashMap<>();
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            String videoId = source.path("videoId").asText("");
            if (videoId.isEmpty() || videoMap.containsKey(videoId)) {
                continue;
            }

            AiMatchedVideoVO matchedVideo = buildMatchedVideo(source);
            matchedVideo.setScore(normalizeKeywordScore(hit.path("_score").asDouble(0D)));
            matchedVideo.setMatchType(AiConstants.MATCH_TYPE_SUBTITLE);
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(videoMap.values()));
    }

    private List<AiMatchedVideoVO> parseTitleSearchResponse(String responseJson, int limit) throws Exception {
        JsonNode hits = objectMapper.readTree(responseJson).path("hits").path("hits");
        Map<String, AiMatchedVideoVO> videoMap = new LinkedHashMap<>();
        double maxScore = 0D;
        for (JsonNode hit : hits) {
            maxScore = Math.max(maxScore, hit.path("_score").asDouble(0D));
        }

        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            String videoId = source.path("videoId").asText("");
            if (videoId.isEmpty() || videoMap.containsKey(videoId)) {
                continue;
            }

            AiMatchedVideoVO matchedVideo = buildMatchedVideo(source);
            matchedVideo.setMatchedText("标题匹配：" + matchedVideo.getVideoName());
            matchedVideo.setStartTime(null);
            matchedVideo.setEndTime(null);
            matchedVideo.setScore(normalizeTitleScore(hit.path("_score").asDouble(0D), maxScore));
            matchedVideo.setMatchType(AiConstants.MATCH_TYPE_TITLE);
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(videoMap.values()));
    }

    private List<AiMatchedVideoVO> mergeMatches(List<AiMatchedVideoVO> vectorMatches, List<AiMatchedVideoVO> keywordMatches, int limit) {
        Map<String, AiMatchedVideoVO> merged = new LinkedHashMap<>();
        for (AiMatchedVideoVO matchedVideo : vectorMatches) {
            merged.put(matchedVideo.getVideoId(), matchedVideo);
        }
        for (AiMatchedVideoVO matchedVideo : keywordMatches) {
            merged.putIfAbsent(matchedVideo.getVideoId(), matchedVideo);
            if (merged.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(merged.values());
    }

    private AiMatchedVideoVO buildMatchedVideo(JsonNode source) {
        AiMatchedVideoVO matchedVideo = new AiMatchedVideoVO();
        matchedVideo.setVideoId(source.path("videoId").asText(""));
        matchedVideo.setVideoName(source.path("videoName").asText(""));
        matchedVideo.setVideoCover(source.path("videoCover").asText(""));
        matchedVideo.setMatchedText(source.path("content").asText(""));
        matchedVideo.setStartTime(source.path("startTime").asDouble(0D));
        matchedVideo.setEndTime(source.path("endTime").asDouble(0D));
        return matchedVideo;
    }

    private double normalizeKeywordScore(double score) {
        if (score <= 0) {
            return 0D;
        }
        return roundScore(Math.min(score / aiProperties.getSearch().getKeywordScoreDivisor(), 1D));
    }

    private double normalizeTitleScore(double score, double maxScore) {
        if (score <= 0 || maxScore <= 0) {
            return 0D;
        }
        // 标题命中只是“相关视频”证据，分数不和字幕向量分数直接竞争，避免看起来像字幕高度匹配。
        double titleScoreMax = aiProperties.getSearch().getTitleScoreMax();
        return roundScore(Math.min(titleScoreMax, Math.max(aiProperties.getSearch().getTitleScoreMin(), score / maxScore * titleScoreMax)));
    }

    private double roundScore(double score) {
        return Math.round(score * AiConstants.SCORE_SCALE) / AiConstants.SCORE_SCALE;
    }

    private int resolveCandidateSize(int limit) {
        return Math.max(limit * aiProperties.getSearch().getCandidateMultiplier(), limit);
    }

    private String buildEsPath(String actionPath) {
        return "/" + aiProperties.getEs().getSubtitleVectorIndexName() + "/" + actionPath;
    }

    private boolean isIndexNotFound(ResponseException e) {
        return e.getResponse() != null && e.getResponse().getStatusLine().getStatusCode() == HTTP_STATUS_NOT_FOUND;
    }
}

