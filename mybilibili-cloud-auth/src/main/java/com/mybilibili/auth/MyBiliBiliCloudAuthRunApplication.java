package com.mybilibili.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.auth", "com.mybilibili.common"})
@SpringBootApplication(
    scanBasePackages = {"com.mybilibili.auth", "com.mybilibili.common.component", "com.mybilibili.common.controller", "com.mybilibili.common.config", "com.mybilibili.common.redis", "com.mybilibili.common.utils"},
    exclude = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class}
)
public class MyBiliBiliCloudAuthRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAuthRunApplication.class, args);
    }
}
