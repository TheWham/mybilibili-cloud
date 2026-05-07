package com.mybilibili.interact.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(Constants.CLOUD_USER_NAME)
public interface UserInfoClient {

    /**
     * 根据评论 userIds 批量查询用户信息。
     *
     * @param userIds 批量查询用户信息
     * @return 用户信息集合
     */
    @RequestMapping(Constants.INNER_API_PREFIX + "/getUserInfoByIds")
    List<UserInfoDTO> getUserInfoByIds(@RequestParam("userIds") List<String> userIds);
}
