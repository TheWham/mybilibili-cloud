package com.mybilibili.ai.entity.dto;

import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;
import com.mybilibili.ai.entity.vo.AiQueryAnalysisVO;
import com.mybilibili.ai.entity.vo.AiSuggestionActionVO;

import java.io.Serializable;
import java.util.List;

/**
 * AI 会话中的单条消息。
 *
 * <p>字段刻意贴近前端消息结构，减少前后端恢复会话时的转换成本。</p>
 */
public class AiChatMessageDTO implements Serializable {

    private String id;
    private String role;
    private String text;
    private List<AiMatchedVideoVO> videos;
    private List<String> suggestions;
    private List<AiSuggestionActionVO> suggestionActions;
    private AiQueryAnalysisVO queryAnalysis;
    private Long createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<AiMatchedVideoVO> getVideos() {
        return videos;
    }

    public void setVideos(List<AiMatchedVideoVO> videos) {
        this.videos = videos;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<AiSuggestionActionVO> getSuggestionActions() {
        return suggestionActions;
    }

    public void setSuggestionActions(List<AiSuggestionActionVO> suggestionActions) {
        this.suggestionActions = suggestionActions;
    }

    public AiQueryAnalysisVO getQueryAnalysis() {
        return queryAnalysis;
    }

    public void setQueryAnalysis(AiQueryAnalysisVO queryAnalysis) {
        this.queryAnalysis = queryAnalysis;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
