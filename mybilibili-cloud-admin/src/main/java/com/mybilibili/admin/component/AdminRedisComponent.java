package com.mybilibili.admin.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.admin.constants.AdminRedisKeys;
import com.mybilibili.admin.convert.SysSettingConverter;
import com.mybilibili.admin.entity.po.SysSetting;
import com.mybilibili.admin.services.SysSettingService;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.admin.entity.po.CategoryInfo;
import com.mybilibili.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 后台管理缓存。
 *
 * <p>分类树和系统配置由后台维护，缓存刷新也应从 admin 侧发起。</p>
 */
@Component
public class AdminRedisComponent {

    private static final long SYS_SETTING_ID = 1L;

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SysSettingService sysSettingService;

    public void saveCategoryList2Redis(List<CategoryInfo> categoryList) {
        redisUtils.set(AdminRedisKeys.CATEGORY_KEY, categoryList);
    }

    public List<CategoryInfo> getCategoryList() {
        Object value = redisUtils.get(AdminRedisKeys.CATEGORY_KEY);
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof CategoryInfo)) {
            return (List<CategoryInfo>) list;
        }
        return JSON.parseArray(JSON.toJSONString(value), CategoryInfo.class);
    }

    public String saveCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT;
        redisUtils.setex(AdminRedisKeys.CHECK_CODE_KEY + checkCodeKey, code, expireTime);
        return checkCodeKey;
    }

    public String getCode(String checkCodeKey) {
        Object value = redisUtils.get(AdminRedisKeys.CHECK_CODE_KEY + checkCodeKey);
        if (Objects.isNull(value)) {
            return null;
        }
        return value.toString();
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(AdminRedisKeys.CHECK_CODE_KEY + checkCodeKey);
    }

    public SysSettingDTO getSysSetting() {
        Object sysSetting = redisUtils.get(AdminRedisKeys.SYS_SETTING_KEY);
        if (sysSetting instanceof SysSettingDTO dto) {
            return dto;
        }
        if (sysSetting != null) {
            return JSON.parseObject(JSON.toJSONString(sysSetting), SysSettingDTO.class);
        }

        SysSetting sysSettingDb = sysSettingService.getSysSettingById(SYS_SETTING_ID);
        if (sysSettingDb == null) {
            // sys_setting 是单行全局配置，空表时先落默认值，后续统一围绕 id=1 读写。
            SysSetting initSetting = SysSettingConverter.toPO(SysSettingDTO.createDefault());
            initSetting.setId(SYS_SETTING_ID);
            sysSettingService.add(initSetting);
            sysSettingDb = sysSettingService.getSysSettingById(SYS_SETTING_ID);
        }
        SysSettingDTO sysSettingDTO = SysSettingConverter.toDTO(sysSettingDb);
        redisUtils.set(AdminRedisKeys.SYS_SETTING_KEY, sysSettingDTO);
        return sysSettingDTO;
    }

    public boolean setSysSetting(SysSettingDTO sysSettingDTO) {
        SysSetting sysSetting = SysSettingConverter.toPO(sysSettingDTO);
        sysSetting.setId(SYS_SETTING_ID);

        SysSetting currentSetting = sysSettingService.getSysSettingById(SYS_SETTING_ID);
        Integer effectRows = currentSetting == null
                ? sysSettingService.add(sysSetting)
                : sysSettingService.updateSysSettingById(sysSetting, SYS_SETTING_ID);
        if (effectRows == null || effectRows == 0) {
            return false;
        }
        return redisUtils.set(AdminRedisKeys.SYS_SETTING_KEY, sysSettingDTO);
    }
}
