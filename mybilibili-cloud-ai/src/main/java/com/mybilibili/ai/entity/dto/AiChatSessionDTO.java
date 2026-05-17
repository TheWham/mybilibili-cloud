package com.mybilibili.ai.entity.dto;

import java.io.Serializable;
import java.util.List;

/**
 * AI 对话会话快照。
 *
 * <p>第二版会把完整消息列表和最近一轮上下文一起落到 Redis，
 * 这样页面刷新后可以恢复完整对话，而不是只恢复最后一轮问答状态。</p>
 */
public class AiChatSessionDTO implements Serializable {

    /**
     * 会话编号，和前端的 conversationId 一一对应。
     */
    private String conversationId;
    /**
     * 当前会话所属用户。
     */
    private String userId;
    /**
     * 会话标题，默认取首条用户问题的摘要。
     */
    private String title;
    /**
     * 最近一轮问答上下文。
     */
    private AiConversationContextDTO context;
    /**
     * 完整消息时间线。
     */
    private List<AiChatMessageDTO> messages;
    /**
     * 最近一轮用户问题。
     */
    private String lastQuestion;
    /**
     * 最近一轮 AI 回答。
     */
    private String lastAnswer;
    /**
     * 最近更新时间，便于排序和排查问题。
     */
    private Long updateTime;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AiConversationContextDTO getContext() {
        return context;
    }

    public void setContext(AiConversationContextDTO context) {
        this.context = context;
    }

    public List<AiChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<AiChatMessageDTO> messages) {
        this.messages = messages;
    }

    public String getLastQuestion() {
        return lastQuestion;
    }

    public void setLastQuestion(String lastQuestion) {
        this.lastQuestion = lastQuestion;
    }

    public String getLastAnswer() {
        return lastAnswer;
    }

    public void setLastAnswer(String lastAnswer) {
        this.lastAnswer = lastAnswer;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
