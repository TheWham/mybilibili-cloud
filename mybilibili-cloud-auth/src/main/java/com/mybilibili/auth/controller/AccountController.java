package com.mybilibili.auth.controller;

import com.mybilibili.auth.services.AuthService;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController {

    @Resource
    private AuthService authService;

    /**
     * 获取验证码接口。
     *
     * @return 验证码图片和对应的 Redis key
     */
    @RequestMapping("/checkCode")
    public ResponseVO getCheckCode(){
        return getSuccessResponseVO(authService.getCheckCode());
    }

    /**
     * 用户注册接口。
     *
     * @param registerDTO 注册信息
     * @return 统一响应
     */
    @RequestMapping("/register")
    public ResponseVO register(@Validated RegisterDTO registerDTO){
        authService.register(registerDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/login")
    public ResponseVO login(
            HttpServletResponse response,
            @Validated WebLoginDTO webLoginDTO) {
        webLoginDTO.setLastLoginIp(getIpAddr());
        TokenUserInfoDTO tokenInfo = authService.login(webLoginDTO);
        saveToken2Session(response, tokenInfo.getTokenId());
        return getSuccessResponseVO(tokenInfo);
    }

    @RequestMapping("/autoLogin")
    public ResponseVO autoLogin(
            HttpServletResponse response) {
        TokenUserInfoDTO tokenUserInfo = authService.autoLogin(getTokenUserInfo());
        if (tokenUserInfo == null) {
            return getSuccessResponseVO(null);
        }

        saveToken2Session(response, tokenUserInfo.getTokenId());
        return getSuccessResponseVO(tokenUserInfo);
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletResponse response){
        cleanCookie(response);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getUserCountInfo")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO getUserCountInfo() {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo();
        UserCountVO userCountVO = authService.getUserCountInfo(tokenUserInfoDTO.getUserId());
        return getSuccessResponseVO(userCountVO);
    }

}
