package com.mybilibili.ai.entity.vo;

import java.io.Serializable;

/**
 * 可追问的推荐问题。
 * suggestionId 会在下一轮请求中带回，后端据此判断这次追问来自哪个视频或片段。
 */
public class AiSuggestionActionVO implements Serializable {

    private String suggestionId;
    private String text;
    private String type;
    private String sourceVideoId;
    private String sourceMatchType;

    public String getSuggestionId() {
        return suggestionId;
    }

    public void setSuggestionId(String suggestionId) {
        this.suggestionId = suggestionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceVideoId() {
        return sourceVideoId;
    }

    public void setSourceVideoId(String sourceVideoId) {
        this.sourceVideoId = sourceVideoId;
    }

    public String getSourceMatchType() {
        return sourceMatchType;
    }

    public void setSourceMatchType(String sourceMatchType) {
        this.sourceMatchType = sourceMatchType;
    }
}

