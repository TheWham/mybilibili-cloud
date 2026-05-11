package com.mybilibili.common.handler;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * MVC 服务统一异常返回。
 *
 * <p>前端一直按 ResponseVO.code 判断业务状态。微服务拆分后如果没有统一处理，
 * BusinessException 会被 Spring 默认转成 500，后台登录超时这类场景就没法正确跳转。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String STATUS_ERROR = "error";

    @ExceptionHandler(BusinessException.class)
    public ResponseVO<Void> handleBusinessException(BusinessException exception) {
        Integer code = exception.getCode() == null
                ? ResponseCodeEnum.CODE_600.getCode()
                : exception.getCode();
        String message = exception.getMessage() == null
                ? ResponseCodeEnum.CODE_600.getMsg()
                : exception.getMessage();
        return buildErrorResponse(code, message);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            BindException.class
    })
    public ResponseVO<Void> handleParamException(Exception exception) {
        LOGGER.warn("请求参数校验失败", exception);
        return buildErrorResponse(ResponseCodeEnum.CODE_600.getCode(), ResponseCodeEnum.CODE_600.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public ResponseVO<Void> handleException(Exception exception) {
        LOGGER.error("服务处理异常", exception);
        return buildErrorResponse(ResponseCodeEnum.CODE_500.getCode(), ResponseCodeEnum.CODE_500.getMsg());
    }

    private ResponseVO<Void> buildErrorResponse(Integer code, String message) {
        ResponseVO<Void> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_ERROR);
        responseVO.setCode(code);
        responseVO.setInfo(message);
        return responseVO;
    }
}
