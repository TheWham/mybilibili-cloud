package com.mybilibili.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminFilter extends AbstractGatewayFilterFactory {
    private static final Logger log = LoggerFactory.getLogger(AdminFilter.class);

    @Override
    public GatewayFilter apply(Object config) {

        return( (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String rawPath = request.getURI().getRawPath();
            log.info("admin 请求路径{}", rawPath);
            // 后台登录态由 mybilibili-cloud-admin 的 AdminLoginInterceptor 统一校验。
            // gateway 这里只保留网关层日志和后续限流、审计等横切能力。
            return chain.filter(exchange);
        });
    }
}
