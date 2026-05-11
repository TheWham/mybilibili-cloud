package com.mybilibili.admin.controller;

import com.mybilibili.admin.component.AdminRedisComponent;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setting")
public class SettingController extends ABaseController {

    @Resource
    private AdminRedisComponent adminRedisComponent;

    @RequestMapping("/getSetting")
    public ResponseVO getSetting() {
        return getSuccessResponseVO(adminRedisComponent.getSysSetting());
    }

    @RequestMapping("/saveSetting")
    public ResponseVO saveSetting(SysSettingDTO sysSettingDTO) {
        boolean success = adminRedisComponent.setSysSetting(sysSettingDTO);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return getSuccessResponseVO(null);
    }
}
