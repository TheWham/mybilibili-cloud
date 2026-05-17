package com.mybilibili.ai.config;

import com.mybilibili.ai.constants.AiConstants;
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
    private ChatProvider chatProvider = new ChatProvider();
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

    public ChatProvider getChatProvider() {
        return chatProvider;
    }

    public void setChatProvider(ChatProvider chatProvider) {
        this.chatProvider = chatProvider == null ? new ChatProvider() : chatProvider;
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
        /**
         * 本地联调时的兜底 embedding 模型。
         *
         * <p>线上和测试环境优先使用 Nacos 等外部配置覆盖，避免仓库默认值和运行环境脱节。</p>
         */
        @NotBlank
        private String embeddingModel = "bge-m3:567m";
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

    public static class ChatProvider {

        @NotBlank
        private String baseUrl = "https://api.openai.com";
        private String apiKey;
        @NotBlank
        private String model = "gpt-4o-mini";
        @NotBlank
        private String chatCompletionsPath = AiConstants.OPENAI_CHAT_COMPLETIONS_API_PATH;
        @Min(1)
        private Integer connectTimeoutSeconds = 5;
        @Min(1)
        private Integer readTimeoutSeconds = 180;
        @Min(1)
        private Integer writeTimeoutSeconds = 30;
        @Min(0)
        private Integer maxRetries = 1;
        @Min(0)
        private Long retryIntervalMs = 1000L;

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getChatCompletionsPath() {
            return chatCompletionsPath;
        }

        public void setChatCompletionsPath(String chatCompletionsPath) {
            this.chatCompletionsPath = chatCompletionsPath;
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

        public Integer getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
        }

        public Long getRetryIntervalMs() {
            return retryIntervalMs;
        }

        public void setRetryIntervalMs(Long retryIntervalMs) {
            this.retryIntervalMs = retryIntervalMs;
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
        /**
         * 问题分析阶段使用的系统提示词。
         *
         * <p>这里要求模型只做结构化理解，不直接回答业务问题，避免把召回前置逻辑和内容生成混在一起。</p>
         */
        @NotBlank
        private String queryAnalysisSystemPrompt = "你是 MyBiliBili 视频检索助手。你的任务是分析用户问题，"
                + "识别用户想找的视频主题、片段、类型或内容，并把问题改写成更适合视频字幕向量检索的关键词。"
                + "你必须只返回 JSON，不要输出 Markdown、解释文字或代码块。";
        @Min(1)
        private Integer answerMaxWords = 120;
        /**
         * 问题分析说明和追问文案的建议上限。
         */
        @Min(1)
        private Integer queryAnalysisMaxWords = 60;
        @Min(1)
        private Long sseTimeoutMs = 180000L;
        /**
         * AI 会话在 Redis 中的过期时间，单位毫秒。
         *
         * <p>保留一个相对宽松的时长，避免用户中途刷新页面后立刻丢上下文。</p>
         */
        @Min(1)
        private Long sessionExpireMs = 86400000L;
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

        public String getQueryAnalysisSystemPrompt() {
            return queryAnalysisSystemPrompt;
        }

        public void setQueryAnalysisSystemPrompt(String queryAnalysisSystemPrompt) {
            this.queryAnalysisSystemPrompt = queryAnalysisSystemPrompt;
        }

        public Integer getAnswerMaxWords() {
            return answerMaxWords;
        }

        public void setAnswerMaxWords(Integer answerMaxWords) {
            this.answerMaxWords = answerMaxWords;
        }

        public Integer getQueryAnalysisMaxWords() {
            return queryAnalysisMaxWords;
        }

        public void setQueryAnalysisMaxWords(Integer queryAnalysisMaxWords) {
            this.queryAnalysisMaxWords = queryAnalysisMaxWords;
        }

        public Long getSseTimeoutMs() {
            return sseTimeoutMs;
        }

        public void setSseTimeoutMs(Long sseTimeoutMs) {
            this.sseTimeoutMs = sseTimeoutMs;
        }

        public Long getSessionExpireMs() {
            return sessionExpireMs;
        }

        public void setSessionExpireMs(Long sessionExpireMs) {
            this.sessionExpireMs = sessionExpireMs;
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
        @NotNull
        private Boolean chatEnabled = false;
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

        public Boolean getChatEnabled() {
            return chatEnabled;
        }

        public void setChatEnabled(Boolean chatEnabled) {
            this.chatEnabled = chatEnabled;
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

        /**
         * AI 服务连接 Elasticsearch 的地址。
         *
         * <p>为了兼容当前项目里 `host:port` 的历史写法，这里直接用一个字符串承载，
         * 避免再拆成多级配置后和公共模块的读取方式出现偏差。</p>
         */
        @NotBlank
        private String hostPort = "127.0.0.1:9201";
        @NotBlank
        private String subtitleVectorIndexName = "easylive_video_subtitle_vector";
        /**
         * 是否在应用启动阶段主动检查并补建字幕向量索引。
         *
         * <p>开发环境经常会遇到本地 ES 重建、索引被手工删除等情况，保留自动初始化可以减少手工步骤；
         * 如果某些环境通过运维脚本统一建索引，也可以显式关闭。</p>
         */
        @NotNull
        private Boolean initEnabled = true;
        /**
         * 初始化索引失败时是否直接终止应用启动。
         *
         * <p>生产环境如果把字幕检索视为核心链路，可以打开 fail-fast；本地联调更推荐关闭，
         * 先让服务启动起来，再根据日志单独处理 ES 侧问题。</p>
         */
        @NotNull
        private Boolean failFastOnInitError = false;

        public String getHostPort() {
            return hostPort;
        }

        public void setHostPort(String hostPort) {
            this.hostPort = hostPort;
        }

        public String getSubtitleVectorIndexName() {
            return subtitleVectorIndexName;
        }

        public void setSubtitleVectorIndexName(String subtitleVectorIndexName) {
            this.subtitleVectorIndexName = subtitleVectorIndexName;
        }

        public Boolean getInitEnabled() {
            return initEnabled;
        }

        public void setInitEnabled(Boolean initEnabled) {
            this.initEnabled = initEnabled;
        }

        public Boolean getFailFastOnInitError() {
            return failFastOnInitError;
        }

        public void setFailFastOnInitError(Boolean failFastOnInitError) {
            this.failFastOnInitError = failFastOnInitError;
        }
    }
}
