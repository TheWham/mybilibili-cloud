package com.mybilibili.admin.controller;

import com.mybilibili.admin.component.AdminRedisComponent;
import com.mybilibili.admin.services.AdminAccountService;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.AdminLoginDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.component.TokenRedisComponent;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController extends ABaseController {

    @Resource
    private AdminAccountService adminAccountService;

    @Resource
    private AdminRedisComponent adminRedisComponent;

    @Resource
    private TokenRedisComponent tokenRedisComponent;

    @RequestMapping("/checkCode")
    public ResponseVO getCheckCode() {
        return getSuccessResponseVO(adminAccountService.getCheckCode());
    }

    @RequestMapping("/login")
    public ResponseVO login(HttpServletResponse response, @Validated AdminLoginDTO adminLoginDTO) {
        try {
            String tokenId = adminAccountService.login(adminLoginDTO);
            saveAdminTokenCookie(response, tokenId);
            return getSuccessResponseVO(adminLoginDTO.getAccount());
        } finally {
            adminRedisComponent.cleanCheckCode(adminLoginDTO.getCheckCodeKey());
        }
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletRequest request, HttpServletResponse response) {
        String tokenId = getAdminToken(request);
        if (tokenId != null) {
            tokenRedisComponent.cleanAdminToken(tokenId);
        }
        cleanAdminTokenCookie(response);
        return getSuccessResponseVO(null);
    }

    private void saveAdminTokenCookie(HttpServletResponse response, String tokenId) {
        Cookie cookie = new Cookie(Constants.ADMIN_TOKEN_KEY, tokenId);
        // 后台管理端保持会话级登录态，关闭浏览器后自然失效。
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void cleanAdminTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(Constants.ADMIN_TOKEN_KEY, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getAdminToken(HttpServletRequest request) {
        String tokenId = request.getHeader(Constants.ADMIN_TOKEN_KEY);
        if (tokenId != null && !tokenId.isBlank()) {
            return tokenId;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (Constants.ADMIN_TOKEN_KEY.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
