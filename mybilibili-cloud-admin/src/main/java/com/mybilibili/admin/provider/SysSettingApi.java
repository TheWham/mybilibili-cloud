package com.mybilibili.admin.provider;

import com.mybilibili.admin.component.AdminRedisComponent;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统配置内部接口。
 *
 * <p>这个接口只面向服务间调用。配置读取链路统一收口到 admin，Redis 未命中时由
 * AdminRedisComponent 继续查库并初始化默认配置。</p>
 */
@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class SysSettingApi {

    @Resource
    private AdminRedisComponent adminRedisComponent;

    @GetMapping("/sysSetting")
    public SysSettingDTO getSysSetting() {
        return adminRedisComponent.getSysSetting();
    }
}
