package com.mybilibili.auth.services.impl;

import com.mybilibili.auth.component.AuthRedisComponent;
import com.mybilibili.auth.consumer.UserLoginClient;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.auth.services.AuthService;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.component.TokenRedisComponent;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserLoginClient userLoginClient;

    @Resource
    private AuthRedisComponent authRedisComponent;

    @Resource
    private TokenRedisComponent tokenRedisComponent;

    @Override
    public Integer changeStatus(String userId, Integer type) {
        return userLoginClient.changeStatus(userId, type);
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        checkImageCode(registerDTO.getCheckCode(), registerDTO.getCheckCodeKey());
        try {
            userLoginClient.register(registerDTO);
        } finally {
            authRedisComponent.cleanCheckCode(registerDTO.getCheckCodeKey());
        }
    }

    @Override
    public TokenUserInfoDTO login(WebLoginDTO webLoginDTO) {
        checkImageCode(webLoginDTO.getCheckCode(), webLoginDTO.getCheckCodeKey());
        try {
            return userLoginClient.login(webLoginDTO);
        } finally {
            authRedisComponent.cleanCheckCode(webLoginDTO.getCheckCodeKey());
        }
    }

    @Override
    public TokenUserInfoDTO autoLogin(TokenUserInfoDTO tokenUserInfo) {
        if (tokenUserInfo == null) {
            return null;
        }

        String latestTokenId = tokenRedisComponent.getTokenIdByUserId(tokenUserInfo.getUserId());
        if (!tokenUserInfo.getTokenId().equals(latestTokenId)) {
            return null;
        }

        if (tokenUserInfo.getExpireAt() - System.currentTimeMillis() < Constants.REDIS_EXPIRE_TIME_ONE_DAY) {
            tokenUserInfo.setExpireAt(System.currentTimeMillis()
                    + (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT);
            tokenRedisComponent.updateTokenUserInfo(tokenUserInfo);
        }

        // 用户实时统计归 user 服务维护，auth 这里只负责登录链路触发一次刷新。
        userLoginClient.refreshRealtimeUserStatsCache(tokenUserInfo.getUserId());
        return tokenUserInfo;
    }

    @Override
    public UserCountVO getUserCountInfo(String userId) {
        return userLoginClient.getUserCountInfo(userId);
    }

    @Override
    public Map<String, String> getCheckCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String checkCodeKey = authRedisComponent.saveCode(captcha.text());

        Map<String, String> result = new HashMap<>(2);
        result.put("checkCodeKey", checkCodeKey);
        result.put("checkCode", captcha.toBase64());
        return result;
    }

    private void checkImageCode(String checkCode, String checkCodeKey) {
        String redisCode = authRedisComponent.getCode(checkCodeKey);
        if (checkCode == null || !checkCode.equalsIgnoreCase(redisCode)) {
            throw new BusinessException("图形验证码不正确");
        }
    }
}
