package com.mybilibili.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 服务配置。
 *
 * <p>默认值贴合当前本地开发环境；上线时放到 Nacos 的 mybilibili-cloud-ai-*.yml 覆盖即可。</p>
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private Ollama ollama = new Ollama();
    private Rag rag = new Rag();
    private Warmup warmup = new Warmup();
    private Es es = new Es();

    public Ollama getOllama() {
        return ollama;
    }

    public void setOllama(Ollama ollama) {
        this.ollama = ollama;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public Warmup getWarmup() {
        return warmup;
    }

    public void setWarmup(Warmup warmup) {
        this.warmup = warmup;
    }

    public Es getEs() {
        return es;
    }

    public void setEs(Es es) {
        this.es = es;
    }

    public static class Ollama {
        private String baseUrl = "http://127.0.0.1:11434";
        private String chatModel = "qwen2.5:3b";
        private String embeddingModel = "bge-m3-cpu:567m";
        private String keepAlive = "10m";
        private Integer connectTimeoutSeconds = 5;
        private Integer readTimeoutSeconds = 180;
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
        private Double minScore = 0.55D;
        private Integer defaultTopK = 5;
        private Integer maxTopK = 10;
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
    }

    public static class Warmup {
        private Boolean enabled = true;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Es {
        private String subtitleVectorIndexName = "easylive_video_subtitle_vector";

        public String getSubtitleVectorIndexName() {
            return subtitleVectorIndexName;
        }

        public void setSubtitleVectorIndexName(String subtitleVectorIndexName) {
            this.subtitleVectorIndexName = subtitleVectorIndexName;
        }
    }
}
