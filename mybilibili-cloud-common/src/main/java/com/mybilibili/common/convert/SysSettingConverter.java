package com.mybilibili.common.convert;

import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.common.entity.po.SysSetting;

/**
 * 系统配置 PO/DTO 转换。
 *
 * <p>转换逻辑放在 common，是因为 SysSetting 表模型目前也在 common。
 * base 只保留 DTO 契约，不直接感知任何数据库表模型。</p>
 */
public final class SysSettingConverter {

    private SysSettingConverter() {
    }

    public static SysSettingDTO toDTO(SysSetting sysSetting) {
        SysSettingDTO sysSettingDTO = SysSettingDTO.createDefault();
        if (sysSetting == null) {
            return sysSettingDTO;
        }
        sysSettingDTO.setId(sysSetting.getId());
        sysSettingDTO.setUpdateTime(sysSetting.getUpdateTime());
        sysSettingDTO.setUpdateBy(sysSetting.getUpdateBy());
        if (sysSetting.getRegisterCoinCount() != null) {
            sysSettingDTO.setRegisterCoinCount(sysSetting.getRegisterCoinCount());
        }
        if (sysSetting.getPostVideoCoinCount() != null) {
            sysSettingDTO.setPostVideoCoinCount(sysSetting.getPostVideoCoinCount());
        }
        if (sysSetting.getVideoSize() != null) {
            sysSettingDTO.setVideoSize(sysSetting.getVideoSize());
        }
        if (sysSetting.getVideoPCount() != null) {
            sysSettingDTO.setVideoPCount(sysSetting.getVideoPCount());
        }
        if (sysSetting.getVideoCount() != null) {
            sysSettingDTO.setVideoCount(sysSetting.getVideoCount());
        }
        if (sysSetting.getCommentCount() != null) {
            sysSettingDTO.setCommentCount(sysSetting.getCommentCount());
        }
        if (sysSetting.getDanmuCount() != null) {
            sysSettingDTO.setDanmuCount(sysSetting.getDanmuCount());
        }
        return sysSettingDTO;
    }

    public static SysSetting toPO(SysSettingDTO sysSettingDTO) {
        SysSettingDTO source = sysSettingDTO == null ? SysSettingDTO.createDefault() : sysSettingDTO;
        SysSetting sysSetting = new SysSetting();
        sysSetting.setId(source.getId());
        sysSetting.setRegisterCoinCount(source.getRegisterCoinCount());
        sysSetting.setPostVideoCoinCount(source.getPostVideoCoinCount());
        sysSetting.setVideoSize(source.getVideoSize());
        sysSetting.setVideoPCount(source.getVideoPCount());
        sysSetting.setVideoCount(source.getVideoCount());
        sysSetting.setCommentCount(source.getCommentCount());
        sysSetting.setDanmuCount(source.getDanmuCount());
        sysSetting.setUpdateTime(source.getUpdateTime());
        sysSetting.setUpdateBy(source.getUpdateBy());
        return sysSetting;
    }
}
