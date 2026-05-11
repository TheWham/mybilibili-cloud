package com.mybilibili.search.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * user 服务用户展示信息客户端。
 */
@FeignClient(contextId = "searchUserInfoClient", name = Constants.CLOUD_USER_NAME)
public interface UserInfoClient {

    /**
     * 批量查询用户基础展示信息，搜索结果只补昵称和头像。
     *
     * @param userIds 用户 id 列表
     * @return 用户展示信息列表
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/getUserInfoByIds")
    List<UserInfoDTO> getUserInfoByIds(@RequestParam("userIds") List<String> userIds);
}
