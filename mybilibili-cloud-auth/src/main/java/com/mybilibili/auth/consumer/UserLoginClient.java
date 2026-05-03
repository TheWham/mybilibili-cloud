package com.mybilibili.auth.consumer;

import com.mybilibili.base.entity.dto.RegisterDTO;
import com.mybilibili.base.entity.dto.WebLoginDTO;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.UserCountVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = Constants.CLOUD_USER_NAME)
public interface UserLoginClient {

    @RequestMapping(Constants.INNER_API_PREFIX + "/changeStatus")
    Integer changeStatus(@RequestParam("userId") String userId, @RequestParam("type") Integer type);

    @RequestMapping(Constants.INNER_API_PREFIX + "/register")
    void register(@RequestBody RegisterDTO registerDTO);

    @RequestMapping(Constants.INNER_API_PREFIX + "/login")
    TokenUserInfoDTO login(@RequestBody WebLoginDTO webLoginDTO);

    @RequestMapping(Constants.INNER_API_PREFIX + "/autoLogin")
    void refreshRealtimeUserStatsCache(@RequestParam("userId") String userId);

    @RequestMapping(Constants.INNER_API_PREFIX + "/getUserCountInfo")
    UserCountVO getUserCountInfo(@RequestParam("userId") String userId);
}
