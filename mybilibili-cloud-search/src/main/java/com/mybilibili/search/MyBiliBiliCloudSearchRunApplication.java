package com.mybilibili.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 搜索服务启动入口。
 *
 * <p>search 模块负责 Elasticsearch 查询和索引同步。搜索服务通常需要从视频、
 * 用户、互动等服务同步或补全索引数据，所以这里启用 Feign 扫描，为后续服务调用
 * 和索引维护任务预留好基础能力。</p>
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mybilibili")
@SpringBootApplication(scanBasePackages = "com.mybilibili")
public class MyBiliBiliCloudSearchRunApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBiliBiliCloudSearchRunApplication.class, args);
    }
}
