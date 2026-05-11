package com.mybilibili.base.entity.dto;

import java.io.Serializable;

/**
 * UP 主视频维度统计。
 *
 * <p>这个 DTO 是 video 服务对外的内部契约，放在 base 中，避免 user 模块
 * 为了读取播放量、点赞量反向依赖 video 模块的内部类。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class VideoCountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * UP 主所有公开视频收到的点赞数。
     */
    private Integer totalLikeCount;

    /**
     * UP 主所有公开视频收到的播放数。
     */
    private Integer totalPlayCount;

    public Integer getTotalLikeCount() {
        return totalLikeCount;
    }

    public void setTotalLikeCount(Integer totalLikeCount) {
        this.totalLikeCount = totalLikeCount;
    }

    public Integer getTotalPlayCount() {
        return totalPlayCount;
    }

    public void setTotalPlayCount(Integer totalPlayCount) {
        this.totalPlayCount = totalPlayCount;
    }
}
