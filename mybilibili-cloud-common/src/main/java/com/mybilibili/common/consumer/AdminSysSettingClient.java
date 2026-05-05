package com.mybilibili.common.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * admin 系统配置读取客户端。
 *
 * <p>sys_setting 的数据库兜底只允许在 admin 内部发生。业务服务通过这个 Client
 * 获取配置，避免重新依赖配置表、Mapper 或 Service。</p>
 */
@FeignClient(Constants.CLOUD_ADMIN_NAME)
public interface AdminSysSettingClient {

    /**
     * 获取当前系统配置。
     *
     * @return 系统配置 DTO，admin 侧负责 Redis miss 后的查库和默认值初始化
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/sysSetting")
    SysSettingDTO getSysSetting();
}
