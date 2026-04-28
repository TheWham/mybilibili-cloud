package com.mybilibili.common.controller;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
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

    protected String getIpAddr() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        String ip = request.getHeader("x-forwarded-for");

        // 多级反向代理时，x-forwarded-for 会有多个 IP，第一个才是真实 IP
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    protected void saveToken2Session(HttpServletResponse response, String tokenId){
        Cookie cookie = new Cookie(Constants.WEB_TOKEN_KEY, tokenId);
        cookie.setMaxAge(Constants.WEB_TOKEN_EXPIRE_TIME * Constants.WEB_EXPIRE_TIME_DAY_COUNT);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    protected TokenUserInfoDTO getTokenUserInfo(){
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes())
                        .getRequest();

        String tokenId = request.getHeader(Constants.WEB_TOKEN_KEY);
        TokenUserInfoDTO tokenInfo = redisComponent.getTokenInfo(tokenId);
        return tokenInfo;
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
            if (cookie.getName().equals(Constants.WEB_TOKEN_KEY)){
                String tokenId = cookie.getValue();
                cookie.setPath("/");
                cookie.setMaxAge(0);
                redisComponent.cleanWebToken(tokenId);
                response.addCookie(cookie);
                break;
            }
        }
    }


}
