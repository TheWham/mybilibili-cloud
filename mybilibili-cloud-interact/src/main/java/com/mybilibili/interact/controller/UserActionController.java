package com.mybilibili.interact.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.interact.entity.dto.UserActionDTO;
import com.mybilibili.interact.services.UserActionCommandService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户互动接口。
 *
 * @author amani
 * @since 2026/05/11
 */
@RestController
@RequestMapping("userAction")
public class UserActionController extends ABaseController {

    @Resource
    private UserActionCommandService userActionCommandService;

    @RequestMapping("/doAction")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO doAction(@Validated UserActionDTO userActionDTO) {
        userActionCommandService.doAction(userActionDTO, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }
}
