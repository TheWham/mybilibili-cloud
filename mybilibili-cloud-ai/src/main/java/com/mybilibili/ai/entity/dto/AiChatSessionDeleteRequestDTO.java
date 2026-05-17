package com.mybilibili.ai.entity.dto;

import java.io.Serializable;

/**
 * 删除 AI 会话的请求。
 *
 * <p>只接收会话编号，用户归属由后端根据 token 校验，前端不能传 userId。</p>
 */
public class AiChatSessionDeleteRequestDTO implements Serializable {

    /**
     * 要删除的会话编号。
     */
    private String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
