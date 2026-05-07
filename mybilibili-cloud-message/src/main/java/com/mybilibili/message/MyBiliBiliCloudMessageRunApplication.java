package com.mybilibili.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 消息服务启动入口。
 *
 * <p>message 模块负责评论通知、点赞通知、系统通知等消息能力。
 * 这里使用统一包扫描，主要是为了后续把通知模板、异步发送、站内信等代码拆进来时，
 * 不必反复调整启动类上的扫描范围。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.message", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.message", "com.mybilibili.common"})
@MapperScan("com.mybilibili.message.mappers")
public class MyBiliBiliCloudMessageRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudMessageRunApplication.class, args);
    }
}
