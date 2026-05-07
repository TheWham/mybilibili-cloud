package com.mybilibili.interact.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频弹幕。
 */
public class VideoDanmu implements Serializable {

    private Integer danmuId;
    private String videoId;
    private String fileId;
    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

    private String text;
    private Integer mode;
    private String color;
    private Integer time;
    private String nickName;
    private String videoName;

    public Integer getDanmuId() {
        return danmuId;
    }

    public void setDanmuId(Integer danmuId) {
        this.danmuId = danmuId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    @Override
    public String toString() {
        return "VideoDanmu{"
                + "danmuId='" + danmuId
                + ", videoId='" + videoId + '\''
                + ", fileId='" + fileId + '\''
                + ", userId='" + userId + '\''
                + ", postTime='" + DateUtils.format(postTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\''
                + ", text='" + text + '\''
                + ", mode='" + mode + '\''
                + ", color='" + color + '\''
                + ", time='" + time + '\''
                + '}';
    }
}
