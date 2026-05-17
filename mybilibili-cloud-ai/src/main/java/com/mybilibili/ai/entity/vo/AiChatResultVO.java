package com.mybilibili.ai.entity.vo;

import com.mybilibili.ai.entity.dto.AiConversationContextDTO;

import java.io.Serializable;
import java.util.List;

/**
 * AI 问答返回结果。
 */
public class AiChatResultVO implements Serializable {

    private String conversationId;
    private String answer;
    /**
     * 当前轮提问分析结果。
     *
     * <p>前端可以据此展示本轮问题的意图、改写后的检索词，以及是否需要进一步补充条件。</p>
     */
    private AiQueryAnalysisVO queryAnalysis;
    private List<AiMatchedVideoVO> videos;
    private List<String> suggestions;
    private List<AiSuggestionActionVO> suggestionActions;
    private AiConversationContextDTO context;
    /**
     * 当前会话摘要。
     *
     * <p>登录用户完成一轮问答后，前端用它直接刷新左侧会话列表。
     * 匿名用户不会持久化会话，因此该字段通常为空。</p>
     */
    private AiChatSessionSummaryVO sessionSummary;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public AiQueryAnalysisVO getQueryAnalysis() {
        return queryAnalysis;
    }

    public void setQueryAnalysis(AiQueryAnalysisVO queryAnalysis) {
        this.queryAnalysis = queryAnalysis;
    }

    public List<AiMatchedVideoVO> getVideos() {
        return videos;
    }

    public void setVideos(List<AiMatchedVideoVO> videos) {
        this.videos = videos;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<AiSuggestionActionVO> getSuggestionActions() {
        return suggestionActions;
    }

    public void setSuggestionActions(List<AiSuggestionActionVO> suggestionActions) {
        this.suggestionActions = suggestionActions;
    }

    public AiConversationContextDTO getContext() {
        return context;
    }

    public void setContext(AiConversationContextDTO context) {
        this.context = context;
    }

    public AiChatSessionSummaryVO getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(AiChatSessionSummaryVO sessionSummary) {
        this.sessionSummary = sessionSummary;
    }
}

