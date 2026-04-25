package com.mybilibili.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证授权服务启动入口。
 *
 * <p>auth 模块负责登录、注册、Token 发放、权限校验等认证相关能力。
 * 认证服务通常会依赖用户服务、消息服务或网关侧校验逻辑，所以这里开启 Feign
 * 客户端扫描，后续新增远程调用接口时不需要再调整启动类。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mybilibili")
@SpringBootApplication(scanBasePackages = "com.mybilibili")
public class MyBiliBiliCloudAuthRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAuthRunApplication.class, args);
    }
}
