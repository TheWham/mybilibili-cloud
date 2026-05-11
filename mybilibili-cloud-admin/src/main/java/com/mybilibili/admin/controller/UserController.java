package com.mybilibili.admin.controller;

import com.mybilibili.admin.consumer.AdminUserClient;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/user")
public class UserController extends ABaseController {

    @Resource
    private AdminUserClient adminUserClient;

    @RequestMapping("/loadUser")
    public ResponseVO loadUser(Integer pageNo, Integer pageSize, String nickNameFuzzy, Integer status) {
        return getSuccessResponseVO(adminUserClient.loadUser(pageNo, pageSize, nickNameFuzzy, status));
    }

    @RequestMapping("/changeStatus")
    public ResponseVO changeStatus(@NotBlank String userId, @NotNull Integer status) {
        adminUserClient.changeStatus(userId, status);
        return getSuccessResponseVO(null);
    }
}
