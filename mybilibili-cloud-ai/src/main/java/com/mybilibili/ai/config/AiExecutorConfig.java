package com.mybilibili.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AI 任务线程池。
 *
 * <p>模型预热和 SSE 输出不能占用 Web 容器线程太久，单独拆线程池更稳。</p>
 */
@Configuration
public class AiExecutorConfig {

    @Bean("aiChatExecutor")
    public Executor aiChatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-chat-");
        executor.initialize();
        return executor;
    }

    @Bean("aiWarmupExecutor")
    public Executor aiWarmupExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("ai-warmup-");
        executor.initialize();
        return executor;
    }
}
