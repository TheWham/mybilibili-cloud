package com.mybilibili.ai.entity.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 检索命中的视频片段。
 */
public class AiMatchedVideoVO implements Serializable {

    private String videoId;
    private String videoName;
    private String videoCover;
    private String matchedText;
    private Double startTime;
    private Double endTime;
    private Double score;
    private String matchType;
    /**
     * 同一个视频下的全部命中明细。
     *
     * <p>旧字段只保留一个主命中，用于兼容前端；新前端展示时优先读取这里。</p>
     */
    private List<AiMatchDetailVO> matchDetails = new ArrayList<>();
    /**
     * 命中来源。
     *
     * <p>用于前端展示本条结果主要来自标题、字幕、向量还是混合召回。</p>
     */
    private String matchSource;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public void setMatchedText(String matchedText) {
        this.matchedText = matchedText;
    }

    public Double getStartTime() {
        return startTime;
    }

    public void setStartTime(Double startTime) {
        this.startTime = startTime;
    }

    public Double getEndTime() {
        return endTime;
    }

    public void setEndTime(Double endTime) {
        this.endTime = endTime;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public List<AiMatchDetailVO> getMatchDetails() {
        return matchDetails;
    }

    public void setMatchDetails(List<AiMatchDetailVO> matchDetails) {
        this.matchDetails = matchDetails == null ? new ArrayList<>() : matchDetails;
    }

    public String getMatchSource() {
        return matchSource;
    }

    public void setMatchSource(String matchSource) {
        this.matchSource = matchSource;
    }
}

