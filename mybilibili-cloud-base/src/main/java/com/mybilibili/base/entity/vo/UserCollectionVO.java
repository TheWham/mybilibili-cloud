package com.mybilibili.base.entity.vo;

import lombok.Data;

import java.util.Date;

/**
 * 用户收藏视频信息
 * @since 2026.3.22
 * @author amani
 */

@Data
public class UserCollectionVO {
    private String videoId;
    private String videoCover;
    private String videoName;
    private Date actionTime;

}
