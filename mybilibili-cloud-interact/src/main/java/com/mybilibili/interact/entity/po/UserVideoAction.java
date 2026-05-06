package com.mybilibili.interact.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/09
 * 用户行为  点赞,评论
 */
public class UserVideoAction implements Serializable {
    /**
     * @description 自增id
     */
    private Integer actionId;

    /**
     * @description 视频Id
     */
    private String videoId;

    /**
     * @description 视频用户id
     */
    private String videoUserId;

    /**
     * @description 0:评论喜欢点赞 1:讨厌评论 2:视频点赞 3:视频收藏 4:视频投币
     */
    private Integer actionType;

    /**
     * @description 数量
     */
    private Integer actionCount;

    /**
     * @description 用户id
     */
    private String userId;

    /**
     * @description 操作时间
     */
    @JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actionTime;



    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }
    public Integer getActionId() {
        return this.actionId;
    }
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    public String getVideoId() {
        return this.videoId;
    }
    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }
    public String getVideoUserId() {
        return this.videoUserId;
    }
    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }
    public Integer getActionType() {
        return this.actionType;
    }
    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }
    public Integer getActionCount() {
        return this.actionCount;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }
    public Date getActionTime() {
        return this.actionTime;
    }

    @Override
    public String toString() {
        return "UserAction{" +
                "actionId='" + actionId +
                ", videoId='" + videoId + '\'' +
                ", videoUserId='" + videoUserId + '\'' +
                ", actionType='" + actionType + '\'' +
                ", actionCount='" + actionCount + '\'' +
                ", userId='" + userId + '\'' +
                ", actionTime='" + DateUtils.format(actionTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' +
                '}';
    }

}