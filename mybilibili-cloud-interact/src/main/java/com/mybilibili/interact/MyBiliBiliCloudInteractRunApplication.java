package com.mybilibili.interact;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 互动服务启动入口。
 *
 * <p>interact 模块承载点赞、收藏、投币、评论、弹幕、播放历史等高频互动能力。
 * 这类业务通常需要访问用户、视频、消息等服务，因此启动阶段开启 Feign 扫描，
 * 后续只需要在 api 模块或本模块补充客户端接口即可。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.interact", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.interact", "com.mybilibili.common"})
public class MyBiliBiliCloudInteractRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudInteractRunApplication.class, args);
    }
}
