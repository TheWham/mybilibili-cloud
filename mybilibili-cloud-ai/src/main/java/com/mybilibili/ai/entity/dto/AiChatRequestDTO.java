package com.mybilibili.ai.entity.dto;

import java.io.Serializable;

/**
 * 前端 AI 问答请求。
 */
public class AiChatRequestDTO implements Serializable {

    private String keyword;
    private String message;
    private String conversationId;
    private String sourceSuggestionId;
    private AiConversationContextDTO context;

    /**
     * 最多返回几个相关视频，不传时使用后端默认值。
     */
    private Integer topK;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSourceSuggestionId() {
        return sourceSuggestionId;
    }

    public void setSourceSuggestionId(String sourceSuggestionId) {
        this.sourceSuggestionId = sourceSuggestionId;
    }

    public AiConversationContextDTO getContext() {
        return context;
    }

    public void setContext(AiConversationContextDTO context) {
        this.context = context;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}

