package com.mybilibili.admin.convert;

import com.mybilibili.admin.entity.po.SysSetting;
import com.mybilibili.base.entity.dto.SysSettingDTO;

/**
 * 系统配置 PO/DTO 转换。
 *
 * <p>转换逻辑跟随 sys_setting 表模型放在 admin。跨服务只传 DTO，
 * 避免把后台配置表结构扩散到业务服务。</p>
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
