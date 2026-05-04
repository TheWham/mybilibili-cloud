package com.mybilibili.base.enums;

import com.mybilibili.base.entity.dto.SysSettingDTO;

/**
 * 用户每日行为限制类型。
 */
public enum UserDailyLimitTypeEnum {

    POST_VIDEO("video", "发布视频") {
        @Override
        public Integer resolveLimit(SysSettingDTO sysSettingDTO) {
            return sysSettingDTO.getVideoCount();
        }
    },
    COMMENT("comment", "发表评论") {
        @Override
        public Integer resolveLimit(SysSettingDTO sysSettingDTO) {
            return sysSettingDTO.getCommentCount();
        }
    },
    DANMU("danmu", "发送弹幕") {
        @Override
        public Integer resolveLimit(SysSettingDTO sysSettingDTO) {
            return sysSettingDTO.getDanmuCount();
        }
    };

    private final String keySuffix;
    private final String desc;

    UserDailyLimitTypeEnum(String keySuffix, String desc) {
        this.keySuffix = keySuffix;
        this.desc = desc;
    }

    public abstract Integer resolveLimit(SysSettingDTO sysSettingDTO);

    public String getKeySuffix() {
        return keySuffix;
    }

    public String getDesc() {
        return desc;
    }
}
