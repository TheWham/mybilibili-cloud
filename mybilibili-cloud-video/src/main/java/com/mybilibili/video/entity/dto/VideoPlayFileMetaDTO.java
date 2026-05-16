package com.mybilibili.video.entity.dto;

import java.io.Serializable;

/**
 * 播放上报需要的分 P 元数据。
 *
 * <p>播放上报入口只接收 fileId，服务端需要据此找到视频、分 P 下标和作者。
 * 这些字段来自已发布的正式分 P 表，可以放心缓存一小段时间，避免每次上报都打到数据库。</p>
 *
 * @author amani
 * @since 2026/05/15
 */
public class VideoPlayFileMetaDTO implements Serializable {

    /**
     * 分 P 文件 id。
     */
    private String fileId;

    /**
     * 视频 id，播放量按视频维度统计。
     */
    private String videoId;

    /**
     * 视频作者 id，用于刷新作者侧播放统计。
     */
    private String videoUserId;

    /**
     * 分 P 下标，用于写入观看历史时恢复用户最后看到的分 P。
     */
    private Integer fileIndex;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoUserId() {
        return videoUserId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public Integer getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(Integer fileIndex) {
        this.fileIndex = fileIndex;
    }
}
