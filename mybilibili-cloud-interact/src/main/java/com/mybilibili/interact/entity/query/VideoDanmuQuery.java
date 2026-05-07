package com.mybilibili.interact.entity.query;

import com.mybilibili.base.entity.query.BaseQuery;

import java.util.Date;

/**
 * 弹幕查询条件。
 */
public class VideoDanmuQuery extends BaseQuery {

    private Integer danmuId;
    private String videoId;
    private String videoIdFuzzy;
    private String fileId;
    private String fileIdFuzzy;
    private String userId;
    private String userIdFuzzy;
    private Date postTime;
    private String postTimeStart;
    private String postTimeEnd;
    private String text;
    private String textFuzzy;
    private Integer mode;
    private String color;
    private String colorFuzzy;
    private Integer time;
    private String videoUserId;
    private Boolean isQueryUserInfo;

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

    public String getVideoIdFuzzy() {
        return videoIdFuzzy;
    }

    public void setVideoIdFuzzy(String videoIdFuzzy) {
        this.videoIdFuzzy = videoIdFuzzy;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileIdFuzzy() {
        return fileIdFuzzy;
    }

    public void setFileIdFuzzy(String fileIdFuzzy) {
        this.fileIdFuzzy = fileIdFuzzy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdFuzzy() {
        return userIdFuzzy;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public String getPostTimeStart() {
        return postTimeStart;
    }

    public void setPostTimeStart(String postTimeStart) {
        this.postTimeStart = postTimeStart;
    }

    public String getPostTimeEnd() {
        return postTimeEnd;
    }

    public void setPostTimeEnd(String postTimeEnd) {
        this.postTimeEnd = postTimeEnd;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTextFuzzy() {
        return textFuzzy;
    }

    public void setTextFuzzy(String textFuzzy) {
        this.textFuzzy = textFuzzy;
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

    public String getColorFuzzy() {
        return colorFuzzy;
    }

    public void setColorFuzzy(String colorFuzzy) {
        this.colorFuzzy = colorFuzzy;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getVideoUserId() {
        return videoUserId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public Boolean getQueryUserInfo() {
        return isQueryUserInfo;
    }

    public void setQueryUserInfo(Boolean queryUserInfo) {
        isQueryUserInfo = queryUserInfo;
    }
}
