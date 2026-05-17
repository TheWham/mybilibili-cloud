package com.mybilibili.ai.entity.dto;

import java.io.Serializable;

/**
 * 查询 AI 会话详情的请求。
 */
public class AiChatSessionDetailRequestDTO implements Serializable {

    /**
     * 要恢复的会话编号。
     */
    private String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
