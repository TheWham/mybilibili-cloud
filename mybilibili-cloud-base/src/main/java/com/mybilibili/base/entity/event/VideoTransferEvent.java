package com.mybilibili.base.entity.event;

import java.io.Serializable;

/**
 * 视频转码事件。
 *
 * <p>投稿主流程只负责把分 P 记录落库，真正的文件合并、转码、切片交给 MQ 异步执行，
 * 这样可以避免请求线程长时间阻塞。</p>
 *
 * @author amani
 * @since 2026/05/15
 */
public class VideoTransferEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一 id，用来处理 MQ 重投时的消费幂等。
     */
    private String eventId;

    /**
     * 投稿分 P 文件 id。
     */
    private String fileId;

    /**
     * 当前上传任务 id。
     */
    private String uploadId;

    /**
     * 所属视频 id。
     */
    private String videoId;

    /**
     * 投稿用户 id。
     */
    private String userId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

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
}
