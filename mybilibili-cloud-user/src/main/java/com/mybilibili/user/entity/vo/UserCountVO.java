package com.mybilibili.user.entity.vo;


import lombok.Data;

@Data
public class UserCountVO {
    // 关注数
    private Integer focusCount;
    // 粉丝数
    private Integer fansCount;
    //硬币数
    private Integer currentCoinCount;
    private Integer likeCount;
    private Integer playCount;
}
