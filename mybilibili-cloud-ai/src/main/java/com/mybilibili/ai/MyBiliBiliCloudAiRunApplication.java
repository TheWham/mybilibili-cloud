package com.mybilibili.ai;

import com.mybilibili.common.component.TokenRedisComponent;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.common.config.ElasticSearchConfig;
import com.mybilibili.common.utils.FFmpegUtils;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 服务启动入口。
 */
@EnableAsync
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.ai", "com.mybilibili.common"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
@ComponentScan(basePackages = {
        "com.mybilibili.ai",
        "com.mybilibili.common.controller",
        "com.mybilibili.common.redis"
})
@Import({TokenRedisComponent.class, AdminConfig.class, ElasticSearchConfig.class, FFmpegUtils.class})
public class MyBiliBiliCloudAiRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAiRunApplication.class, args);
    }
}
