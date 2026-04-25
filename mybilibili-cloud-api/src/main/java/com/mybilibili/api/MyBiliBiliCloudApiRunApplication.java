package com.mybilibili.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * API 模块启动入口。
 *
 * <p>按模块职责，api 更适合作为 DTO、Feign Client、事件对象的公共契约包。
 * 但当前 POM 已经配置了可执行 mainClass，所以这里先补齐启动类，保证 Maven
 * 打包和 IDE 启动配置不报错。后续如果确认 api 只做契约模块，可以再移除
 * spring-boot-maven-plugin 和这个启动入口。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mybilibili")
@SpringBootApplication(scanBasePackages = "com.mybilibili")
public class MyBiliBiliCloudApiRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudApiRunApplication.class, args);
    }
}
