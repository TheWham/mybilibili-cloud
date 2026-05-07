package com.mybilibili.interact.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户评论行为。
 */
public class UserCommentAction implements Serializable {

    private Integer actionId;
    private String videoId;
    private String videoUserId;
    private Integer commentId;
    private Integer actionType;
    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actionTime;

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    @Override
    public String toString() {
        return "UserCommentAction{"
                + "actionId='" + actionId
                + ", videoId='" + videoId + '\''
                + ", videoUserId='" + videoUserId + '\''
                + ", commentId='" + commentId + '\''
                + ", actionType='" + actionType + '\''
                + ", userId='" + userId + '\''
                + ", actionTime='" + DateUtils.format(actionTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\''
                + '}';
    }
}
