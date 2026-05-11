package com.mybilibili.base.enums;

import java.util.Objects;

/**
 * 视频搜索排序类型。
 *
 * <p>field 是 ES 索引字段名，只允许在 search 服务内部使用。其他服务通过 status
 * 表达业务意图，不能直接传字段名。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public enum SearchOrderTypeEnum {

    VIDEO_PLAY(0, "playCount", "视频播放量"),
    VIDEO_TIME(1, "createTime", "视频发布时间"),
    VIDEO_DANMU(2, "danmuCount", "视频弹幕数"),
    VIDEO_COLLECT(3, "collectCount", "视频收藏量");

    private final Integer status;
    private final String field;
    private final String desc;

    SearchOrderTypeEnum(Integer status, String field, String desc) {
        this.status = status;
        this.field = field;
        this.desc = desc;
    }

    public static SearchOrderTypeEnum getEnum(Integer status) {
        for (SearchOrderTypeEnum item : SearchOrderTypeEnum.values()) {
            if (Objects.equals(item.getStatus(), status)) {
                return item;
            }
        }
        return null;
    }

    public static SearchOrderTypeEnum getEnums(Integer status) {
        return getEnum(status);
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
