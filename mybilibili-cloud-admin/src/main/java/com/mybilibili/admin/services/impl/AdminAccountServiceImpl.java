package com.mybilibili.admin.services.impl;

import com.mybilibili.admin.component.AdminRedisComponent;
import com.mybilibili.admin.services.AdminAccountService;
import com.mybilibili.base.entity.dto.AdminLoginDTO;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.component.TokenRedisComponent;
import com.mybilibili.common.config.AdminConfig;
import com.mybilibili.common.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    @Resource
    private AdminRedisComponent adminRedisComponent;

    @Resource
    private AdminConfig adminConfig;

    @Resource
    private TokenRedisComponent tokenRedisComponent;

    @Override
    public Map<String, String> getCheckCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String checkCodeKey = adminRedisComponent.saveCode(captcha.text());

        Map<String, String> result = new HashMap<>(2);
        result.put("checkCodeKey", checkCodeKey);
        result.put("checkCode", captcha.toBase64());
        return result;
    }

    @Override
    public String login(AdminLoginDTO adminLoginDTO) {
        String redisCode = adminRedisComponent.getCode(adminLoginDTO.getCheckCodeKey());
        if (redisCode == null || !adminLoginDTO.getCheckCode().equalsIgnoreCase(redisCode)) {
            throw new BusinessException("图形验证码不正确");
        }

        String configPassword = StringTools.md5Password(adminConfig.getPassword());
        boolean accountMatched = Objects.equals(adminLoginDTO.getAccount(), adminConfig.getAccount());
        boolean passwordMatched = Objects.equals(adminLoginDTO.getPassword(), configPassword);
        if (!accountMatched || !passwordMatched) {
            throw new BusinessException("账号或密码错误");
        }
        return tokenRedisComponent.saveToken4Admin(adminLoginDTO.getAccount());
    }
}
