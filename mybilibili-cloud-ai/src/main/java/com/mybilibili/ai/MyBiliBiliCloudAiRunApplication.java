package com.mybilibili.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 服务启动入口。
 */
@EnableAsync
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.ai", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.ai", "com.mybilibili.common"})
public class MyBiliBiliCloudAiRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAiRunApplication.class, args);
    }
}
