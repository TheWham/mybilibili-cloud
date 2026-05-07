package com.mybilibili.message.enums;

/**
 * 用户消息类型。
 */
public enum MessageTypeEnum {

    SYSTEM(1, "系统通知"),
    LIKE(2, "收到的赞"),
    COLLECT(3, "收到收藏"),
    COMMENT(4, "评论和@");

    private final Integer type;
    private final String desc;

    MessageTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static MessageTypeEnum getEnum(Integer type)
    {
        for (MessageTypeEnum messageTypeEnum : MessageTypeEnum.values())
            if (messageTypeEnum.getType().equals(type))
                return messageTypeEnum;
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
