package com.mybilibili.base.entity.dto;

import java.io.Serializable;

/**
 * 视频字幕向量化任务。
 *
 * <p>video 只负责投递任务数据，后续由 ai 或 Python Worker 消费。</p>
 */
public class AiSubtitleIndexTaskDTO implements Serializable {

    private String videoId;
    private String userId;
    private String videoName;
    private String videoCover;
    private String tags;
    private String fileId;
    private Integer fileIndex;
    private String sourceVideoPath;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(Integer fileIndex) {
        this.fileIndex = fileIndex;
    }

    public String getSourceVideoPath() {
        return sourceVideoPath;
    }

    public void setSourceVideoPath(String sourceVideoPath) {
        this.sourceVideoPath = sourceVideoPath;
    }
}
