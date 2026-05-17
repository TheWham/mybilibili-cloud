package com.mybilibili.ai.entity.vo;

import java.io.Serializable;

/**
 * AI 检索命中的单个字段明细。
 *
 * <p>一个视频可能同时命中字幕、标题、标签。这里把每个命中点拆开保存，前端可以按来源分别展示，
 * 问答链路也能只挑字幕命中作为 RAG 证据，避免把标题、标签误当成字幕内容。</p>
 */
public class AiMatchDetailVO implements Serializable {

    /**
     * 命中类型，例如 subtitle、title、tag。
     */
    private String matchType;
    /**
     * 命中来源，例如 vector、subtitle、title、tag。
     */
    private String matchSource;
    /**
     * 命中的展示文本。
     */
    private String matchedText;
    /**
     * 当前命中明细的归一化分数。
     */
    private Double score;
    /**
     * 字幕命中的开始时间，标题和标签命中为空。
     */
    private Double startTime;
    /**
     * 字幕命中的结束时间，标题和标签命中为空。
     */
    private Double endTime;

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getMatchSource() {
        return matchSource;
    }

    public void setMatchSource(String matchSource) {
        this.matchSource = matchSource;
    }

    public String getMatchedText() {
        return matchedText;
    }

    public void setMatchedText(String matchedText) {
        this.matchedText = matchedText;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
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
}
