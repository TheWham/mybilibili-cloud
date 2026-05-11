package com.mybilibili.admin.interceptor;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.utils.JsonUtils;
import com.mybilibili.common.component.TokenRedisComponent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;

/**
 * 后台接口登录校验。
 *
 * <p>Gateway 只做路由，不接 Redis。真正的 adminToken 校验放在 admin 服务，
 * 这样后台登录态的读写和清理都收口在同一个业务服务里。</p>
 */
@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    private static final String ACCOUNT_PATH = "/account/";
    private static final String DIRECT_ACCOUNT_PATH = "/admin/account/";
    private static final String ERROR_PATH = "/error";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String STATUS_ERROR = "error";

    @Resource
    private TokenRedisComponent tokenRedisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(ACCOUNT_PATH)
                || requestUri.startsWith(DIRECT_ACCOUNT_PATH)
                || requestUri.startsWith(ERROR_PATH)) {
            return true;
        }

        String tokenId = getTokenId(request);
        if (tokenId == null || tokenRedisComponent.getTokenInfo4Admin(tokenId) == null) {
            writeNotLogin(response);
            return false;
        }
        return true;
    }

    private String getTokenId(HttpServletRequest request) {
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

    private void writeNotLogin(HttpServletResponse response) throws Exception {
        ResponseVO<Void> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUS_ERROR);
        responseVO.setCode(ResponseCodeEnum.CODE_901.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_901.getMsg());
        response.setContentType(CONTENT_TYPE_JSON);
        response.getWriter().write(JsonUtils.convertObj2Json(responseVO));
    }
}
