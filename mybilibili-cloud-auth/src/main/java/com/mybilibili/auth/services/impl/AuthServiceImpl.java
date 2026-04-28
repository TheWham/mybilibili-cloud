package com.mybilibili.auth.services.impl;

import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.auth.services.AuthService;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public Integer changeStatus(String userId, Integer type) {
        return 0;
    }

    @Override
    public void register(RegisterDTO registerDTO) {

    }

    @Override
    public TokenUserInfoDTO login(WebLoginDTO webLoginDTO) {
        return null;
    }

    @Override
    public Map<String, String> getCheckCode() {
        return Map.of();
    }
}
