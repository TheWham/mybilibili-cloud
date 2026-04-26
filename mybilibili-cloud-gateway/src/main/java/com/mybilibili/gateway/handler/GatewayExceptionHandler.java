package com.mybilibili.gateway.handler;

import entity.ResponseVO;
import enums.ResponseCodeEnum;
import exception.BusinessException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import utils.JsonUtils;

import java.nio.charset.StandardCharsets;

/**
 * 全局异常管理
 * @author amani
 * @since 2026.4.26
 */
@Component
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {
    protected static final String STATIC_ERROR = "error";

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ResponseVO responseVO = getResponse(ex);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buff = response.bufferFactory().wrap(JsonUtils.convertObj2Json(responseVO).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buff));
    }

    private ResponseVO getResponse(Throwable ex)
    {
        ResponseVO responseVO = new ResponseVO();
        responseVO.setStatus(STATIC_ERROR);
        if (ex instanceof ResponseStatusException)
        {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            if (HttpStatus.NOT_FOUND==responseStatusException.getStatusCode())
            {
                responseVO.setCode(ResponseCodeEnum.CODE_404.getCode());
                responseVO.setInfo(ResponseCodeEnum.CODE_404.getMsg());
                return responseVO;

            } else if (HttpStatus.SERVICE_UNAVAILABLE==responseStatusException.getStatusCode())
            {
                responseVO.setCode(ResponseCodeEnum.CODE_503.getCode());
                responseVO.setInfo(ResponseCodeEnum.CODE_503.getMsg());
                return responseVO;
            }else
            {
                responseVO.setCode(responseStatusException.getStatusCode().value());
                responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
                return responseVO;
            }
        } else if (ex instanceof BusinessException)
        {
            BusinessException exception = (BusinessException) ex;
            responseVO.setCode(exception.getCode());
            responseVO.setInfo(exception.getMessage());
            return responseVO;
        }
        responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        return responseVO;
    }
}
