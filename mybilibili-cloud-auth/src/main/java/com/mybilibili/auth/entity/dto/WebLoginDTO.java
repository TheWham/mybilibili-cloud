package com.mybilibili.auth.entity.dto;


import com.mybilibili.base.constants.Constants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;


public class WebLoginDTO {
    @NotEmpty
    @Pattern(regexp = Constants.EMAIL_REGEX)
    private String email;

    @NotEmpty
    private String password;

    @NotEmpty
    private String checkCodeKey;

    @NotEmpty
    private String checkCode;

    private String lastLoginIp;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
}
