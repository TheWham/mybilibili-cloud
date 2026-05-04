package com.mybilibili.base.enums;

/**
 * 视频文件是否有更新。
 */
public enum VideoFileUpdateTypeEnum {

    UPDATE(1, "更新"),
    UN_UPDATE(0, "未更新");

    private final Integer status;
    private final String desc;

    VideoFileUpdateTypeEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
