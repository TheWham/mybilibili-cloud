package com.mybilibili.base.entity.dto;

import java.io.Serializable;

/**
 * 视频分片转码任务数据。
 *
 * <p>media 和 video 之间只传任务数据，不共享 video 模块的 PO。
 * 字段名保持和原分片表一致，避免 Redis 队列中已有数据无法反序列化。</p>
 *
 * @author amani
 * @date 2026/02/09
 */
public class VideoInfoFilePostDTO implements Serializable {

    private String fileId;
    private String uploadId;
    private String userId;
    private String videoId;
    private String fileName;
    private Integer fileIndex;
    private Long fileSize;
    private String filePath;
    private Integer transferResult;
    private Integer updateType;
    private Integer duration;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(Integer fileIndex) {
        this.fileIndex = fileIndex;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getTransferResult() {
        return transferResult;
    }

    public void setTransferResult(Integer transferResult) {
        this.transferResult = transferResult;
    }

    public Integer getUpdateType() {
        return updateType;
    }

    public void setUpdateType(Integer updateType) {
        this.updateType = updateType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
