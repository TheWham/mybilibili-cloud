package com.mybilibili.ai.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * AI 服务配置。
 *
 * <p>默认值贴合当前本地开发环境；上线时放到 Nacos 的 mybilibili-cloud-ai-*.yml 覆盖即可。</p>
 */
@Validated
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    @Valid
    private Ollama ollama = new Ollama();
    @Valid
    private Rag rag = new Rag();
    @Valid
    private Chat chat = new Chat();
    @Valid
    private Embedding embedding = new Embedding();
    @Valid
    private Search search = new Search();
    @Valid
    private Warmup warmup = new Warmup();
    @Valid
    private Es es = new Es();

    public Ollama getOllama() {
        return ollama;
    }

    public void setOllama(Ollama ollama) {
        this.ollama = ollama == null ? new Ollama() : ollama;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag == null ? new Rag() : rag;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat == null ? new Chat() : chat;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding == null ? new Embedding() : embedding;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search == null ? new Search() : search;
    }

    public Warmup getWarmup() {
        return warmup;
    }

    public void setWarmup(Warmup warmup) {
        this.warmup = warmup == null ? new Warmup() : warmup;
    }

    public Es getEs() {
        return es;
    }

    public void setEs(Es es) {
        this.es = es == null ? new Es() : es;
    }

    public static class Ollama {

        @NotBlank
        private String baseUrl = "http://127.0.0.1:11434";
        @NotBlank
        private String chatModel = "qwen2.5:3b";
        @NotBlank
        private String embeddingModel = "bge-m3-cpu:567m";
        @NotBlank
        private String keepAlive = "10m";
        @Min(1)
        private Integer connectTimeoutSeconds = 5;
        @Min(1)
        private Integer readTimeoutSeconds = 180;
        @Min(1)
        private Integer writeTimeoutSeconds = 30;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getChatModel() {
            return chatModel;
        }

        public void setChatModel(String chatModel) {
            this.chatModel = chatModel;
        }

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public String getKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(String keepAlive) {
            this.keepAlive = keepAlive;
        }

        public Integer getConnectTimeoutSeconds() {
            return connectTimeoutSeconds;
        }

        public void setConnectTimeoutSeconds(Integer connectTimeoutSeconds) {
            this.connectTimeoutSeconds = connectTimeoutSeconds;
        }

        public Integer getReadTimeoutSeconds() {
            return readTimeoutSeconds;
        }

        public void setReadTimeoutSeconds(Integer readTimeoutSeconds) {
            this.readTimeoutSeconds = readTimeoutSeconds;
        }

        public Integer getWriteTimeoutSeconds() {
            return writeTimeoutSeconds;
        }

        public void setWriteTimeoutSeconds(Integer writeTimeoutSeconds) {
            this.writeTimeoutSeconds = writeTimeoutSeconds;
        }
    }

    public static class Rag {

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private Double minScore = 0.55D;
        @Min(1)
        private Integer defaultTopK = 5;
        @Min(1)
        private Integer maxTopK = 10;
        @Min(1)
        private Integer embeddingDimension = 1024;

        public Double getMinScore() {
            return minScore;
        }

        public void setMinScore(Double minScore) {
            this.minScore = minScore;
        }

        public Integer getDefaultTopK() {
            return defaultTopK;
        }

        public void setDefaultTopK(Integer defaultTopK) {
            this.defaultTopK = defaultTopK;
        }

        public Integer getMaxTopK() {
            return maxTopK;
        }

        public void setMaxTopK(Integer maxTopK) {
            this.maxTopK = maxTopK;
        }

        public Integer getEmbeddingDimension() {
            return embeddingDimension;
        }

        public void setEmbeddingDimension(Integer embeddingDimension) {
            this.embeddingDimension = embeddingDimension;
        }

        @AssertTrue(message = "defaultTopK 不能大于 maxTopK")
        public boolean isTopKRangeValid() {
            return defaultTopK == null || maxTopK == null || defaultTopK <= maxTopK;
        }
    }

    public static class Chat {

        @NotBlank
        private String systemPrompt = "你是 MyBiliBili 的视频内容助手。回答要基于给定视频字幕片段，不要编造片段里没有的信息。";
        @Min(1)
        private Integer answerMaxWords = 120;
        @Min(1)
        private Long sseTimeoutMs = 180000L;
        @Min(1)
        private Integer fixedAnswerChunkSize = 16;
        @Min(1)
        private Integer shortKeywordLength = 2;

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public Integer getAnswerMaxWords() {
            return answerMaxWords;
        }

        public void setAnswerMaxWords(Integer answerMaxWords) {
            this.answerMaxWords = answerMaxWords;
        }

        public Long getSseTimeoutMs() {
            return sseTimeoutMs;
        }

        public void setSseTimeoutMs(Long sseTimeoutMs) {
            this.sseTimeoutMs = sseTimeoutMs;
        }

        public Integer getFixedAnswerChunkSize() {
            return fixedAnswerChunkSize;
        }

        public void setFixedAnswerChunkSize(Integer fixedAnswerChunkSize) {
            this.fixedAnswerChunkSize = fixedAnswerChunkSize;
        }

        public Integer getShortKeywordLength() {
            return shortKeywordLength;
        }

        public void setShortKeywordLength(Integer shortKeywordLength) {
            this.shortKeywordLength = shortKeywordLength;
        }
    }

    public static class Embedding {

        @Min(1)
        private Integer queryMaxAttempts = 2;
        @Min(0)
        private Long retryIntervalMs = 1500L;

        public Integer getQueryMaxAttempts() {
            return queryMaxAttempts;
        }

        public void setQueryMaxAttempts(Integer queryMaxAttempts) {
            this.queryMaxAttempts = queryMaxAttempts;
        }

        public Long getRetryIntervalMs() {
            return retryIntervalMs;
        }

        public void setRetryIntervalMs(Long retryIntervalMs) {
            this.retryIntervalMs = retryIntervalMs;
        }
    }

    public static class Search {

        @Min(1)
        private Integer candidateMultiplier = 3;
        @DecimalMin("0.0")
        private Double subtitlePhraseBoost = 5D;
        @DecimalMin("0.0")
        private Double subtitleAndBoost = 2D;
        @DecimalMin("0.0")
        private Double titlePhraseBoost = 5D;
        @DecimalMin("0.0")
        private Double titleAndBoost = 2D;
        @DecimalMin(value = "0.0", inclusive = false)
        private Double keywordScoreDivisor = 10D;
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private Double titleScoreMin = 0.4D;
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private Double titleScoreMax = 0.7D;

        public Integer getCandidateMultiplier() {
            return candidateMultiplier;
        }

        public void setCandidateMultiplier(Integer candidateMultiplier) {
            this.candidateMultiplier = candidateMultiplier;
        }

        public Double getSubtitlePhraseBoost() {
            return subtitlePhraseBoost;
        }

        public void setSubtitlePhraseBoost(Double subtitlePhraseBoost) {
            this.subtitlePhraseBoost = subtitlePhraseBoost;
        }

        public Double getSubtitleAndBoost() {
            return subtitleAndBoost;
        }

        public void setSubtitleAndBoost(Double subtitleAndBoost) {
            this.subtitleAndBoost = subtitleAndBoost;
        }

        public Double getTitlePhraseBoost() {
            return titlePhraseBoost;
        }

        public void setTitlePhraseBoost(Double titlePhraseBoost) {
            this.titlePhraseBoost = titlePhraseBoost;
        }

        public Double getTitleAndBoost() {
            return titleAndBoost;
        }

        public void setTitleAndBoost(Double titleAndBoost) {
            this.titleAndBoost = titleAndBoost;
        }

        public Double getKeywordScoreDivisor() {
            return keywordScoreDivisor;
        }

        public void setKeywordScoreDivisor(Double keywordScoreDivisor) {
            this.keywordScoreDivisor = keywordScoreDivisor;
        }

        public Double getTitleScoreMin() {
            return titleScoreMin;
        }

        public void setTitleScoreMin(Double titleScoreMin) {
            this.titleScoreMin = titleScoreMin;
        }

        public Double getTitleScoreMax() {
            return titleScoreMax;
        }

        public void setTitleScoreMax(Double titleScoreMax) {
            this.titleScoreMax = titleScoreMax;
        }

        @AssertTrue(message = "titleScoreMin 不能大于 titleScoreMax")
        public boolean isTitleScoreRangeValid() {
            return titleScoreMin == null || titleScoreMax == null || titleScoreMin <= titleScoreMax;
        }
    }

    public static class Warmup {

        @NotNull
        private Boolean enabled = true;
        @Min(1)
        private Integer embeddingMaxAttempts = 3;
        @Min(0)
        private Long embeddingRetryIntervalMs = 2000L;
        @NotBlank
        private String embeddingProbeText = "warmup";
        @NotBlank
        private String chatSystemPrompt = "你是 MyBiliBili 的视频内容助手。";
        @NotBlank
        private String chatUserPrompt = "用一句中文回复：模型预热完成。";

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getEmbeddingMaxAttempts() {
            return embeddingMaxAttempts;
        }

        public void setEmbeddingMaxAttempts(Integer embeddingMaxAttempts) {
            this.embeddingMaxAttempts = embeddingMaxAttempts;
        }

        public Long getEmbeddingRetryIntervalMs() {
            return embeddingRetryIntervalMs;
        }

        public void setEmbeddingRetryIntervalMs(Long embeddingRetryIntervalMs) {
            this.embeddingRetryIntervalMs = embeddingRetryIntervalMs;
        }

        public String getEmbeddingProbeText() {
            return embeddingProbeText;
        }

        public void setEmbeddingProbeText(String embeddingProbeText) {
            this.embeddingProbeText = embeddingProbeText;
        }

        public String getChatSystemPrompt() {
            return chatSystemPrompt;
        }

        public void setChatSystemPrompt(String chatSystemPrompt) {
            this.chatSystemPrompt = chatSystemPrompt;
        }

        public String getChatUserPrompt() {
            return chatUserPrompt;
        }

        public void setChatUserPrompt(String chatUserPrompt) {
            this.chatUserPrompt = chatUserPrompt;
        }
    }

    public static class Es {

        @NotBlank
        private String subtitleVectorIndexName = "easylive_video_subtitle_vector";

        public String getSubtitleVectorIndexName() {
            return subtitleVectorIndexName;
        }

        public void setSubtitleVectorIndexName(String subtitleVectorIndexName) {
            this.subtitleVectorIndexName = subtitleVectorIndexName;
        }
    }
}
