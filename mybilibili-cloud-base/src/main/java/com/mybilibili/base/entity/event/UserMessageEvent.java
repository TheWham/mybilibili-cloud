package com.mybilibili.base.entity.event;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户站内信事件。
 *
 * <p>消息内容由业务服务按场景组装，message 服务只负责校验、幂等和落库。</p>
 *
 * @author amani
 * @since 2026/05/13
 */
public class UserMessageEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一 id，用来处理 MQ 重投。
     */
    private String eventId;

    /**
     * 接收通知的用户 id。
     */
    private String receiveUserId;

    /**
     * 触发通知的用户 id，系统通知可以为空。
     */
    private String sendUserId;

    /**
     * 关联视频 id。
     */
    private String videoId;

    /**
     * 消息类型，见 MessageTypeEnum。
     */
    private Integer messageType;

    /**
     * 通知创建时间。
     */
    private Date createTime;

    /**
     * 业务扩展信息，前端按消息类型解析。
     */
    private String extendJson;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getExtendJson() {
        return extendJson;
    }

    public void setExtendJson(String extendJson) {
        this.extendJson = extendJson;
    }
}
