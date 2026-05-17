package com.mybilibili.ai.entity.vo;

import java.io.Serializable;

/**
 * 用户提问分析结果。
 *
 * <p>这一层结果主要给两个地方使用：一是把自然语言问题改写成更适合做向量检索的关键词，
 * 二是把后端对问题的理解透出给前端，方便页面展示“我理解你想找什么”。</p>
 */
public class AiQueryAnalysisVO implements Serializable {

    /**
     * 用户原始问题，保持前端输入原样，便于页面回显和后续追问。
     */
    private String originalQuestion;

    /**
     * 提问意图类型，例如视频检索、知识问答、继续追问等。
     */
    private String intentType;

    /**
     * 真正送去做向量检索的关键词。
     */
    private String searchKeyword;

    /**
     * 当前信息是否不足，是否需要先向用户追问。
     */
    private Boolean needClarification;

    /**
     * 需要追问时给前端展示的追问文案。
     */
    private String clarificationQuestion;

    /**
     * 模型对本次分析结果的置信度，范围控制在 0 到 1 之间。
     */
    private Double confidence;

    /**
     * 对当前问题理解的一句话说明。
     */
    private String explanation;

    public String getOriginalQuestion() {
        return originalQuestion;
    }

    public void setOriginalQuestion(String originalQuestion) {
        this.originalQuestion = originalQuestion;
    }

    public String getIntentType() {
        return intentType;
    }

    public void setIntentType(String intentType) {
        this.intentType = intentType;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public Boolean getNeedClarification() {
        return needClarification;
    }

    public void setNeedClarification(Boolean needClarification) {
        this.needClarification = needClarification;
    }

    public String getClarificationQuestion() {
        return clarificationQuestion;
    }

    public void setClarificationQuestion(String clarificationQuestion) {
        this.clarificationQuestion = clarificationQuestion;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
