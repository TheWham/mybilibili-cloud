package com.mybilibili.video.entity.enums;

import java.util.Objects;

public enum SearchOrderTypeEnum {
    VIDEO_PLAY(0, "playCount", "视频播放量"),
    VIDEO_TIME(1, "createTime", "视频时间"),
    VIDEO_DANMU(2, "danmuCount", "视频弹幕数"),
    VIDEO_COLLECT(3, "collectCount", "视频收藏量");

    private Integer status;
    private String field;
    private String desc;

    SearchOrderTypeEnum(Integer status, String field, String desc) {
        this.status = status;
        this.field = field;
        this.desc = desc;
    }

    public static SearchOrderTypeEnum getEnums(Integer status) {
        for (SearchOrderTypeEnum searchOrderTypeEnum : SearchOrderTypeEnum.values())
        {
            if (Objects.equals(searchOrderTypeEnum.getStatus(), status))
                return searchOrderTypeEnum;
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }

    public String getDesc() {
        return desc;
    }
}
