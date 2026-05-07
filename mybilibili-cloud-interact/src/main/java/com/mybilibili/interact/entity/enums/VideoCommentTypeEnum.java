package com.mybilibili.interact.entity.enums;

/**
 * 评论置顶状态。
 */
public enum VideoCommentTypeEnum {

    NO_TOP(0, "未置顶"),
    TOP(1, "置顶");

    private final Integer type;
    private final String desc;

    VideoCommentTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static VideoCommentTypeEnum getEnum(Integer type) {
        for (VideoCommentTypeEnum typeEnum : VideoCommentTypeEnum.values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
