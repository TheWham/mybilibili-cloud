package com.mybilibili.message.entity.dto;

/**
 * Redis 播放历史裁剪事件。
 * 当某个视频因为“最近 1000 条”规则被淘汰时，把 userId 和 videoId 放进删除队列，
 * 后面的定时任务再异步删数据库里的旧记录。
 */
public class VideoHistoryDeleteDTO {
    private String userId;
    private String videoId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
