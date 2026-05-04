package com.mybilibili.base.enums;

/**
 * 视频推荐状态。
 */
public enum VideoRecommendEnum {

    NO_RECOMMEND(0, "未推荐"),
    RECOMMEND(1, "已推荐");

    private final Integer status;
    private final String desc;

    VideoRecommendEnum(Integer status, String desc) {
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
