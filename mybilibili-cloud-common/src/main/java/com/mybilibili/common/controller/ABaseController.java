package com.mybilibili.common.controller;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.component.RedisComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ABaseController {

    protected static final String STATUS_SUCCESS = "success";
    protected static final String STATUS_ERROR = "error";

    @Resource
    private RedisComponent redisComponent;



    protected <T> ResponseVO getSuccessResponseVO(T t){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getBusinessErrorResponseVO(BusinessException e, T t){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_ERROR);
        if (e.getCode() == null){
            responseVO.setCode(ResponseCodeEnum.CODE_600.getCode());
        }else{
            responseVO.setCode(e.getCode());
        }
        responseVO.setInfo(e.getMessage());
        responseVO.setData(t);
        return responseVO;
    }

    protected <T> ResponseVO getServerErrorResponseVO(T t){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_500.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected void saveToken2Session(HttpServletResponse response, String tokenId){
        Cookie cookie = new Cookie(Constants.ADMIN_TOKEN_KEY, tokenId);
        //admin cookie周期为会话, 关闭浏览器则失效
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    protected void cleanCookie(HttpServletResponse response)
    {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(Constants.ADMIN_TOKEN_KEY)){
                String tokenId = cookie.getValue();
                cookie.setPath("/");
                cookie.setMaxAge(0);
                redisComponent.cleanAdminToken(tokenId);
                response.addCookie(cookie);
                break;
            }
        }
    }
}
