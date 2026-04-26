package com.mybilibili.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 媒体服务启动入口。
 *
 * <p>media 模块负责文件存储、图片上传、视频分片、HLS 资源和缩略图生成。
 * 媒体处理后续可能会回写视频服务、通知消息服务或触发审核流程，所以这里保留
 * Feign 客户端扫描，方便服务间调用统一走接口契约。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.media", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.media", "com.mybilibili.common"})
public class MyBiliBiliCloudMediaRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudMediaRunApplication.class, args);
    }
}
