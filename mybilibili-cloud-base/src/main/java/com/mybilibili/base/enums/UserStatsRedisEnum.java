package com.mybilibili.base.enums;

/**
 * @since 2026.3.22
 * @author amani
 */
public enum UserStatsRedisEnum {
    // 视频点赞枚举值
    VIDEO_LIKE(0, "likeCount", "视频点赞"),
    // 视频播放量枚举值
    VIDEO_PLAY(1, "playCount", "视频播放量"),
    VIDEO_DANMU(2, "danmuCount", "弹幕数量"),
    VIDEO_COIN(3, "coinCount","投币数量"),
    //关注数量枚举
    USER_FOCUS(4, "focusCount", "用户关注数量"),
    //粉丝数量枚举
    USER_FANS(5, "fansCount", "用户粉丝数量"),
    // 用户硬币数量枚举值
    USER_COIN(6, "currentCoinCount", "用户硬币数量"),
    USER_COMMENT_COUNT(7, "commentCount", "用户评论量"),
    USER_COLLECT_COUNT(8,"collectCount", "用户收藏量");

    // 用户行为类型
    private Integer type;
    // 对应数据库字段名
    private String field;
    // 行为描述
    private String desc;

    // 构造方法，初始化用户行为类型
    UserStatsRedisEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    /**
     * 根据类型获取对应的枚举值
     * @param type 用户行为类型
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static UserStatsRedisEnum getEnum(Integer type)
    {
        // 遍历所有枚举值
        for(UserStatsRedisEnum typeEnum : UserStatsRedisEnum.values())
        {
            // 如果找到匹配的类型，返回对应的枚举值
            if (typeEnum.getType().equals(type))
                return typeEnum;
        }
        return null;
    }

    /**
     * 获取用户行为类型
     * @return 用户行为类型
     */
    public Integer getType() {
        return type;
    }

    /**
     * 设置用户行为类型
     * @param type 用户行为类型
     */
    public void setType(Integer type) {
        this.type = type;
    }

    /**
     * 获取对应数据库字段名
     * @return 数据库字段名
     */
    public String getField() {
        return field;
    }

    /**
     * 设置对应数据库字段名
     * @param field 数据库字段名
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * 获取行为描述
     * @return 行为描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 设置行为描述
     * @param desc 行为描述
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
