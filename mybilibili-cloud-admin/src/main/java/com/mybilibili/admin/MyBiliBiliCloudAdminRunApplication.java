package com.mybilibili.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台管理服务启动入口。
 *
 * <p>admin 模块负责后台管理、审核、统计看板等能力，依赖 common 模块获取
 * Nacos、OpenFeign、MyBatis、Redis 等基础设施能力。这里统一扫描 com.mybilibili，
 * 是为了后续拆分 controller、service、mapper、feign client 时，不会因为包路径分散
 * 导致组件无法被 Spring 容器发现。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.mybilibili.admin", "com.mybilibili.common"})
@SpringBootApplication(scanBasePackages = {"com.mybilibili.admin", "com.mybilibili.common"})
@MapperScan({"com.mybilibili.admin.mappers", "com.mybilibili.common.mappers"})
public class MyBiliBiliCloudAdminRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudAdminRunApplication.class, args);
    }
}
