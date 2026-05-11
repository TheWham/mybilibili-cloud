package com.mybilibili.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * user 服务异步线程池配置。
 *
 * <p>用户统计缓存刷新不是接口主链路，单独使用小线程池处理，避免和 Web 请求线程互相影响。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Configuration
public class UserAsyncConfig {

    @Bean("userStatsCacheExecutor")
    public Executor userStatsCacheExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("user-stats-cache-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
