package com.mybilibili.user.provider;


import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class UserInfoApi {

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/getUserInfoByIds")
    public List<UserInfoDTO> getUserInfoByIds(@RequestParam("userIds") List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setUserIds(userIds);
        List<UserInfo> userInfoList = userInfoService.findListByParam(userInfoQuery);
        return BeanUtil.copyToList(userInfoList, UserInfoDTO.class);
    }

    @RequestMapping("/message/selecUserInfoList")
    public List<UserInfoDTO> selecUserInfoList(@RequestBody UserInfoQuery userInfoQuery)
    {
        List<UserInfo> userInfoList = userInfoService.findListByParam(userInfoQuery);
        List<UserInfoDTO> userInfoDTOS = BeanUtil.copyToList(userInfoList, UserInfoDTO.class);
        return userInfoDTOS;
    }


}

