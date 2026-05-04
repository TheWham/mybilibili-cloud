package com.mybilibili.video.entity.dto;

public class VideoCountDTO {
    private Integer totalLikeCount;
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
