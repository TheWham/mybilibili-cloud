package com.mybilibili.ai.component;

import com.mybilibili.ai.client.AiChatModelClient;
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

    @Resource
    private OllamaClient ollamaClient;
    @Resource
    private AiChatModelClient aiChatModelClient;
    @Resource
    private AiProperties aiProperties;

    @Async("aiWarmupExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        if (!Boolean.TRUE.equals(aiProperties.getWarmup().getEnabled())) {
            log.info("AI 模型预热已关闭");
            return;
        }

        // 先把本地向量模型拉起来。云端对话预热默认关闭，避免启动阶段消耗额度或被网络抖动拖慢。
        warmupEmbeddingModel("首次加载");
        if (Boolean.TRUE.equals(aiProperties.getWarmup().getChatEnabled())) {
            warmupChatModel();
        } else {
            log.info("AI 对话模型预热已关闭");
        }
    }

    private void warmupEmbeddingModel(String scene) {
        long start = System.currentTimeMillis();
        int maxAttempts = aiProperties.getWarmup().getEmbeddingMaxAttempts();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ollamaClient.embed(aiProperties.getWarmup().getEmbeddingProbeText());
                log.info("AI 向量模型预热完成, scene={}, model={}, attempt={}, cost={}ms",
                        scene, aiProperties.getOllama().getEmbeddingModel(), attempt, System.currentTimeMillis() - start);
                return;
            } catch (Exception e) {
                if (attempt >= maxAttempts) {
                    // 预热失败不影响服务启动，真正请求时还会走正常错误处理。
                    log.warn("AI 向量模型预热失败, scene={}, model={}, attempt={}, cost={}ms",
                            scene, aiProperties.getOllama().getEmbeddingModel(), attempt, System.currentTimeMillis() - start, e);
                    return;
                }
                // Ollama 刚切换模型时偶尔会返回 runner terminated，等它清理完进程后再试一次。
                log.warn("AI 向量模型预热重试, scene={}, model={}, attempt={}, cost={}ms",
                        scene, aiProperties.getOllama().getEmbeddingModel(), attempt, System.currentTimeMillis() - start, e);
                sleepQuietly(aiProperties.getWarmup().getEmbeddingRetryIntervalMs());
            }
        }
    }

    private void warmupChatModel() {
        long start = System.currentTimeMillis();
        try {
            aiChatModelClient.chat(aiProperties.getWarmup().getChatSystemPrompt(), aiProperties.getWarmup().getChatUserPrompt());
            log.info("AI 对话模型预热完成, model={}, cost={}ms",
                    aiProperties.getChatProvider().getModel(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            // 对话模型现在走外部 API，预热失败只记录，不影响字幕向量检索和服务启动。
            log.warn("AI 对话模型预热失败, model={}, cost={}ms",
                    aiProperties.getChatProvider().getModel(), System.currentTimeMillis() - start, e);
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
