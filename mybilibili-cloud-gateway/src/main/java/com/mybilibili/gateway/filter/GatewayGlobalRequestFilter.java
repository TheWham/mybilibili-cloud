package com.mybilibili.gateway.filter;

import enums.ResponseCodeEnum;
import exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author amani
 */
@Component
public class GatewayGlobalRequestFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(GatewayGlobalRequestFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String request = exchange.getRequest().getURI().getRawPath();
        if (request.contains("innerApi"))
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        log.info("全局拦截请求路径是{}", request);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
