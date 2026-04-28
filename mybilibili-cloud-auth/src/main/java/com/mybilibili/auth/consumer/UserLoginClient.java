package com.mybilibili.auth.consumer;

import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = Constants.CLOUD_USER_NAME)
public interface UserLoginClient {

    @RequestMapping(Constants.INNER_API_PREFIX + "/changeStatus")
    Integer changeStatus(@RequestParam String userId, @RequestParam Integer type);

    @RequestMapping(Constants.INNER_API_PREFIX + "/register")
    void register(RegisterDTO registerDTO);

    @RequestMapping(Constants.INNER_API_PREFIX + "/login")
    TokenUserInfoDTO login(WebLoginDTO webLoginDTO);

    @RequestMapping(Constants.INNER_API_PREFIX + "/autoLogin")
    void refreshRealtimeUserStatsCache();
}
