package com.mybilibili.user.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.user.component.UserRedisComponent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台系统配置读取入口。
 *
 * <p>当前阶段系统配置仍然复用 common 里的基础服务，前台只开放只读查询。
 * 后续后台配置修改完成后，再把刷新缓存的动作放回 admin 服务。</p>
 *
 * @author amani
 * @since 2026/05/03
 */
@RestController
@RequestMapping("sysSetting")
public class SysSettingController extends ABaseController {

    @Resource
    private UserRedisComponent userRedisComponent;

    /**
     * 获取前台运行所需的系统配置。
     */
    @RequestMapping("getSetting")
    public ResponseVO getSetting() {
        return getSuccessResponseVO(userRedisComponent.getSysSetting());
    }
}
