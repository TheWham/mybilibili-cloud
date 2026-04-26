package com.mybilibili.base.enums;


/**
 * @author 86150
 */

public enum UserActionTypeEnum {


    // 评论点赞数量枚举值
    COMMENT_LIKE(0, "like_count", "评论点赞数量"),
    // 评论讨厌枚举值
    COMMENT_HATE(1, "hate_count", "评论讨厌"),
    // 视频点赞枚举值
    VIDEO_LIKE(2, "like_count", "视频点赞"),
    // 视频收藏枚举值
    VIDEO_COLLECT(3, "collect_count", "视频收藏"),
    // 视频投币枚举值
    VIDEO_COIN(4, "coin_count", "视频投币"),
    // 评论数量枚举值
    VIDEO_COMMENT(5, "comment_count", "评论数量"),
    // 弹幕数量枚举值
    VIDEO_DNAMU(6,"danmu_count", "弹幕数量"),
    // 视频播放量枚举值
    VIDEO_PLAY(7, "play_count", "视频播放量"),
    //关注数量枚举
    USER_FOCUS(8, "focus_count", "用户关注数量"),
    //粉丝数量枚举
    USER_FANS(9, "fans_count", "用户粉丝数量"),
    // 用户硬币数量枚举值
    USER_COIN(10, "current_coin_count", "用户硬币数量");
    // 用户行为类型
    private Integer type;
    // 对应数据库字段名
    private String field;
    // 行为描述
    private String desc;

    // 构造方法，初始化用户行为类型
    UserActionTypeEnum(Integer type, String field, String desc) {
        this.type = type;
        this.field = field;
        this.desc = desc;
    }

    /**
     * 根据类型获取对应的枚举值
     * @param type 用户行为类型
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static UserActionTypeEnum getEnum(Integer type)
    {
        // 遍历所有枚举值
        for(UserActionTypeEnum typeEnum : UserActionTypeEnum.values())
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
