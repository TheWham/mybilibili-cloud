package com.mybilibili.ai.entity.dto;

import com.mybilibili.ai.entity.vo.AiMatchedVideoVO;
import com.mybilibili.ai.entity.vo.AiSuggestionActionVO;

import java.io.Serializable;
import java.util.List;

/**
 * 前端临时持有的 AI 会话上下文。
 * 当前版本不把会话落库，也不写 Redis；页面刷新后上下文丢失是可接受的。
 */
public class AiConversationContextDTO implements Serializable {

    private String lastQuestion;
    private String lastAnswer;
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

