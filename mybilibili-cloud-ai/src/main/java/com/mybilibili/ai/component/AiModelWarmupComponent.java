package com.mybilibili.ai.component;

import com.mybilibili.ai.client.OllamaClient;
import com.mybilibili.ai.config.AiProperties;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 启动后预热本地 AI 模型，减少第一次 AI 搜索时的模型加载等待。
 */
@Component
public class AiModelWarmupComponent {

    private static final Logger log = LoggerFactory.getLogger(AiModelWarmupComponent.class);
    private static final int EMBEDDING_WARMUP_MAX_ATTEMPTS = 3;
    private static final long EMBEDDING_WARMUP_RETRY_INTERVAL = 2000L;

    @Resource
    private OllamaClient ollamaClient;
    @Resource
    private AiProperties aiProperties;

    @Async("aiWarmupExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        if (!Boolean.TRUE.equals(aiProperties.getWarmup().getEnabled())) {
            log.info("AI 模型预热已关闭");
            return;
        }

        // 先加载向量模型，再加载对话模型，保证两类请求第一次进来时都已经走过模型初始化。
        warmupEmbeddingModel("首次加载");
        warmupChatModel();

        // Ollama 在加载对话模型时可能会调度/卸载刚加载过的向量模型。
        // 当前建议配置 OLLAMA_MAX_LOADED_MODELS=2，所以最后再轻量请求一次向量模型。
        warmupEmbeddingModel("驻留确认");
    }

    private void warmupEmbeddingModel(String scene) {
        long start = System.currentTimeMillis();
        for (int attempt = 1; attempt <= EMBEDDING_WARMUP_MAX_ATTEMPTS; attempt++) {
            try {
                ollamaClient.embed("warmup");
                log.info("AI 向量模型预热完成, scene={}, model={}, attempt={}, cost={}ms",
                        scene, aiProperties.getOllama().getEmbeddingModel(), attempt, System.currentTimeMillis() - start);
                return;
            } catch (Exception e) {
                if (attempt >= EMBEDDING_WARMUP_MAX_ATTEMPTS) {
                    // 预热失败不影响服务启动，真正请求时还会走正常错误处理。
                    log.warn("AI 向量模型预热失败, scene={}, model={}, attempt={}, cost={}ms",
                            scene, aiProperties.getOllama().getEmbeddingModel(), attempt, System.currentTimeMillis() - start, e);
                    return;
                }
                // Ollama 刚切换模型时偶尔会返回 runner terminated，等它清理完进程后再试一次。
                log.warn("AI 向量模型预热重试, scene={}, model={}, attempt={}, cost={}ms",
                        scene, aiProperties.getOllama().getEmbeddingModel(), attempt, System.currentTimeMillis() - start, e);
                sleepQuietly(EMBEDDING_WARMUP_RETRY_INTERVAL);
            }
        }
    }

    private void warmupChatModel() {
        long start = System.currentTimeMillis();
        try {
            ollamaClient.chat("你是 MyBiliBili 的视频内容助手。", "用一句中文回复：模型预热完成。");
            log.info("AI 对话模型预热完成, model={}, cost={}ms", aiProperties.getOllama().getChatModel(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            // 这里不抛异常，避免 Ollama 暂时没启动时拖垮整个 AI 服务。
            log.warn("AI 对话模型预热失败, model={}, cost={}ms", aiProperties.getOllama().getChatModel(), System.currentTimeMillis() - start, e);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
