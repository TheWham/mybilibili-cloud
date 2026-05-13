package com.mybilibili.base.entity.event;

import java.io.Serializable;
import java.util.Date;

/**
 * 弹幕发送事件。
 *
 * <p>发送接口只负责校验和投递事件，真正的弹幕正文落库由 interact 消费端批量处理。</p>
 */
public class VideoDanmuPostEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一 id，用来处理 MQ 重投和并发重复消费。
     */
    private String eventId;

    private String videoId;

    private String videoUserId;

    private String fileId;

    private String userId;

    private Date postTime;

    private String text;

    private Integer mode;

    private String color;

    private Integer time;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }
}
