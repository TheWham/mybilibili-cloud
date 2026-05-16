package com.mybilibili.ai.service.impl;

import com.mybilibili.ai.client.AiChatModelClient;
import com.mybilibili.ai.client.OllamaClient;
import com.mybilibili.ai.config.AiProperties;
import com.mybilibili.ai.constants.AiConstants;
import com.mybilibili.ai.entity.dto.AiChatRequestDTO;
import com.mybilibili.ai.entity.dto.AiConversationContextDTO;
import com.mybilibili.ai.entity.vo.AiChatResultVO;
import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;
import com.mybilibili.ai.entity.vo.AiSuggestionActionVO;
import com.mybilibili.ai.service.AiChatService;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Service("aiChatService")
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    @Resource
    private OllamaClient ollamaClient;
    @Resource
    private AiChatModelClient aiChatModelClient;
    @Resource
    private AiSubtitleVectorService aiSubtitleVectorService;
    @Resource
    private AiProperties aiProperties;
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

    private AiChatResultVO buildChatResult(AiChatRequestDTO request, Consumer<String> deltaConsumer) {
        AiChatRequestDTO safeRequest = request == null ? new AiChatRequestDTO() : request;
        String message = resolveMessage(safeRequest);
        String conversationId = resolveConversationId(safeRequest);
        AiConversationContextDTO previousContext = safeRequest.getContext();
        AiSuggestionActionVO sourceSuggestion = findSourceSuggestion(previousContext, safeRequest.getSourceSuggestionId());
        String searchKeyword = resolveSearchKeyword(message, previousContext, sourceSuggestion);
        int topK = normalizeTopK(safeRequest.getTopK());
        long startTime = System.currentTimeMillis();

        List<Double> queryVector = buildQueryVector(searchKeyword);
        long embeddingEndTime = System.currentTimeMillis();
        List<AiMatchedVideoVO> searchedVideos = aiSubtitleVectorService.search(
                searchKeyword,
                queryVector,
                topK,
                aiProperties.getRag().getMinScore()
        );
        long searchEndTime = System.currentTimeMillis();

        /*
         * 推荐问题不是普通关键词。比如“查看《xxx》里的相关片段”这类按钮文案，
         * 真正含义是沿着上一轮结果继续看某个视频，所以这里先把上一轮命中的片段放回上下文。
         */
        List<AiMatchedVideoVO> contextVideos = findContextMatches(previousContext, sourceSuggestion);
        List<AiMatchedVideoVO> matchedVideos = mergeVideoMatches(contextVideos, searchedVideos, topK);
        AiChatResultVO resultVO = new AiChatResultVO();
        resultVO.setConversationId(conversationId);
        resultVO.setVideos(matchedVideos);

        if (matchedVideos.isEmpty()) {
            String answer = buildNoMatchAnswer(message);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, matchedVideos, buildNoMatchSuggestionActions(message));
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
             * ai 这类短词容易被向量召回带偏。没有字幕文本直接包含关键词时，
             * 优先按标题相关展示，别把弱相关字幕拿去让模型组织答案。
             */
            resultVO.setVideos(titleMatches);
            String answer = buildTitleMatchAnswer(message, titleMatches);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, titleMatches, buildFollowUpSuggestionActions(message, titleMatches));
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
            String answer = buildTitleMatchAnswer(message, matchedVideos);
            sendFixedAnswer(deltaConsumer, answer);
            fillResult(resultVO, message, answer, matchedVideos, buildFollowUpSuggestionActions(message, matchedVideos));
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

        String answer = buildAiAnswer(message, previousContext, sourceSuggestion, subtitleMatches, deltaConsumer);
        long answerEndTime = System.currentTimeMillis();
        fillResult(resultVO, message, answer, matchedVideos, buildFollowUpSuggestionActions(message, matchedVideos));
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

    private String resolveSearchKeyword(String message, AiConversationContextDTO previousContext, AiSuggestionActionVO sourceSuggestion) {
        if (sourceSuggestion != null && previousContext != null && StringUtils.isNotBlank(previousContext.getLastQuestion())) {
            return previousContext.getLastQuestion();
        }
        return message;
    }

    private List<Double> buildQueryVector(String keyword) {
        Exception lastException = null;
        int maxAttempts = aiProperties.getEmbedding().getQueryMaxAttempts();
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
        log.error("生成查询向量失败, keyword={}", keyword, lastException);
        throw new BusinessException("AI 向量模型暂时不可用，请稍后再试", lastException);
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
                                 List<AiMatchedVideoVO> matchedVideos,
                                 Consumer<String> deltaConsumer) {
        String prompt = buildPrompt(message, previousContext, sourceSuggestion, matchedVideos);
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
                               List<AiMatchedVideoVO> matchedVideos) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < matchedVideos.size(); i++) {
            AiMatchedVideoVO video = matchedVideos.get(i);
            context.append("【视频").append(i + 1).append("】")
                    .append(video.getVideoName()).append("\n")
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
            if (sourceSuggestion != null) {
                prompt.append("本轮来自推荐问题：").append(sourceSuggestion.getText()).append("\n");
            }
            prompt.append("\n");
        }
        prompt.append("用户本轮问题：").append(message).append("\n\n")
                .append("可参考的视频字幕片段：\n").append(context).append("\n")
                .append("请只基于这些字幕片段回答，不要根据视频标题补充信息。")
                .append("回答控制在").append(aiProperties.getChat().getAnswerMaxWords())
                .append("字以内，先说明片段里能确认的内容，再给出可观看的视频。");
        return prompt.toString();
    }

    private String buildNoMatchAnswer(String keyword) {
        return "暂时没有找到和“" + keyword + "”高度匹配的视频内容。你可以换一个更具体的关键词，"
                + "比如技术名词、业务场景、功能名称，或者先查询相关视频列表再继续提问。";
    }

    private String buildTitleMatchAnswer(String keyword, List<AiMatchedVideoVO> matchedVideos) {
        StringBuilder answer = new StringBuilder();
        answer.append("没有找到和“").append(keyword).append("”直接匹配的字幕片段，")
                .append("但找到了标题相关的视频：");
        for (int i = 0; i < matchedVideos.size(); i++) {
            if (i > 0) {
                answer.append("、");
            }
            answer.append("《").append(matchedVideos.get(i).getVideoName()).append("》");
        }
        answer.append("。这些结果只按标题相关展示，没有当作字幕命中喂给 AI。");
        return answer.toString();
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
                            List<AiMatchedVideoVO> matchedVideos,
                            List<AiSuggestionActionVO> suggestionActions) {
        resultVO.setAnswer(answer);
        resultVO.setSuggestionActions(suggestionActions);
        resultVO.setSuggestions(toSuggestionTexts(suggestionActions));
        resultVO.setContext(buildContext(message, answer, matchedVideos, suggestionActions));
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
                                                  List<AiMatchedVideoVO> matchedVideos,
                                                  List<AiSuggestionActionVO> suggestionActions) {
        AiConversationContextDTO context = new AiConversationContextDTO();
        context.setLastQuestion(message);
        context.setLastAnswer(answer);
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
