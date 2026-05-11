package com.mybilibili.base.entity.dto;

import jakarta.validation.constraints.NotEmpty;

/**
 * 后台登录参数。
 *
 * <p>后台现在是单账号配置模式，这里只保留前端实际提交的字段，
 * 不和普通用户登录 DTO 混用，避免邮箱校验之类的前台规则误伤后台登录。</p>
 */
public class AdminLoginDTO {

    @NotEmpty
    private String account;

    @NotEmpty
    private String password;

    @NotEmpty
    private String checkCodeKey;

    @NotEmpty
    private String checkCode;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCheckCodeKey() {
        return checkCodeKey;
    }

    public void setCheckCodeKey(String checkCodeKey) {
        this.checkCodeKey = checkCodeKey;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }
}
