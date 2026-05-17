package com.mybilibili.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybilibili.ai.config.AiProperties;
import com.mybilibili.ai.constants.AiConstants;
import com.mybilibili.ai.entity.vo.AiMatchDetailVO;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service("aiSubtitleVectorService")
public class AiSubtitleVectorServiceImpl implements AiSubtitleVectorService {

    private static final String HTTP_METHOD_POST = "POST";
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final List<String> SOURCE_FIELDS = List.of(
            "videoId", "videoName", "videoCover", "tags", "content", "startTime", "endTime"
    );
    private static final int METADATA_KEYWORD_MAX_COUNT = 8;
    private static final int METADATA_KEYWORD_MAX_LENGTH = 40;
    private static final Pattern ASCII_KEYWORD_PATTERN = Pattern.compile("[A-Za-z]+[A-Za-z0-9_+#.-]*|[0-9]+[A-Za-z0-9_+#.-]*");
    private static final Pattern ASCII_OR_DIGIT_PATTERN = Pattern.compile(".*[A-Za-z0-9].*");
    private static final Pattern CJK_PATTERN = Pattern.compile(".*[\\u4e00-\\u9fa5].*");
    private static final List<String> METADATA_NOISE_WORDS = List.of(
            "相关的视频", "相关视频", "相关片段", "帮我", "我想看", "想看", "查找", "查询",
            "搜索", "标题", "标签", "片名", "相关", "视频", "片段", "内容", "一下", "的"
    );

    @Resource
    private RestClient restClient;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AiProperties aiProperties;

    @Override
    public List<AiMatchedVideoVO> search(List<Double> queryVector, Integer topK, Double minScore) {
        return search(null, null, List.of(), queryVector, topK, minScore);
    }

    @Override
    public List<AiMatchedVideoVO> search(String keyword, List<Double> queryVector, Integer topK, Double minScore) {
        return search(keyword, keyword, List.of(), queryVector, topK, minScore);
    }

