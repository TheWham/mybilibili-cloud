package com.mybilibili.video.entity.enums;

/**
 * @author amani
 * @since 2026.3.18
 */
public enum VideoOrderTypeEnum {

    //按照视频最新发布排序
    ORDER_POST_TIME(0, "v.create_time","最新发布"),
    //按照视频最多播放排序
    ORDER_PLAY_COUNT(1, "v.play_count","最多播放"),
    //按照视频最多收藏排序
    ORDER_COLLECT_COUNT(2,"v.collect_count","最多收藏");

    VideoOrderTypeEnum(Integer status, String field, String desc) {
        this.status = status;
        this.desc = desc;
        this.field = field;
    }

    private Integer status;
    private String field;
    private String desc;


    public static VideoOrderTypeEnum getEnum(Integer type)
    {
        for (VideoOrderTypeEnum typeEnum : VideoOrderTypeEnum.values())
        {
            if (typeEnum.status.equals(type))
                return typeEnum;
        }
        return null;
    }

    public String getField() {
        return field;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
