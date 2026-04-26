package com.mybilibili.base.entity.dto;

import java.io.Serializable;
import java.util.Date;

public class UserActionSyncDTO implements Serializable {

    private String userId;
    private String videoId;
    private String videoUserId;
    private Integer commentId;
    private Integer actionType;
    private Integer actionCount;
    private Boolean active;
    private Integer likeDiff;
    private Integer hateDiff;
    private Date actionTime;
    private String statsDay;

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

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
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

    public Integer getLikeDiff() {
        return likeDiff;
    }

    public void setLikeDiff(Integer likeDiff) {
        this.likeDiff = likeDiff;
    }

    public Integer getHateDiff() {
        return hateDiff;
    }

    public void setHateDiff(Integer hateDiff) {
        this.hateDiff = hateDiff;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public String getStatsDay() {
        return statsDay;
    }

    public void setStatsDay(String statsDay) {
        this.statsDay = statsDay;
    }
}
