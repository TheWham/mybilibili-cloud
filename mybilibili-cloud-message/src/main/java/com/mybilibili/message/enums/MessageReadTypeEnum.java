package com.mybilibili.message.enums;

public enum MessageReadTypeEnum {

    NO_READ(0, "未读"),
    READ(1, "已读");

    private Integer type;
    private String desc;

    MessageReadTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageReadTypeEnum getByStatus(Integer type)
    {
        for (MessageReadTypeEnum messageReadTypeEnum : MessageReadTypeEnum.values())
        {
            if (messageReadTypeEnum.getType().equals(type))
                return messageReadTypeEnum;
        }
        return null;
    }

}
