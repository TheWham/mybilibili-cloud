package com.mybilibili.base.entity.dto;

import lombok.Data;

@Data
public class UserInfoDTO {
    private String nickName;
    private String avatar;
    private String userId;
    private Integer currentCoinCount;
}
