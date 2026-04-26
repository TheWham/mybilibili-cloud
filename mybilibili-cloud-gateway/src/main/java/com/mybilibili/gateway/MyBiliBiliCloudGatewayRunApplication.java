package com.mybilibili.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动入口。
 *
 * <p>gateway 模块是外部请求进入系统的第一层，主要负责路由转发、鉴权拦截、
 * 限流和跨域处理。网关只依赖 base，不依赖 common，避免把 Web MVC、MyBatis、
 * Redis、Feign 等业务服务侧依赖全部带进网关进程。</p>
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.mybilibili")
public class MyBiliBiliCloudGatewayRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudGatewayRunApplication.class, args);
    }
}
