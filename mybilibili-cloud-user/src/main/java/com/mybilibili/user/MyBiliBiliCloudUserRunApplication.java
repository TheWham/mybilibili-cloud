package com.mybilibili.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动入口。
 *
 * <p>user 模块负责用户资料、关注关系、用户统计和用户主页等能力。
 * 作为基础业务服务，它会被 auth、interact、message 等服务频繁调用，因此启动类
 * 保持标准微服务配置：注册到 Nacos，并开启 Feign 客户端扫描。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.user", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.user", "com.mybilibili.common"})
@MapperScan("com.mybilibili.user.mappers")
public class MyBiliBiliCloudUserRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudUserRunApplication.class, args);
    }
}
