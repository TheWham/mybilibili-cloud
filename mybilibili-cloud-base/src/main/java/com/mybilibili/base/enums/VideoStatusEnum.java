package com.mybilibili.base.enums;

public enum VideoStatusEnum {

    STATUS_0(0, "转码中"),
    STATUS_1(1, "转码失败"),
    STATUS_2(2, "待审核"),
    STATUS_3(3, "审核通过"),
    STATUS_4(4, "审核不通过");


    private Integer status;
    private String desc;

    VideoStatusEnum(Integer status, String desc)
    {
        this.desc = desc;
        this.status = status;
    }
    public Integer getStatus(){return this.status;}

    public String getDesc(){
        return desc;
    }

    public static VideoStatusEnum getByStatus(Integer status) {
        for (VideoStatusEnum statusEnum : VideoStatusEnum.values())
        {
            if (statusEnum.getStatus().equals(status))
                return statusEnum;
        }
        return null;
    }
}
