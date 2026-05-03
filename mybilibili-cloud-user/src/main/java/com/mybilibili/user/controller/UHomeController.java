package com.mybilibili.user.controller;

import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户主页接口。
 *
 * <p>这里先恢复用户资料读取。主页视频、收藏和系列接口后面会分别接 video/interact，
 * 当前 controller 不直接引入其他业务服务，避免拆分初期又形成模块互相依赖。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Validated
@RestController
@RequestMapping("uhome")
public class UHomeController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 获取用户主页资料。
     *
     * @param userId 被查看主页的用户 ID
     * @return 用户公开资料和统计数据
     */
    @RequestMapping("getUserInfo")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        TokenUserInfoDTO currentUser = getTokenUserInfo();
        return getSuccessResponseVO(userInfoService.getUHomeUserInfo(userId, currentUser));
    }
}
