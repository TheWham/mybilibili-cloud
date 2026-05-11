package com.mybilibili.base.entity.event;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户硬币同步事件。
 *
 * <p>投币和审核奖励都会影响 user_info 中的硬币字段。事件里保留来源语义，
 * 消费端可以按普通投币和平台奖励分别计算扣减、增加逻辑。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class UserCoinSyncEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一 id，用来处理 MQ 重投时的消费幂等。
     */
    private String eventId;

    /**
     * 投币用户 id。审核奖励没有投币用户，可以为空。
     */
    private String userId;

    /**
     * 收到硬币的用户 id。
     */
    private String videoUserId;

    /**
     * 视频 id。
     */
    private String videoId;

    /**
     * 本次硬币数量，必须为正数。
     */
    private Integer actionCount;

    /**
     * true 表示平台审核奖励，false 表示用户投币。
     */
    private Boolean auditReward;

    /**
     * 事件产生时间。
     */
    private Date actionTime;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoUserId() {
        return videoUserId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getActionCount() {
        return actionCount;
    }

    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }

    public Boolean getAuditReward() {
        return auditReward;
    }

    public void setAuditReward(Boolean auditReward) {
        this.auditReward = auditReward;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }
}
