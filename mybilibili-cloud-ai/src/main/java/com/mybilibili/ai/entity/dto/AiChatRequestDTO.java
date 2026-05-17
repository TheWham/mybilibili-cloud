package com.mybilibili.ai.entity.dto;

import java.io.Serializable;

/**
 * 前端 AI 问答请求。
 */
public class AiChatRequestDTO implements Serializable {

    private String keyword;
    private String message;
    private String conversationId;
    /**
     * 会话动作。
     *
     * <p>第二版 AI 模式支持 init/chat 两种模式：
     * init 只返回欢迎语和推荐提问；
     * chat 进入真正的问答检索链路。</p>
     */
    private String sessionAction;
    private String sourceSuggestionId;
    private AiConversationContextDTO context;
    /**
     * 当前登录用户。
     *
     * <p>这个字段由后端 controller 注入，前端不需要传，也不作为公开协议字段使用。</p>
     */
    private transient String loginUserId;

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

    public String getSessionAction() {
        return sessionAction;
    }

    public void setSessionAction(String sessionAction) {
        this.sessionAction = sessionAction;
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

    public String getLoginUserId() {
        return loginUserId;
    }

    public void setLoginUserId(String loginUserId) {
        this.loginUserId = loginUserId;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}

