package com.mybilibili.video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 视频服务启动入口。
 *
 * <p>video 模块负责投稿、视频信息、分 P 文件、审核状态和分类等核心视频能力。
 * 视频服务会和媒体、搜索、互动等服务协作，所以这里开启 Feign 客户端扫描，
 * 后续拆业务接口时可以直接落在 com.mybilibili 统一包结构下。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mybilibili")
@SpringBootApplication(scanBasePackages = "com.mybilibili")
public class MyBiliBiliCloudVideoRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudVideoRunApplication.class, args);
    }
}
