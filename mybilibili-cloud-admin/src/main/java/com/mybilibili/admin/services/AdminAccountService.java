package com.mybilibili.admin.services;

import com.mybilibili.base.entity.dto.AdminLoginDTO;

import java.util.Map;

/**
 * 后台账号服务。
 */
public interface AdminAccountService {

    /**
     * 生成后台登录验证码。
     *
     * @return 验证码图片和 Redis key
     */
    Map<String, String> getCheckCode();

    /**
     * 校验后台账号密码，成功后返回新 token。
     *
     * @param adminLoginDTO 登录参数
     * @return adminToken
     */
    String login(AdminLoginDTO adminLoginDTO);
}
