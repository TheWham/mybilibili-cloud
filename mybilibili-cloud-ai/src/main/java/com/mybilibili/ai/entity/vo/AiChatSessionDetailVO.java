package com.mybilibili.ai.entity.vo;

import com.mybilibili.ai.entity.dto.AiChatMessageDTO;
import com.mybilibili.ai.entity.dto.AiConversationContextDTO;

import java.io.Serializable;
import java.util.List;

/**
 * AI 会话详情。
 */
public class AiChatSessionDetailVO implements Serializable {

    private String conversationId;
    private String title;
    private List<AiChatMessageDTO> messages;
    private AiConversationContextDTO context;
    private Long updateTime;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<AiChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<AiChatMessageDTO> messages) {
        this.messages = messages;
    }

    public AiConversationContextDTO getContext() {
        return context;
    }

    public void setContext(AiConversationContextDTO context) {
        this.context = context;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
