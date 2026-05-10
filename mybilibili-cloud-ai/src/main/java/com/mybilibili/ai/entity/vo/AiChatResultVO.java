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
    private List<AiMatchedVideoVO> videos;
    private List<String> suggestions;
    private List<AiSuggestionActionVO> suggestionActions;
    private AiConversationContextDTO context;

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
}

