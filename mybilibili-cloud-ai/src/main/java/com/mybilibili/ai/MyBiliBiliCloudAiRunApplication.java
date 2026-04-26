package com.mybilibili.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI 服务启动入口。
 *
 * <p>ai 模块后续主要承载关键词生成、语义向量、相似视频召回、问答等能力。
 * 当前先把服务发现、Feign 扫描和基础包扫描补齐，方便后续接入独立业务代码时
 * 直接按微服务方式注册到 Nacos 并调用其他服务。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.ai", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.ai", "com.mybilibili.common"})
public class MyBiliBiliCloudAiRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAiRunApplication.class, args);
    }
}
