package com.mybilibili.common.aspect;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.component.RedisComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Aspect
@Component
public class UserLoginAspect extends GlobalOperationAspect{

    @Resource
    private RedisComponent redisComponent;

    @Before("@annotation(com.mybilibili.common.annotation.LoginInterceptor) || @within(com.mybilibili.common.annotation.LoginInterceptor)")
    public void validLogin(JoinPoint point)
    {
        LoginInterceptor annotation = getAnnotation(point, LoginInterceptor.class);
        if (annotation == null || !annotation.checkLogin())
            return;

        checkLogin();
    }


    private void checkLogin()
    {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        String tokenId = request.getHeader(Constants.WEB_TOKEN_KEY);
        if (tokenId == null)
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        TokenUserInfoDTO tokenInfo = redisComponent.getTokenInfo(tokenId);
        if (tokenInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_901);
    }

}
