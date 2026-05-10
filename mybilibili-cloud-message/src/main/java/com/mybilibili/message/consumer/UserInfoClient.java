package com.mybilibili.message.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(contextId = "messageUserInfoClient", name = Constants.CLOUD_USER_NAME)
public interface UserInfoClient {

    @GetMapping(Constants.INNER_API_PREFIX + "/message/selecUserInfoList")
    List<UserInfoDTO> selecUserInfoList(@RequestBody UserInfoQuery userInfoQuery);

}
