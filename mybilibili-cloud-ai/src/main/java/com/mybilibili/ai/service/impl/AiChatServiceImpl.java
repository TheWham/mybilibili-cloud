package com.mybilibili.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybilibili.ai.client.AiChatModelClient;
import com.mybilibili.ai.client.OllamaClient;
import com.mybilibili.ai.config.AiProperties;
import com.mybilibili.ai.constants.AiConstants;
import com.mybilibili.ai.entity.dto.AiChatRequestDTO;
import com.mybilibili.ai.entity.dto.AiConversationContextDTO;
import com.mybilibili.ai.entity.vo.AiChatResultVO;
import com.mybilibili.ai.entity.vo.AiChatSessionSummaryVO;
import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;
import com.mybilibili.ai.entity.vo.AiQueryAnalysisVO;
import com.mybilibili.ai.entity.vo.AiSuggestionActionVO;
import com.mybilibili.ai.service.AiChatService;
import com.mybilibili.ai.service.AiChatSessionService;
import com.mybilibili.ai.service.AiSubtitleVectorService;
import com.mybilibili.base.exception.BusinessException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * AI 问答服务实现。
 *
 * <p>当前版本把一次提问拆成三个阶段：先分析用户真实意图，再做字幕向量检索，
 * 最后基于命中片段组织回答。这样可以减少“用户随口一说就直接向量化”带来的召回偏差。</p>
 */
