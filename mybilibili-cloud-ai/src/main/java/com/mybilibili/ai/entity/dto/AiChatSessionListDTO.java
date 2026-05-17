package com.mybilibili.ai.entity.dto;

import com.mybilibili.ai.entity.vo.AiChatSessionSummaryVO;

import java.io.Serializable;
import java.util.List;

/**
 * 用户 AI 会话索引。
 *
 * <p>单独用一个 Redis key 保存会话摘要列表，避免每次都扫描所有会话详情 key。</p>
 */
public class AiChatSessionListDTO implements Serializable {

    private String userId;
    private List<AiChatSessionSummaryVO> sessions;
    private Long updateTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<AiChatSessionSummaryVO> getSessions() {
        return sessions;
    }

    public void setSessions(List<AiChatSessionSummaryVO> sessions) {
        this.sessions = sessions;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
