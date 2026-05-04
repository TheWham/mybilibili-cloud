package com.mybilibili.video.entity.vo;

import java.util.Date;

/**
 * @author amani
 * @since 2026.4.15
 * 视频历史记录信息
 */
public class VideoHistoryVO {
    private String videoId;
    private String videoCover;
    private String videoName;
    private Date lastUpdateTime;

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
