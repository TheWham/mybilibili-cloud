package com.mybilibili.base.enums;

public enum AdminStatsTypeEnum {
    LIKE_COUNT(0, "likeCount", "点赞量"),
    PLAY_COUNT(1, "playCount", "播放量"),
    DANMU_COUNT(2, "danmuCount", "弹幕量"),
    COIN_COUNT(3, "coinCount", "投币量"),
    USER_COUNT(4, "userCount", "用户数"),
    COMMENT_COUNT(5, "commentCount", "评论量"),
    COLLECT_COUNT(6, "collectCount", "收藏量");

    private final Integer type;
    private final String field;
    private final String desc;

    AdminStatsTypeEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    public static AdminStatsTypeEnum getEnum(Integer type) {
        for (AdminStatsTypeEnum statsTypeEnum : AdminStatsTypeEnum.values()) {
            if (statsTypeEnum.getType().equals(type)) {
                return statsTypeEnum;
            }
        }
        return null;
    }

    public Integer getType() { return type; }
    public String getField() { return field; }
    public String getDesc() { return desc; }
}
