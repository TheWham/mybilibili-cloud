package com.mybilibili.ai.entity.dto;

import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;
import com.mybilibili.ai.entity.vo.AiQueryAnalysisVO;
import com.mybilibili.ai.entity.vo.AiSuggestionActionVO;

import java.io.Serializable;
import java.util.List;

/**
 * 前端临时持有的 AI 会话上下文。
 *
 * <p>第二版开始后端会把最近一轮上下文写入 Redis，但这里仍然保留给前端透传，
 * 兼容旧页面在未命中 Redis 会话时回退使用。</p>
 */
public class AiConversationContextDTO implements Serializable {

    private String lastQuestion;
    private String lastAnswer;
    /**
     * 上一轮问题分析结果。
     *
     * <p>下一轮如果是“继续了解”“查看相关片段”这类追问，可以优先复用这里的检索语义，
     * 避免每轮都从零分析。</p>
     */
    private AiQueryAnalysisVO queryAnalysis;
    private List<AiMatchedVideoVO> videos;
    private List<AiSuggestionActionVO> suggestionActions;

    public String getLastQuestion() {
        return lastQuestion;
    }

    public void setLastQuestion(String lastQuestion) {
        this.lastQuestion = lastQuestion;
    }

    public String getLastAnswer() {
        return lastAnswer;
    }

    public void setLastAnswer(String lastAnswer) {
        this.lastAnswer = lastAnswer;
    }

    public AiQueryAnalysisVO getQueryAnalysis() {
        return queryAnalysis;
    }

    public void setQueryAnalysis(AiQueryAnalysisVO queryAnalysis) {
        this.queryAnalysis = queryAnalysis;
    }

    public List<AiMatchedVideoVO> getVideos() {
        return videos;
    }

    public void setVideos(List<AiMatchedVideoVO> videos) {
        this.videos = videos;
    }

    public List<AiSuggestionActionVO> getSuggestionActions() {
        return suggestionActions;
    }

    public void setSuggestionActions(List<AiSuggestionActionVO> suggestionActions) {
        this.suggestionActions = suggestionActions;
    }
}