    @Override
    public List<AiMatchedVideoVO> search(String subtitleKeyword,
                                         String rawKeyword,
                                         List<String> metadataKeywords,
                                         List<Double> queryVector,
                                         Integer topK,
                                         Double minScore) {
        int limit = topK == null || topK <= 0 ? aiProperties.getRag().getDefaultTopK() : topK;
        List<AiMatchedVideoVO> vectorMatches = searchByVector(queryVector, limit, minScore);
        List<String> metadataSearchKeywords = resolveMetadataSearchKeywords(rawKeyword, metadataKeywords, subtitleKeyword);
        if (!StringUtils.hasText(subtitleKeyword) && metadataSearchKeywords.isEmpty()) {
            return mergeAndRankMatches(limit, vectorMatches);
        }

        List<AiMatchedVideoVO> subtitleKeywordMatches = StringUtils.hasText(subtitleKeyword)
                ? searchBySubtitleKeyword(subtitleKeyword, limit) : new ArrayList<>();
        List<AiMatchedVideoVO> titleMatches = searchByTitleKeyword(metadataSearchKeywords, limit);
        List<AiMatchedVideoVO> tagMatches = searchByTagKeyword(metadataSearchKeywords, limit);
        return mergeAndRankMatches(limit, subtitleKeywordMatches, vectorMatches, titleMatches, tagMatches);
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
            return parseSubtitleSearchResponse(responseJson, limit);
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

    private List<AiMatchedVideoVO> searchByTitleKeyword(List<String> keywords, int limit) {
        if (keywords == null || keywords.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_SEARCH_PATH));
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(
                    buildTitleKeywordSearchBody(keywords, resolveCandidateSize(limit))), ContentType.APPLICATION_JSON));
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

    private List<AiMatchedVideoVO> searchByTagKeyword(List<String> keywords, int limit) {
        if (keywords == null || keywords.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_SEARCH_PATH));
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(
                    buildTagKeywordSearchBody(keywords, resolveCandidateSize(limit))), ContentType.APPLICATION_JSON));
            Response response = restClient.performRequest(request);
            String responseJson = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return parseTagSearchResponse(responseJson, limit);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                log.warn("字幕向量索引不存在, index={}", aiProperties.getEs().getSubtitleVectorIndexName());
                return new ArrayList<>();
            }
            log.error("视频标签关键词检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        } catch (Exception e) {
            log.error("视频标签关键词检索失败", e);
            throw new BusinessException("AI 检索服务异常，请稍后再试", e);
        }
    }

    @Override
    public void deleteByVideoId(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return;
        }
        executeDeleteByQuery(buildDeleteByVideoIdBody(videoId), "删除视频字幕向量失败");
    }

    @Override
    public void deleteByFileIds(List<String> fileIds) {
        List<String> validFileIds = distinctTextList(fileIds);
        if (validFileIds.isEmpty()) {
            return;
        }
        executeDeleteByQuery(buildDeleteByFileIdsBody(validFileIds), "删除分 P 字幕向量失败");
    }

    @Override
    public void updateVideoMetaByVideoId(String videoId, String videoName, String videoCover, String tags) {
        if (videoId == null || videoId.isBlank()) {
            return;
        }
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_UPDATE_BY_QUERY_PATH));
            request.addParameter(AiConstants.ES_CONFLICTS_PARAM, AiConstants.ES_CONFLICTS_PROCEED);
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(
                    buildUpdateVideoMetaBody(videoId, videoName, videoCover, tags)), ContentType.APPLICATION_JSON));
            restClient.performRequest(request);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                return;
            }
            throw new BusinessException("更新字幕向量视频信息失败", e);
        } catch (Exception e) {
            throw new BusinessException("更新字幕向量视频信息失败", e);
        }
    }

    /**
     * 执行 ES delete-by-query。
     *
     * <p>索引不存在通常出现在首次上线或历史视频没有字幕向量的场景，删除类操作保持幂等即可。</p>
     *
     * @param body ES 查询体
     * @param errorMessage 业务异常提示
     */
    private void executeDeleteByQuery(Map<String, Object> body, String errorMessage) {
        try {
            Request request = new Request(HTTP_METHOD_POST, buildEsPath(AiConstants.ES_DELETE_BY_QUERY_PATH));
            request.addParameter(AiConstants.ES_CONFLICTS_PARAM, AiConstants.ES_CONFLICTS_PROCEED);
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON));
            restClient.performRequest(request);
        } catch (ResponseException e) {
            if (isIndexNotFound(e)) {
                return;
            }
            throw new BusinessException(errorMessage, e);
        } catch (Exception e) {
            throw new BusinessException(errorMessage, e);
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
        return buildTitleKeywordSearchBody(List.of(keyword), candidateSize);
    }

    private Map<String, Object> buildTitleKeywordSearchBody(List<String> keywords, int candidateSize) {
        List<Map<String, Object>> shouldQueries = new ArrayList<>();
        for (String keyword : keywords) {
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
            if (shouldUseTitleOrQuery(keyword)) {
                shouldQueries.add(Map.of(
                        "match", Map.of(
                                "videoName", Map.of("query", keyword, "operator", "or", "boost", aiProperties.getSearch().getTitleOrBoost())
                        )
                ));
            }
        }

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

    private Map<String, Object> buildTagKeywordSearchBody(String keyword, int candidateSize) {
        return buildTagKeywordSearchBody(List.of(keyword), candidateSize);
    }

    private Map<String, Object> buildTagKeywordSearchBody(List<String> keywords, int candidateSize) {
        List<Map<String, Object>> shouldQueries = new ArrayList<>();
        for (String keyword : keywords) {
            shouldQueries.add(Map.of(
                    "match_phrase", Map.of(
                            "tags", Map.of("query", keyword, "boost", aiProperties.getSearch().getTagPhraseBoost())
                    )
            ));
            shouldQueries.add(Map.of(
                    "match", Map.of(
                            "tags", Map.of("query", keyword, "operator", "and", "boost", aiProperties.getSearch().getTagAndBoost())
                    )
            ));
        }

        Map<String, Object> bool = new LinkedHashMap<>();
        bool.put("should", shouldQueries);
        bool.put("minimum_should_match", 1);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", candidateSize);
        body.put("_source", SOURCE_FIELDS);
        // 标签是视频级元数据，只用于说明视频相关性，不参与字幕片段问答证据。
        body.put("query", Map.of("bool", bool));
        return body;
    }

    private Map<String, Object> buildDeleteByVideoIdBody(String videoId) {
        return Map.of(
                "query", Map.of(
                        "term", Map.of(
                                "videoId", Map.of("value", videoId)
                        )
                )
        );
    }

    private Map<String, Object> buildDeleteByFileIdsBody(List<String> fileIds) {
        return Map.of(
                "query", Map.of(
                        "terms", Map.of(
                                "fileId", fileIds
                        )
                )
        );
    }

    private Map<String, Object> buildUpdateVideoMetaBody(String videoId, String videoName, String videoCover, String tags) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("videoName", videoName);
        params.put("videoCover", videoCover);
        params.put("tags", tags);

        Map<String, Object> script = new LinkedHashMap<>();
        script.put("lang", "painless");
        // 只刷新展示字段，不碰 content/contentVector，避免把未变化分 P 重新向量化。
        script.put("source", "ctx._source.videoName = params.videoName; "
                + "ctx._source.videoCover = params.videoCover; ctx._source.tags = params.tags;");
        script.put("params", params);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("script", script);
        body.put("query", Map.of(
                "term", Map.of(
                        "videoId", Map.of("value", videoId)
                )
        ));
        return body;
    }

    private List<String> distinctTextList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (!StringUtils.hasText(value) || result.contains(value)) {
                continue;
            }
            result.add(value);
        }
        return result;
    }

    /**
     * 整理标题/标签检索词。
     *
     * <p>字幕检索词通常会被模型扩写成语义描述，适合向量检索；标题和标签更需要短词命中。
     * 这里先从用户原话和模型给出的 metadataKeywords 中抽取英文缩写、数字等核心词，
     * 再保留清洗后的中文短语；字幕检索词不再兜底到元数据检索，避免长扩写词拆出 “1”
     * 这类弱 token 后命中 test1。</p>
     *
     * @param rawKeyword 用户原始问题
     * @param metadataKeywords 模型输出的元数据短关键词
     * @param subtitleKeyword 字幕检索词
     * @return 去重后的标题/标签检索词
     */
    private List<String> resolveMetadataSearchKeywords(String rawKeyword,
                                                       List<String> metadataKeywords,
                                                       String subtitleKeyword) {
        List<String> result = new ArrayList<>();
        addAsciiKeywords(result, rawKeyword);
        addCleanKeyword(result, rawKeyword);
        if (metadataKeywords != null) {
            for (String metadataKeyword : metadataKeywords) {
                addAsciiKeywords(result, metadataKeyword);
                addCleanKeyword(result, metadataKeyword);
            }
        }
        if (result.size() <= METADATA_KEYWORD_MAX_COUNT) {
            return result;
        }
        return new ArrayList<>(result.subList(0, METADATA_KEYWORD_MAX_COUNT));
    }

    /**
     * 判断标题检索是否需要 OR 兜底。
     *
     * <p>英文缩写、数字和中英混合词容易被 IK 拆成单字符 token，例如 f1 会拆出 1。
     * 这类关键词只保留 phrase/AND；OR 只给较长中文短语兜底。</p>
     *
     * @param keyword 标题检索词
     * @return true 表示允许追加低权重 OR 查询
     */
    private boolean shouldUseTitleOrQuery(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return false;
        }
        String trimmedKeyword = keyword.trim();
        return trimmedKeyword.length() >= 4
                && CJK_PATTERN.matcher(trimmedKeyword).matches()
                && !ASCII_OR_DIGIT_PATTERN.matcher(trimmedKeyword).matches();
    }

    private void addAsciiKeywords(List<String> target, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        Matcher matcher = ASCII_KEYWORD_PATTERN.matcher(keyword);
        while (matcher.find()) {
            addKeywordIfAbsent(target, matcher.group());
        }
    }

    private void addCleanKeyword(List<String> target, String keyword) {
        String cleanKeyword = cleanMetadataKeyword(keyword);
        addKeywordIfAbsent(target, cleanKeyword);
    }

    private String cleanMetadataKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        String cleanKeyword = keyword.trim();
        for (String noiseWord : METADATA_NOISE_WORDS) {
            cleanKeyword = cleanKeyword.replace(noiseWord, " ");
        }
        cleanKeyword = cleanKeyword
                .replaceAll("[\\p{Punct}，。！？、：；（）《》【】“”‘’]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleanKeyword.length() > METADATA_KEYWORD_MAX_LENGTH) {
            return "";
        }
        return cleanKeyword;
    }

    private void addKeywordIfAbsent(List<String> target, String keyword) {
        if (!StringUtils.hasText(keyword) || target.size() >= METADATA_KEYWORD_MAX_COUNT) {
            return;
        }
        String trimmedKeyword = keyword.trim();
        for (String existingKeyword : target) {
            if (existingKeyword.toLowerCase(Locale.ROOT).equals(trimmedKeyword.toLowerCase(Locale.ROOT))) {
                return;
            }
        }
        target.add(trimmedKeyword);
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
            matchedVideo.setMatchSource(AiConstants.MATCH_SOURCE_VECTOR);
            matchedVideo.getMatchDetails().add(buildMatchDetail(
                    AiConstants.MATCH_TYPE_SUBTITLE,
                    AiConstants.MATCH_SOURCE_VECTOR,
                    matchedVideo.getMatchedText(),
                    matchedVideo.getScore(),
                    matchedVideo.getStartTime(),
                    matchedVideo.getEndTime()
            ));
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(videoMap.values());
    }

    private List<AiMatchedVideoVO> parseSubtitleSearchResponse(String responseJson, int limit) throws Exception {
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
            matchedVideo.setMatchSource(AiConstants.MATCH_SOURCE_SUBTITLE);
            matchedVideo.getMatchDetails().add(buildMatchDetail(
                    AiConstants.MATCH_TYPE_SUBTITLE,
                    AiConstants.MATCH_SOURCE_SUBTITLE,
                    matchedVideo.getMatchedText(),
                    matchedVideo.getScore(),
                    matchedVideo.getStartTime(),
                    matchedVideo.getEndTime()
            ));
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(videoMap.values());
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
            matchedVideo.setMatchSource(AiConstants.MATCH_SOURCE_TITLE);
            matchedVideo.getMatchDetails().add(buildMatchDetail(
                    AiConstants.MATCH_TYPE_TITLE,
                    AiConstants.MATCH_SOURCE_TITLE,
                    matchedVideo.getMatchedText(),
                    matchedVideo.getScore(),
                    null,
                    null
            ));
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(videoMap.values());
    }

    private List<AiMatchedVideoVO> parseTagSearchResponse(String responseJson, int limit) throws Exception {
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
            String tags = source.path("tags").asText("");
            matchedVideo.setMatchedText("标签匹配：" + tags);
            matchedVideo.setStartTime(null);
            matchedVideo.setEndTime(null);
            matchedVideo.setScore(normalizeTagScore(hit.path("_score").asDouble(0D), maxScore));
            matchedVideo.setMatchType(AiConstants.MATCH_TYPE_TAG);
            matchedVideo.setMatchSource(AiConstants.MATCH_SOURCE_TAG);
            matchedVideo.getMatchDetails().add(buildMatchDetail(
                    AiConstants.MATCH_TYPE_TAG,
                    AiConstants.MATCH_SOURCE_TAG,
                    matchedVideo.getMatchedText(),
                    matchedVideo.getScore(),
                    null,
                    null
            ));
            videoMap.put(videoId, matchedVideo);

            if (videoMap.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(videoMap.values());
    }

    @SafeVarargs
    private final List<AiMatchedVideoVO> mergeAndRankMatches(int limit, List<AiMatchedVideoVO>... matchGroups) {
        Map<String, AiMatchedVideoVO> merged = new LinkedHashMap<>();
        for (List<AiMatchedVideoVO> matchGroup : matchGroups) {
            if (matchGroup == null || matchGroup.isEmpty()) {
                continue;
            }
            for (AiMatchedVideoVO matchedVideo : matchGroup) {
                if (matchedVideo == null || !StringUtils.hasText(matchedVideo.getVideoId())) {
                    continue;
                }
                AiMatchedVideoVO target = merged.computeIfAbsent(matchedVideo.getVideoId(), key -> copyVideoBase(matchedVideo));
                mergeMatchDetails(target, matchedVideo);
                refreshPrimaryMatch(target);
            }
        }

        List<AiMatchedVideoVO> result = new ArrayList<>(merged.values());
        result.sort((left, right) -> {
            int scoreCompare = Double.compare(defaultScore(right.getScore()), defaultScore(left.getScore()));
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return Integer.compare(detailCount(right), detailCount(left));
        });
        if (result.size() <= limit) {
            return result;
        }
        return new ArrayList<>(result.subList(0, limit));
    }

    private AiMatchedVideoVO copyVideoBase(AiMatchedVideoVO source) {
        AiMatchedVideoVO target = new AiMatchedVideoVO();
        target.setVideoId(source.getVideoId());
        target.setVideoName(source.getVideoName());
        target.setVideoCover(source.getVideoCover());
        return target;
    }

    private void mergeMatchDetails(AiMatchedVideoVO target, AiMatchedVideoVO source) {
        List<AiMatchDetailVO> sourceDetails = source.getMatchDetails();
        if (sourceDetails == null || sourceDetails.isEmpty()) {
            sourceDetails = List.of(buildMatchDetail(
                    source.getMatchType(),
                    source.getMatchSource(),
                    source.getMatchedText(),
                    source.getScore(),
                    source.getStartTime(),
                    source.getEndTime()
            ));
        }
        for (AiMatchDetailVO detail : sourceDetails) {
            if (detail == null || !StringUtils.hasText(detail.getMatchType())
                    || !StringUtils.hasText(detail.getMatchedText())) {
                continue;
            }
            upsertMatchDetail(target.getMatchDetails(), detail);
        }
    }

    private void upsertMatchDetail(List<AiMatchDetailVO> targetDetails, AiMatchDetailVO incoming) {
        for (int i = 0; i < targetDetails.size(); i++) {
            AiMatchDetailVO existing = targetDetails.get(i);
            if (!isSameDetail(existing, incoming)) {
                continue;
            }
            if (defaultScore(incoming.getScore()) > defaultScore(existing.getScore())) {
                targetDetails.set(i, copyMatchDetail(incoming));
            }
            return;
        }
        targetDetails.add(copyMatchDetail(incoming));
    }

    private boolean isSameDetail(AiMatchDetailVO left, AiMatchDetailVO right) {
        return left != null && right != null
                && left.getMatchType().equals(right.getMatchType())
                && left.getMatchedText().equals(right.getMatchedText());
    }

    private void refreshPrimaryMatch(AiMatchedVideoVO matchedVideo) {
        if (matchedVideo.getMatchDetails().isEmpty()) {
            return;
        }
        matchedVideo.getMatchDetails().sort(Comparator
                .comparingDouble((AiMatchDetailVO detail) -> defaultScore(detail.getScore()))
                .reversed());
        AiMatchDetailVO primaryDetail = matchedVideo.getMatchDetails().get(0);
        matchedVideo.setMatchedText(primaryDetail.getMatchedText());
        matchedVideo.setStartTime(primaryDetail.getStartTime());
        matchedVideo.setEndTime(primaryDetail.getEndTime());
        matchedVideo.setScore(primaryDetail.getScore());
        matchedVideo.setMatchType(primaryDetail.getMatchType());
        matchedVideo.setMatchSource(primaryDetail.getMatchSource());
    }

    private AiMatchDetailVO buildMatchDetail(String matchType,
                                             String matchSource,
                                             String matchedText,
                                             Double score,
                                             Double startTime,
                                             Double endTime) {
        AiMatchDetailVO detail = new AiMatchDetailVO();
        detail.setMatchType(matchType);
        detail.setMatchSource(matchSource);
        detail.setMatchedText(matchedText);
        detail.setScore(score);
        detail.setStartTime(startTime);
        detail.setEndTime(endTime);
        return detail;
    }

    private AiMatchDetailVO copyMatchDetail(AiMatchDetailVO source) {
        return buildMatchDetail(
                source.getMatchType(),
                source.getMatchSource(),
                source.getMatchedText(),
                source.getScore(),
                source.getStartTime(),
                source.getEndTime()
        );
    }

    private int detailCount(AiMatchedVideoVO matchedVideo) {
        return matchedVideo.getMatchDetails() == null ? 0 : matchedVideo.getMatchDetails().size();
    }

    private double defaultScore(Double score) {
        return score == null ? 0D : score;
    }

    private AiMatchedVideoVO buildMatchedVideo(JsonNode source) {
        AiMatchedVideoVO matchedVideo = new AiMatchedVideoVO();
        matchedVideo.setVideoId(source.path("videoId").asText(""));
        matchedVideo.setVideoName(source.path("videoName").asText(""));
        matchedVideo.setVideoCover(source.path("videoCover").asText(""));
        matchedVideo.setMatchedText(source.path("content").asText(""));
        matchedVideo.setStartTime(source.path("startTime").asDouble(0D));
        matchedVideo.setEndTime(source.path("endTime").asDouble(0D));
        matchedVideo.setMatchSource(AiConstants.MATCH_SOURCE_SUBTITLE);
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

    private double normalizeTagScore(double score, double maxScore) {
        if (score <= 0 || maxScore <= 0) {
            return 0D;
        }
        // 标签和标题一样是视频级证据，分数区间单独配置，方便后续按真实数据调权。
        double tagScoreMax = aiProperties.getSearch().getTagScoreMax();
        return roundScore(Math.min(tagScoreMax, Math.max(aiProperties.getSearch().getTagScoreMin(), score / maxScore * tagScoreMax)));
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