@Service("aiChatService")
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private static final String ANALYSIS_RESPONSE_EXAMPLE = "{\"intentType\":\"video_search\","
            + "\"searchKeyword\":\"流浪地球 科幻电影 相关片段\",\"needClarification\":false,"
            + "\"clarificationQuestion\":\"\",\"confidence\":0.92,"
            + "\"explanation\":\"用户想找和流浪地球有关的视频片段\"}";

    @Resource
    private OllamaClient ollamaClient;
    @Resource
    private AiChatModelClient aiChatModelClient;
    @Resource
    private AiSubtitleVectorService aiSubtitleVectorService;
    @Resource
    private AiChatSessionService aiChatSessionService;
    @Resource
    private AiProperties aiProperties;
    @Resource
    private ObjectMapper objectMapper;
    @Resource(name = "aiChatExecutor")
    private Executor aiChatExecutor;

    @Override
    public AiChatResultVO chat(AiChatRequestDTO request) {
        return buildChatResult(request, null);
    }

    @Override
    public SseEmitter streamChat(AiChatRequestDTO request) {
        AiChatRequestDTO safeRequest = request == null ? new AiChatRequestDTO() : request;
        String conversationId = resolveConversationId(safeRequest);
        SseEmitter emitter = new SseEmitter(aiProperties.getChat().getSseTimeoutMs());
        aiChatExecutor.execute(() -> {
            try {
                Map<String, Object> startData = new HashMap<>(1);
                startData.put(AiConstants.SSE_FIELD_CONVERSATION_ID, conversationId);
                sendEvent(emitter, AiConstants.SSE_EVENT_START, startData);

                if (isInitAction(safeRequest)) {
                    AiChatResultVO initResult = buildWelcomeResult(conversationId);
                    sendEvent(emitter, AiConstants.SSE_EVENT_WELCOME, initResult);
                    sendEvent(emitter, AiConstants.SSE_EVENT_SUGGESTIONS, initResult.getSuggestionActions());
                    sendEvent(emitter, AiConstants.SSE_EVENT_DONE, initResult);
                    emitter.complete();
                    return;
                }

                AiChatResultVO resultVO = buildChatResult(safeRequest, delta -> sendDelta(emitter, delta));
                sendEvent(emitter, AiConstants.SSE_EVENT_VIDEOS, resultVO.getVideos());
                sendEvent(emitter, AiConstants.SSE_EVENT_SUGGESTIONS, resultVO.getSuggestionActions());
                sendEvent(emitter, AiConstants.SSE_EVENT_DONE, resultVO);
                emitter.complete();
            } catch (Exception e) {
                log.error("AI SSE 问答失败, conversationId={}", conversationId, e);
                try {
                    Map<String, Object> errorData = new HashMap<>(1);
                    errorData.put(AiConstants.SSE_FIELD_MESSAGE, getClientErrorMessage(e));
                    sendEvent(emitter, AiConstants.SSE_EVENT_ERROR, errorData);
                    emitter.complete();
                } catch (Exception sendError) {
                    emitter.completeWithError(sendError);
                }
            }
        });
        return emitter;
    }

    /**
     * 判断是否为初始化动作。
     *
     * <p>AI 对话页第一次打开时只需要欢迎语和推荐问题，不应该触发向量检索。</p>
     */
    private boolean isInitAction(AiChatRequestDTO request) {
        return request != null
                && StringUtils.equalsIgnoreCase(AiConstants.SESSION_ACTION_INIT, request.getSessionAction());
    }

    /**
     * 构造欢迎语结果。
     */
    private AiChatResultVO buildWelcomeResult(String conversationId) {
        AiChatResultVO resultVO = new AiChatResultVO();
        resultVO.setConversationId(conversationId);
        resultVO.setAnswer("hi, 我是mybilibili视频助手，想查询视频可以随时问我哦；试着可以这样提问我想找一部xx类型的视频");
        resultVO.setQueryAnalysis(buildWelcomeQueryAnalysis());
        resultVO.setVideos(Collections.emptyList());
        resultVO.setSuggestionActions(buildWelcomeSuggestionActions());
        resultVO.setSuggestions(toSuggestionTexts(resultVO.getSuggestionActions()));
        resultVO.setContext(new AiConversationContextDTO());
        return resultVO;
    }

    private AiQueryAnalysisVO buildWelcomeQueryAnalysis() {
        AiQueryAnalysisVO queryAnalysis = new AiQueryAnalysisVO();
        queryAnalysis.setOriginalQuestion("");
        queryAnalysis.setIntentType(AiConstants.INTENT_TYPE_UNKNOWN);
        queryAnalysis.setSearchKeyword("");
        queryAnalysis.setNeedClarification(Boolean.FALSE);
        queryAnalysis.setClarificationQuestion("");
        queryAnalysis.setConfidence(1.0D);
        queryAnalysis.setExplanation("当前是 AI 助手欢迎语。");
        return queryAnalysis;
    }

    private List<AiSuggestionActionVO> buildWelcomeSuggestionActions() {
        List<AiSuggestionActionVO> suggestions = new ArrayList<>(3);
        suggestions.add(buildSuggestionAction("我想找一部科幻电影", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        suggestions.add(buildSuggestionAction("我想看流浪地球相关片段", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        suggestions.add(buildSuggestionAction("帮我找一个技术讲解视频", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        return suggestions;
    }

    /**
     * 核心问答链路。
     *
     * <p>先分析意图，再决定是否进入向量召回；如果问题信息不足，直接返回追问，
     * 避免把“我想找个电影”这类泛化表达送入 embedding。</p>
     *
     * @param request 请求参数
     * @param deltaConsumer 流式输出回调，同步接口可传 null
     * @return 结构化问答结果
     */
    private AiChatResultVO buildChatResult(AiChatRequestDTO request, Consumer<String> deltaConsumer) {
        AiChatRequestDTO safeRequest = request == null ? new AiChatRequestDTO() : request;
        String message = resolveMessage(safeRequest);
        String conversationId = resolveConversationId(safeRequest);
        AiConversationContextDTO previousContext = resolvePreviousContext(safeRequest);
        AiSuggestionActionVO sourceSuggestion = findSourceSuggestion(previousContext, safeRequest.getSourceSuggestionId());
        int topK = normalizeTopK(safeRequest.getTopK());
        long startTime = System.currentTimeMillis();

        AiQueryAnalysisVO queryAnalysis = analyzeQuestion(message, previousContext, sourceSuggestion);
        AiChatResultVO resultVO = new AiChatResultVO();
        resultVO.setConversationId(conversationId);
        resultVO.setQueryAnalysis(queryAnalysis);
        resultVO.setVideos(Collections.emptyList());

        if (Boolean.TRUE.equals(queryAnalysis.getNeedClarification())) {
            String answer = buildClarificationAnswer(queryAnalysis);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, queryAnalysis, Collections.emptyList(),
                    buildClarificationSuggestionActions(queryAnalysis));
            saveChatRound(safeRequest, conversationId, message, resultVO);
            log.info("AI 问答返回追问, message={}, conversationId={}, intentType={}, totalCost={}ms",
                    message, conversationId, queryAnalysis.getIntentType(), System.currentTimeMillis() - startTime);
            return resultVO;
        }

        String searchKeyword = resolveQueryKeyword(queryAnalysis, message);
        List<Double> queryVector = buildQueryVector(searchKeyword);
        long embeddingEndTime = System.currentTimeMillis();
        List<AiMatchedVideoVO> searchedVideos = searchVideosByIntent(queryAnalysis, searchKeyword, queryVector, topK);
        long searchEndTime = System.currentTimeMillis();

        /*
         * 推荐问题不是普通关键词。比如“查看《xxx》里的相关片段”这类按钮文案，
         * 真正含义是沿着上一轮结果继续看某个视频，所以这里先把上一轮命中的片段放回上下文。
         */
        List<AiMatchedVideoVO> contextVideos = findContextMatches(previousContext, sourceSuggestion);
        List<AiMatchedVideoVO> matchedVideos = mergeVideoMatches(contextVideos, searchedVideos, topK);
        resultVO.setVideos(matchedVideos);

        if (matchedVideos.isEmpty()) {
            String answer = buildNoMatchAnswer(message, queryAnalysis);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, queryAnalysis, matchedVideos, buildNoMatchSuggestionActions(message));
            saveChatRound(safeRequest, conversationId, message, resultVO);
            log.info(
                    "AI 问答未命中, message={}, searchKeyword={}, conversationId={}, embeddingCost={}ms, searchCost={}ms, totalCost={}ms",
                    message,
                    searchKeyword,
                    conversationId,
                    embeddingEndTime - startTime,
                    searchEndTime - embeddingEndTime,
                    System.currentTimeMillis() - startTime
            );
            return resultVO;
        }

        List<AiMatchedVideoVO> subtitleMatches = filterSubtitleMatches(matchedVideos);
        List<AiMatchedVideoVO> titleMatches = filterTitleMatches(matchedVideos);
        if (!titleMatches.isEmpty() && isShortKeyword(searchKeyword)
                && !hasSubtitleKeywordHit(subtitleMatches, searchKeyword)) {
            /*
             * 短词很容易被语义检索带偏。没有字幕文本直接包含关键词时，
             * 优先展示标题相关结果，别把弱相关字幕喂给模型硬组织答案。
             */
            resultVO.setVideos(titleMatches);
            String answer = buildTitleMatchAnswer(message, queryAnalysis, titleMatches);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, queryAnalysis, titleMatches,
                    buildFollowUpSuggestionActions(message, titleMatches));
            saveChatRound(safeRequest, conversationId, message, resultVO);
            log.info(
                    "AI 问答返回短词标题匹配, message={}, searchKeyword={}, conversationId={}, titleCount={}, totalCost={}ms",
                    message,
                    searchKeyword,
                    conversationId,
                    titleMatches.size(),
                    System.currentTimeMillis() - startTime
            );
            return resultVO;
        }

        if (subtitleMatches.isEmpty()) {
            String answer = buildTitleMatchAnswer(message, queryAnalysis, matchedVideos);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, queryAnalysis, matchedVideos,
                    buildFollowUpSuggestionActions(message, matchedVideos));
            saveChatRound(safeRequest, conversationId, message, resultVO);
            log.info(
                    "AI 问答返回标题匹配, message={}, searchKeyword={}, conversationId={}, matchedCount={}, embeddingCost={}ms, searchCost={}ms, totalCost={}ms",
                    message,
                    searchKeyword,
                    conversationId,
                    matchedVideos.size(),
                    embeddingEndTime - startTime,
                    searchEndTime - embeddingEndTime,
                    System.currentTimeMillis() - startTime
            );
            return resultVO;
        }

        String answer = buildAiAnswer(message, previousContext, sourceSuggestion, queryAnalysis, subtitleMatches, deltaConsumer);
        long answerEndTime = System.currentTimeMillis();
        fillResult(resultVO, message, answer, queryAnalysis, matchedVideos,
                buildFollowUpSuggestionActions(message, matchedVideos));
        saveChatRound(safeRequest, conversationId, message, resultVO);
        log.info(
                "AI 问答完成, message={}, searchKeyword={}, conversationId={}, matchedCount={}, embeddingCost={}ms, searchCost={}ms, answerCost={}ms, totalCost={}ms",
                message,
                searchKeyword,
                conversationId,
                matchedVideos.size(),
                embeddingEndTime - startTime,
                searchEndTime - embeddingEndTime,
                answerEndTime - searchEndTime,
                answerEndTime - startTime
        );
        return resultVO;
    }

    /**
     * 优先读取 Redis 会话上下文，兼容前端继续透传 context 的第一版协议。
     */
    private AiConversationContextDTO resolvePreviousContext(AiChatRequestDTO request) {
        if (request == null) {
            return null;
        }
        AiConversationContextDTO redisContext = aiChatSessionService.getContext(request.getConversationId());
        if (redisContext != null) {
            return redisContext;
        }
        return request.getContext();
    }

    /**
     * 保存登录用户的一轮完整问答。
     *
     * <p>匿名用户不做历史持久化，只保留前端透传的临时上下文；登录用户则把完整消息追加到 Redis，
     * 同时把摘要放回结果对象，方便前端刷新左侧会话列表。</p>
     */
    private void saveChatRound(AiChatRequestDTO request,
                               String conversationId,
                               String message,
                               AiChatResultVO resultVO) {
        if (request == null || resultVO == null) {
            return;
        }
        AiChatSessionSummaryVO summaryVO = aiChatSessionService.saveRound(
                request.getLoginUserId(),
                conversationId,
                message,
                resultVO
        );
        resultVO.setSessionSummary(summaryVO);
        if (summaryVO == null) {
            aiChatSessionService.saveContext(conversationId, resultVO.getContext());
        }
    }

    /**
     * 按问题意图选择检索方式。
     *
     * <p>第二版仍复用现有 ES 检索实现，当前 title/subtitle/hybrid 的差异主要体现在
     * 返回给前端的意图表达和命中来源标识上，避免把协议先做复杂。</p>
     */
    private List<AiMatchedVideoVO> searchVideosByIntent(AiQueryAnalysisVO queryAnalysis,
                                                        String searchKeyword,
                                                        List<Double> queryVector,
                                                        int topK) {
        List<AiMatchedVideoVO> searchedVideos = aiSubtitleVectorService.search(
                searchKeyword,
                queryVector,
                topK,
                aiProperties.getRag().getMinScore()
        );
        applyMatchSourceByIntent(searchedVideos, queryAnalysis);
        return searchedVideos;
    }

    private void applyMatchSourceByIntent(List<AiMatchedVideoVO> videos, AiQueryAnalysisVO queryAnalysis) {
        if (videos == null || videos.isEmpty()) {
            return;
        }
        String intentType = queryAnalysis == null ? "" : StringUtils.defaultString(queryAnalysis.getIntentType());
        for (AiMatchedVideoVO video : videos) {
            if (video == null) {
                continue;
            }
            if (AiConstants.MATCH_TYPE_TITLE.equals(video.getMatchType())) {
                video.setMatchSource(AiConstants.MATCH_SOURCE_TITLE);
                continue;
            }
            if (AiConstants.INTENT_TYPE_VIDEO_SUBTITLE_SEARCH.equals(intentType)) {
                video.setMatchSource(AiConstants.MATCH_SOURCE_SUBTITLE);
                continue;
            }
            if (AiConstants.INTENT_TYPE_VIDEO_TITLE_SEARCH.equals(intentType)) {
                video.setMatchSource(AiConstants.MATCH_SOURCE_TITLE);
                continue;
            }
            if (StringUtils.isBlank(video.getMatchSource())) {
                video.setMatchSource(AiConstants.MATCH_SOURCE_HYBRID);
            } else if (AiConstants.MATCH_SOURCE_VECTOR.equals(video.getMatchSource())
                    || AiConstants.MATCH_SOURCE_SUBTITLE.equals(video.getMatchSource())) {
                video.setMatchSource(AiConstants.MATCH_SOURCE_HYBRID);
            }
        }
    }

    private String resolveMessage(AiChatRequestDTO request) {
        String message = StringUtils.trimToEmpty(request.getMessage());
        if (StringUtils.isBlank(message)) {
            message = StringUtils.trimToEmpty(request.getKeyword());
        }
        if (StringUtils.isBlank(message)) {
            throw new BusinessException("关键词不能为空");
        }
        return message;
    }

    private String resolveConversationId(AiChatRequestDTO request) {
        String conversationId = StringUtils.trimToEmpty(request.getConversationId());
        if (StringUtils.isBlank(conversationId)) {
            conversationId = UUID.randomUUID().toString();
            request.setConversationId(conversationId);
        }
        return conversationId;
    }

    /**
     * 先分析问题，再决定是否进入检索。
     *
     * <p>推荐问题场景优先继承上一轮语义，普通问题则交给模型做结构化改写。
     * 如果分析阶段失败，统一降级到“原问题直接检索”，保证主链路可用。</p>
     */
    private AiQueryAnalysisVO analyzeQuestion(String message,
                                              AiConversationContextDTO previousContext,
                                              AiSuggestionActionVO sourceSuggestion) {
        if (sourceSuggestion != null) {
            return buildSuggestionQueryAnalysis(message, previousContext, sourceSuggestion);
        }

        String prompt = buildQueryAnalysisPrompt(message, previousContext);
        try {
            String response = aiChatModelClient.chat(aiProperties.getChat().getQueryAnalysisSystemPrompt(), prompt);
            AiQueryAnalysisVO analysis = parseQueryAnalysis(message, response);
            normalizeQueryAnalysis(analysis, message);
            return analysis;
        } catch (Exception e) {
            log.warn("AI 提问分析失败，使用原问题降级检索, message={}", message, e);
            return buildFallbackQueryAnalysis(message);
        }
    }

    private AiQueryAnalysisVO buildSuggestionQueryAnalysis(String message,
                                                           AiConversationContextDTO previousContext,
                                                           AiSuggestionActionVO sourceSuggestion) {
        AiQueryAnalysisVO analysis = new AiQueryAnalysisVO();
        analysis.setOriginalQuestion(message);
        analysis.setIntentType(AiConstants.INTENT_TYPE_FOLLOW_UP);
        if (previousContext != null && previousContext.getQueryAnalysis() != null
                && StringUtils.isNotBlank(previousContext.getQueryAnalysis().getSearchKeyword())) {
            analysis.setSearchKeyword(previousContext.getQueryAnalysis().getSearchKeyword());
        } else if (previousContext != null && StringUtils.isNotBlank(previousContext.getLastQuestion())) {
            analysis.setSearchKeyword(previousContext.getLastQuestion());
        } else {
            analysis.setSearchKeyword(message);
        }
        analysis.setNeedClarification(Boolean.FALSE);
        analysis.setClarificationQuestion("");
        analysis.setConfidence(0.95D);
        analysis.setExplanation("这是基于上一轮结果继续追问，我会优先沿用上一轮的检索语义。");
        return analysis;
    }

    private String buildQueryAnalysisPrompt(String message, AiConversationContextDTO previousContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请分析用户当前问题，并输出 JSON。")
                .append("JSON 字段固定为 intentType、searchKeyword、needClarification、clarificationQuestion、confidence、explanation。")
                .append("其中 needClarification 为 true 时，不要生成宽泛检索词，要直接给出追问文案。")
                .append("searchKeyword 要适合做视频字幕向量检索，尽量保留主题、片名、类型、业务词，不要带客套话。")
                .append("clarificationQuestion 和 explanation 控制在")
                .append(aiProperties.getChat().getQueryAnalysisMaxWords())
                .append("字以内。")
                .append("只返回 JSON，不要输出代码块。")
                .append("示例：").append(ANALYSIS_RESPONSE_EXAMPLE).append("\n");
        if (previousContext != null && StringUtils.isNotBlank(previousContext.getLastQuestion())) {
            prompt.append("上一轮问题：").append(previousContext.getLastQuestion()).append("\n");
            if (previousContext.getQueryAnalysis() != null
                    && StringUtils.isNotBlank(previousContext.getQueryAnalysis().getSearchKeyword())) {
                prompt.append("上一轮检索词：").append(previousContext.getQueryAnalysis().getSearchKeyword()).append("\n");
            }
            if (StringUtils.isNotBlank(previousContext.getLastAnswer())) {
                prompt.append("上一轮回答摘要：").append(previousContext.getLastAnswer()).append("\n");
            }
        }
        prompt.append("用户本轮问题：").append(message);
        return prompt.toString();
    }

    private AiQueryAnalysisVO parseQueryAnalysis(String message, String response) throws Exception {
        JsonNode root = objectMapper.readTree(StringUtils.trimToEmpty(response));
        AiQueryAnalysisVO analysis = new AiQueryAnalysisVO();
        analysis.setOriginalQuestion(message);
        analysis.setIntentType(root.path("intentType").asText(""));
        analysis.setSearchKeyword(root.path("searchKeyword").asText(""));
        analysis.setNeedClarification(root.path("needClarification").asBoolean(false));
        analysis.setClarificationQuestion(root.path("clarificationQuestion").asText(""));
        analysis.setConfidence(root.path("confidence").asDouble(0.0D));
        analysis.setExplanation(root.path("explanation").asText(""));
        return analysis;
    }

    private void normalizeQueryAnalysis(AiQueryAnalysisVO analysis, String message) {
        if (analysis == null) {
            throw new BusinessException("问题分析结果不能为空");
        }
        analysis.setOriginalQuestion(message);
        if (StringUtils.isBlank(analysis.getIntentType())) {
            analysis.setIntentType(AiConstants.INTENT_TYPE_UNKNOWN);
        }
        if (Boolean.TRUE.equals(analysis.getNeedClarification())) {
            if (StringUtils.isBlank(analysis.getClarificationQuestion())) {
                analysis.setClarificationQuestion("你想找哪一类视频？可以告诉我片名、类型、演员、剧情关键词或你记得的片段。");
            }
            analysis.setSearchKeyword("");
        } else if (StringUtils.isBlank(analysis.getSearchKeyword())) {
            analysis.setSearchKeyword(message);
        }
        if (analysis.getConfidence() == null) {
            analysis.setConfidence(0.0D);
        }
        analysis.setConfidence(Math.max(0D, Math.min(analysis.getConfidence(), 1D)));
        if (StringUtils.isBlank(analysis.getExplanation())) {
            analysis.setExplanation("我会按你的问题原意继续检索相关视频内容。");
        }
        normalizeIntentType(analysis);
    }

    /**
     * 统一整理意图类型，避免模型时而返回泛化 video_search，时而返回更细分结果。
     */
    private void normalizeIntentType(AiQueryAnalysisVO analysis) {
        if (analysis == null) {
            return;
        }
        String intentType = StringUtils.defaultString(analysis.getIntentType());
        String searchKeyword = StringUtils.defaultString(analysis.getSearchKeyword());
        if (Boolean.TRUE.equals(analysis.getNeedClarification())) {
            return;
        }
        if (AiConstants.INTENT_TYPE_FOLLOW_UP.equals(intentType)
                || AiConstants.INTENT_TYPE_KNOWLEDGE_QUESTION.equals(intentType)) {
            return;
        }
        if (StringUtils.containsAny(searchKeyword, "标题", "片名")) {
            analysis.setIntentType(AiConstants.INTENT_TYPE_VIDEO_TITLE_SEARCH);
            return;
        }
        if (StringUtils.containsAny(searchKeyword, "片段", "字幕")) {
            analysis.setIntentType(AiConstants.INTENT_TYPE_VIDEO_SUBTITLE_SEARCH);
            return;
        }
        if (AiConstants.INTENT_TYPE_VIDEO_SEARCH.equals(intentType) || StringUtils.isBlank(intentType)) {
            analysis.setIntentType(AiConstants.INTENT_TYPE_VIDEO_HYBRID_SEARCH);
        }
    }

    private AiQueryAnalysisVO buildFallbackQueryAnalysis(String message) {
        AiQueryAnalysisVO analysis = new AiQueryAnalysisVO();
        analysis.setOriginalQuestion(message);
        analysis.setIntentType(AiConstants.INTENT_TYPE_UNKNOWN);
        analysis.setSearchKeyword(message);
        analysis.setNeedClarification(Boolean.FALSE);
        analysis.setClarificationQuestion("");
        analysis.setConfidence(0.0D);
        analysis.setExplanation("本轮问题分析未命中结构化结果，已按原问题继续检索。");
        return analysis;
    }

    private String resolveQueryKeyword(AiQueryAnalysisVO queryAnalysis, String message) {
        if (queryAnalysis == null || StringUtils.isBlank(queryAnalysis.getSearchKeyword())) {
            return message;
        }
        return queryAnalysis.getSearchKeyword();
    }

    private List<Double> buildQueryVector(String keyword) {
        Exception lastException = null;
        int maxAttempts = Math.max(1, aiProperties.getEmbedding().getQueryMaxAttempts());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                List<Double> output = ollamaClient.embed(keyword);
                if (output == null || output.isEmpty()) {
                    throw new BusinessException("AI 向量模型没有返回结果");
                }
                Integer expectedDimension = aiProperties.getRag().getEmbeddingDimension();
                if (expectedDimension != null && output.size() != expectedDimension) {
                    log.warn("查询向量维度和字幕索引不一致, expected={}, actual={}", expectedDimension, output.size());
                }
                return output;
            } catch (BusinessException e) {
                lastException = e;
                if (attempt >= maxAttempts) {
                    break;
                }
                log.warn("生成查询向量失败，准备重试, keyword={}, attempt={}", keyword, attempt, e);
                sleepQuietly(aiProperties.getEmbedding().getRetryIntervalMs());
            } catch (Exception e) {
                lastException = e;
                if (attempt >= maxAttempts) {
                    break;
                }
                // Ollama 模型刚被调度起来时偶发 runner 退出，短暂等待后重试通常能恢复。
                log.warn("生成查询向量失败，准备重试, keyword={}, attempt={}", keyword, attempt, e);
                sleepQuietly(aiProperties.getEmbedding().getRetryIntervalMs());
            }
        }
        /*
         * 本地 Ollama 资源不足时，embedding 模型可能因为 CUDA_Host buffer 分配失败直接退出。
         * 这类问题不应该把整条 SSE 对话打断；后续 ES 检索会在 queryVector 为空时自动走字幕/标题关键词兜底。
         */
        log.warn("生成查询向量失败，降级为关键词检索, keyword={}", keyword, lastException);
        return Collections.emptyList();
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildAiAnswer(String message,
                                 AiConversationContextDTO previousContext,
                                 AiSuggestionActionVO sourceSuggestion,
                                 AiQueryAnalysisVO queryAnalysis,
                                 List<AiMatchedVideoVO> matchedVideos,
                                 Consumer<String> deltaConsumer) {
        String prompt = buildPrompt(message, previousContext, sourceSuggestion, queryAnalysis, matchedVideos);
        if (deltaConsumer != null) {
            return aiChatModelClient.streamChat(aiProperties.getChat().getSystemPrompt(), prompt, deltaConsumer);
        }
        return aiChatModelClient.chat(aiProperties.getChat().getSystemPrompt(), prompt);
    }

    private AiSuggestionActionVO findSourceSuggestion(AiConversationContextDTO previousContext, String sourceSuggestionId) {
        if (previousContext == null || StringUtils.isBlank(sourceSuggestionId)
                || previousContext.getSuggestionActions() == null) {
            return null;
        }
        for (AiSuggestionActionVO suggestionAction : previousContext.getSuggestionActions()) {
            if (suggestionAction != null && sourceSuggestionId.equals(suggestionAction.getSuggestionId())) {
                return suggestionAction;
            }
        }
        return null;
    }

    private List<AiMatchedVideoVO> findContextMatches(AiConversationContextDTO previousContext, AiSuggestionActionVO sourceSuggestion) {
        if (previousContext == null || previousContext.getVideos() == null || previousContext.getVideos().isEmpty()
                || sourceSuggestion == null) {
            return Collections.emptyList();
        }
        List<AiMatchedVideoVO> contextMatches = new ArrayList<>();
        String sourceVideoId = sourceSuggestion.getSourceVideoId();
        if (StringUtils.isBlank(sourceVideoId)) {
            /*
             * “继续了解”没有绑定单个视频，保留上一轮全部命中片段即可。
             * 这里不扩展到历史多轮，避免把上下文越滚越大。
             */
            contextMatches.addAll(previousContext.getVideos());
            return contextMatches;
        }
        for (AiMatchedVideoVO video : previousContext.getVideos()) {
            if (video != null && sourceVideoId.equals(video.getVideoId())) {
                contextMatches.add(video);
            }
        }
        return contextMatches;
    }

    private List<AiMatchedVideoVO> mergeVideoMatches(List<AiMatchedVideoVO> contextVideos,
                                                     List<AiMatchedVideoVO> searchedVideos,
                                                     int topK) {
        LinkedHashMap<String, AiMatchedVideoVO> merged = new LinkedHashMap<>();
        addVideoMatches(merged, contextVideos);
        addVideoMatches(merged, searchedVideos);
        List<AiMatchedVideoVO> result = new ArrayList<>(merged.values());
        if (result.size() <= topK) {
            return result;
        }
        return new ArrayList<>(result.subList(0, topK));
    }

    private void addVideoMatches(Map<String, AiMatchedVideoVO> target, List<AiMatchedVideoVO> videos) {
        if (videos == null || videos.isEmpty()) {
            return;
        }
        for (AiMatchedVideoVO video : videos) {
            if (video == null || StringUtils.isBlank(video.getVideoId())) {
                continue;
            }
            String key = video.getVideoId() + ":" + StringUtils.defaultString(video.getMatchType()) + ":"
                    + StringUtils.defaultString(video.getMatchedText());
            target.putIfAbsent(key, video);
        }
    }

    private List<AiMatchedVideoVO> filterSubtitleMatches(List<AiMatchedVideoVO> matchedVideos) {
        if (matchedVideos == null || matchedVideos.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiMatchedVideoVO> subtitleMatches = new ArrayList<>();
        for (AiMatchedVideoVO matchedVideo : matchedVideos) {
            if (!AiConstants.MATCH_TYPE_TITLE.equals(matchedVideo.getMatchType())) {
                subtitleMatches.add(matchedVideo);
            }
        }
        return subtitleMatches;
    }

    private List<AiMatchedVideoVO> filterTitleMatches(List<AiMatchedVideoVO> matchedVideos) {
        if (matchedVideos == null || matchedVideos.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiMatchedVideoVO> titleMatches = new ArrayList<>();
        for (AiMatchedVideoVO matchedVideo : matchedVideos) {
            if (matchedVideo != null && AiConstants.MATCH_TYPE_TITLE.equals(matchedVideo.getMatchType())) {
                titleMatches.add(matchedVideo);
            }
        }
        return titleMatches;
    }

    private boolean isShortKeyword(String keyword) {
        return StringUtils.length(StringUtils.trimToEmpty(keyword)) <= aiProperties.getChat().getShortKeywordLength();
    }

    private boolean hasSubtitleKeywordHit(List<AiMatchedVideoVO> subtitleMatches, String keyword) {
        if (subtitleMatches == null || subtitleMatches.isEmpty() || StringUtils.isBlank(keyword)) {
            return false;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        for (AiMatchedVideoVO matchedVideo : subtitleMatches) {
            String matchedText = matchedVideo == null ? null : matchedVideo.getMatchedText();
            if (StringUtils.contains(StringUtils.lowerCase(matchedText, Locale.ROOT), lowerKeyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildPrompt(String message,
                               AiConversationContextDTO previousContext,
                               AiSuggestionActionVO sourceSuggestion,
                               AiQueryAnalysisVO queryAnalysis,
                               List<AiMatchedVideoVO> matchedVideos) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < matchedVideos.size(); i++) {
            AiMatchedVideoVO video = matchedVideos.get(i);
            context.append("【视频").append(i + 1).append("】")
                    .append(video.getVideoName()).append("\n")
                    .append("命中来源：").append(StringUtils.defaultIfBlank(video.getMatchSource(), "unknown")).append("\n")
                    .append("命中字幕：").append(video.getMatchedText()).append("\n")
                    .append("时间：").append(formatTime(video.getStartTime())).append(" - ")
                    .append(formatTime(video.getEndTime())).append("\n\n");
        }

        StringBuilder prompt = new StringBuilder();
        if (previousContext != null && StringUtils.isNotBlank(previousContext.getLastQuestion())) {
            prompt.append("上一轮问题：").append(previousContext.getLastQuestion()).append("\n");
            if (StringUtils.isNotBlank(previousContext.getLastAnswer())) {
                prompt.append("上一轮回答摘要：").append(previousContext.getLastAnswer()).append("\n");
            }
            if (previousContext.getQueryAnalysis() != null
                    && StringUtils.isNotBlank(previousContext.getQueryAnalysis().getSearchKeyword())) {
                prompt.append("上一轮检索词：").append(previousContext.getQueryAnalysis().getSearchKeyword()).append("\n");
            }
            if (sourceSuggestion != null) {
                prompt.append("本轮来自推荐问题：").append(sourceSuggestion.getText()).append("\n");
            }
            prompt.append("\n");
        }
        prompt.append("用户本轮问题：").append(message).append("\n");
        if (queryAnalysis != null) {
            prompt.append("问题意图：").append(StringUtils.defaultIfBlank(queryAnalysis.getIntentType(), AiConstants.INTENT_TYPE_UNKNOWN)).append("\n")
                    .append("检索词：").append(StringUtils.defaultIfBlank(queryAnalysis.getSearchKeyword(), message)).append("\n")
                    .append("理解说明：").append(StringUtils.defaultIfBlank(queryAnalysis.getExplanation(), "无")).append("\n");
        }
        prompt.append("\n可参考的视频字幕片段：\n").append(context).append("\n")
                .append("请只基于这些字幕片段回答，不要根据视频标题补充片段里没有的信息。")
                .append("先说明根据片段能够确认的内容，再补充推荐看哪几个视频、为什么值得看。")
                .append("回答控制在").append(aiProperties.getChat().getAnswerMaxWords())
                .append("字以内，语气自然，适合直接返回给用户。");
        return prompt.toString();
    }

    private String buildClarificationAnswer(AiQueryAnalysisVO queryAnalysis) {
        if (queryAnalysis == null || StringUtils.isBlank(queryAnalysis.getClarificationQuestion())) {
            return "你想找哪一类视频？可以告诉我片名、类型、剧情关键词，或者你记得的一个片段。";
        }
        return queryAnalysis.getClarificationQuestion();
    }

    private String buildNoMatchAnswer(String keyword, AiQueryAnalysisVO queryAnalysis) {
        StringBuilder answer = new StringBuilder();
        answer.append("暂时没有找到和“").append(keyword).append("”高度匹配的视频内容。");
        if (queryAnalysis != null && StringUtils.isNotBlank(queryAnalysis.getSearchKeyword())
                && !StringUtils.equals(keyword, queryAnalysis.getSearchKeyword())) {
            answer.append("这次我是按“").append(queryAnalysis.getSearchKeyword()).append("”去检索的。");
        }
        answer.append("你可以换一个更具体的关键词，比如片名、演员、剧情片段、技术名词或业务场景。");
        return answer.toString();
    }

    private String buildTitleMatchAnswer(String keyword, AiQueryAnalysisVO queryAnalysis, List<AiMatchedVideoVO> matchedVideos) {
        StringBuilder answer = new StringBuilder();
        answer.append("没有找到和“").append(keyword).append("”直接匹配的字幕片段，");
        if (queryAnalysis != null && StringUtils.isNotBlank(queryAnalysis.getSearchKeyword())
                && !StringUtils.equals(keyword, queryAnalysis.getSearchKeyword())) {
            answer.append("我按“").append(queryAnalysis.getSearchKeyword()).append("”做了一次更聚焦的检索，");
        }
        answer.append("但找到了标题相关的视频：");
        for (int i = 0; i < matchedVideos.size(); i++) {
            if (i > 0) {
                answer.append("、");
            }
            answer.append("《").append(matchedVideos.get(i).getVideoName()).append("》");
        }
        answer.append("。这些结果只按标题相关展示，没有当作字幕命中喂给 AI。");
        return answer.toString();
    }

    private List<AiSuggestionActionVO> buildClarificationSuggestionActions(AiQueryAnalysisVO queryAnalysis) {
        List<AiSuggestionActionVO> suggestions = new ArrayList<>(3);
        suggestions.add(buildSuggestionAction("告诉我片名或主角", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        suggestions.add(buildSuggestionAction("描述一下剧情片段", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        if (queryAnalysis != null && StringUtils.isNotBlank(queryAnalysis.getExplanation())) {
            suggestions.add(buildSuggestionAction("换一个更具体的说法继续问", AiConstants.SUGGESTION_TYPE_MORE, null, null));
        }
        return suggestions;
    }

    private List<AiSuggestionActionVO> buildNoMatchSuggestionActions(String keyword) {
        List<AiSuggestionActionVO> suggestions = new ArrayList<>(3);
        suggestions.add(buildSuggestionAction("换一个更具体的关键词重新查询", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        suggestions.add(buildSuggestionAction("查询和“" + keyword + "”相关的视频", AiConstants.SUGGESTION_TYPE_MORE, null, null));
        suggestions.add(buildSuggestionAction("按分类或热门视频先筛选内容", AiConstants.SUGGESTION_TYPE_MORE, null, null));
        return suggestions;
    }

    private List<AiSuggestionActionVO> buildFollowUpSuggestionActions(String keyword, List<AiMatchedVideoVO> matchedVideos) {
        if (matchedVideos == null || matchedVideos.isEmpty()) {
            return Collections.emptyList();
        }
        List<AiSuggestionActionVO> suggestions = new ArrayList<>(3);
        AiMatchedVideoVO firstVideo = matchedVideos.get(0);
        suggestions.add(buildSuggestionAction("继续了解“" + keyword + "”的实现细节", AiConstants.SUGGESTION_TYPE_CONTINUE, null, null));
        suggestions.add(buildSuggestionAction(
                "查看《" + firstVideo.getVideoName() + "》里的相关片段",
                AiConstants.SUGGESTION_TYPE_VIDEO,
                firstVideo.getVideoId(),
                firstVideo.getMatchType()
        ));
        suggestions.add(buildSuggestionAction("查询这个主题下更多相似视频", AiConstants.SUGGESTION_TYPE_MORE, null, null));
        return suggestions;
    }

    private AiSuggestionActionVO buildSuggestionAction(String text, String type, String sourceVideoId, String sourceMatchType) {
        AiSuggestionActionVO action = new AiSuggestionActionVO();
        action.setSuggestionId(AiConstants.SUGGESTION_ID_PREFIX + UUID.randomUUID().toString().replace("-", ""));
        action.setText(text);
        action.setType(type);
        action.setSourceVideoId(sourceVideoId);
        action.setSourceMatchType(sourceMatchType);
        return action;
    }

    private void fillResult(AiChatResultVO resultVO,
                            String message,
                            String answer,
                            AiQueryAnalysisVO queryAnalysis,
                            List<AiMatchedVideoVO> matchedVideos,
                            List<AiSuggestionActionVO> suggestionActions) {
        resultVO.setAnswer(answer);
        resultVO.setQueryAnalysis(queryAnalysis);
        resultVO.setSuggestionActions(suggestionActions);
        resultVO.setSuggestions(toSuggestionTexts(suggestionActions));
        resultVO.setContext(buildContext(message, answer, queryAnalysis, matchedVideos, suggestionActions));
    }

    private List<String> toSuggestionTexts(List<AiSuggestionActionVO> suggestionActions) {
        if (suggestionActions == null || suggestionActions.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> suggestions = new ArrayList<>(suggestionActions.size());
        for (AiSuggestionActionVO action : suggestionActions) {
            if (action != null) {
                suggestions.add(action.getText());
            }
        }
        return suggestions;
    }

    private AiConversationContextDTO buildContext(String message,
                                                  String answer,
                                                  AiQueryAnalysisVO queryAnalysis,
                                                  List<AiMatchedVideoVO> matchedVideos,
                                                  List<AiSuggestionActionVO> suggestionActions) {
        AiConversationContextDTO context = new AiConversationContextDTO();
        context.setLastQuestion(message);
        context.setLastAnswer(answer);
        context.setQueryAnalysis(queryAnalysis);
        context.setVideos(matchedVideos);
        context.setSuggestionActions(suggestionActions);
        return context;
    }

    private void sendFixedAnswer(Consumer<String> deltaConsumer, String answer) {
        if (deltaConsumer == null) {
            return;
        }
        String safeAnswer = StringUtils.defaultString(answer);
        if (safeAnswer.isEmpty()) {
            deltaConsumer.accept("");
            return;
        }
        int chunkSize = aiProperties.getChat().getFixedAnswerChunkSize();
        for (int start = 0; start < safeAnswer.length(); start += chunkSize) {
            int end = Math.min(start + chunkSize, safeAnswer.length());
            deltaConsumer.accept(safeAnswer.substring(start, end));
        }
    }

    private void sendDelta(SseEmitter emitter, String delta) {
        try {
            sendEvent(emitter, AiConstants.SSE_EVENT_DELTA, delta);
        } catch (IOException e) {
            throw new BusinessException("AI SSE 输出失败", e);
        }
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) throws IOException {
        emitter.send(SseEmitter.event().name(name).data(data));
    }

    private String getClientErrorMessage(Exception e) {
        if (e instanceof BusinessException) {
            return e.getMessage();
        }
        if (e.getCause() instanceof BusinessException) {
            return e.getCause().getMessage();
        }
        return "AI 问答暂时不可用，请稍后再试";
    }

    private int normalizeTopK(Integer topK) {
        int defaultTopK = aiProperties.getRag().getDefaultTopK();
        int maxTopK = aiProperties.getRag().getMaxTopK();
        if (topK == null || topK <= 0) {
            return defaultTopK;
        }
        return Math.min(topK, maxTopK);
    }

    private String formatTime(Double seconds) {
        if (seconds == null) {
            return "0.0s";
        }
        return seconds + "s";
    }
}
