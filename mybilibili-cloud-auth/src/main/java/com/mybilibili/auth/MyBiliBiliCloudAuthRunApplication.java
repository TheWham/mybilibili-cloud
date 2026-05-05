package com.mybilibili.auth;

import com.mybilibili.common.component.TokenRedisComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.auth", "com.mybilibili.common"})
@SpringBootApplication(
    exclude = {DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class}
)
@ComponentScan(basePackages = {
        "com.mybilibili.auth",
        "com.mybilibili.common.aspect",
        "com.mybilibili.common.controller",
        "com.mybilibili.common.redis"
})
@Import(TokenRedisComponent.class)
public class MyBiliBiliCloudAuthRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAuthRunApplication.class, args);
    }
}
