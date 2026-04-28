package com.mybilibili.user.enums;


import com.mybilibili.base.constants.Constants;

/**
 * @version 2026.1.8
 * @author amani
 */
public enum StatusEnum {
    DISABLE(Constants.ZERO, "禁用"),
    NORMAL(Constants.ONE, "正常");

    private Integer type;
    private String status;

    StatusEnum(int number, String status) {
        this.type = number;
        this.status = status;
    }


    public static StatusEnum getEnum(Integer type)
    {
        for (StatusEnum statusEnum : StatusEnum.values())
        {
            if (statusEnum.getType().equals(type))
                return statusEnum;
        }
        return null;
    }

    public Integer getType() {
        return type;
    }


    public String getStatus() {
        return status;
    }

}
