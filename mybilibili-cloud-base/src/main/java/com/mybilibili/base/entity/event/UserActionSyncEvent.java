package com.mybilibili.base.entity.event;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视频互动同步事件。
 *
 * <p>这个对象只描述一次已经通过 Redis 校验的用户行为，真正落库和计数同步交给
 * 对应服务消费。这样请求线程不用直接写多个服务的数据表。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class UserActionSyncEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一 id，用来处理 MQ 重投时的消费幂等。
     */
    private String eventId;

    /**
     * 操作用户 id。
     */
    private String userId;

    /**
     * 视频 id。
     */
    private String videoId;

    /**
     * 视频作者 id。
     */
    private String videoUserId;

    /**
     * 行为类型，见 UserActionTypeEnum。
     */
    private Integer actionType;

    /**
     * 本次计数增量。点赞/收藏取消时为负数，投币时为正数。
     */
    private Integer actionCount;

    /**
     * 当前行为是否生效。取消点赞、取消收藏时为 false。
     */
    private Boolean active;

    /**
     * 操作时间。
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

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoUserId() {
        return videoUserId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public Integer getActionCount() {
        return actionCount;
    }

    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }
}
