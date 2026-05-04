package com.mybilibili.base.enums;

/**
 * 视频分片转码结果。
 */
public enum VideoFileTransferResultEnum {

    TRANSFER(0, "转码中"),
    SUCCESS(1, "转码成功"),
    FAILED(2, "转码失败");

    private final Integer status;
    private final String desc;

    VideoFileTransferResultEnum(Integer status, String desc) {
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
