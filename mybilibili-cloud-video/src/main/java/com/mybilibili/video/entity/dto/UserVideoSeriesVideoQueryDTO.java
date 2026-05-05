package com.mybilibili.video.entity.dto;

import java.util.Date;

/**
 * 用户主页合集视频查询结果。
 *
 * <p>这个对象只承接 video 模块 Mapper 的多表查询结果，不作为跨服务契约使用。</p>
 */
public class UserVideoSeriesVideoQueryDTO {

    private Integer seriesId;
    private String videoId;
    private String userId;
    private Integer sort;
    private String videoName;
    private String videoCover;
    private Integer playCount;
    private Date createTime;

    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
