package com.mybilibili.base.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频搜索结果展示对象。
 *
 * <p>搜索页只需要展示视频基础信息、作者展示信息和排序相关计数，
 * 不把 video_info 表里的投稿来源、互动配置等内部字段暴露出去。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class VideoSearchResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频 id。
     */
    private String videoId;

    /**
     * 视频标题，开启高亮时这里会带高亮标签。
     */
    private String videoName;

    /**
     * 视频封面。
     */
    private String videoCover;

    /**
     * 视频作者 id。
     */
    private String userId;

    /**
     * 视频作者昵称。
     */
    private String nickName;

    /**
     * 视频作者头像。
     */
    private String avatar;

    /**
     * 视频发布时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 播放数。
     */
    private Integer playCount;

    /**
     * 弹幕数。
     */
    private Integer danmuCount;

    /**
     * 收藏数。
     */
    private Integer collectCount;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getDanmuCount() {
        return danmuCount;
    }

    public void setDanmuCount(Integer danmuCount) {
        this.danmuCount = danmuCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }
}
