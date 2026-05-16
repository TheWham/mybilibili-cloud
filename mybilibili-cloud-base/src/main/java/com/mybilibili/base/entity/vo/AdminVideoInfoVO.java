package com.mybilibili.base.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.AiSubtitleIndexStatusEnum;
import com.mybilibili.base.enums.VideoStatusEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 后台视频列表展示数据。
 *
 * <p>这个 VO 是 admin 和 video 之间的稳定返回契约，admin 不直接依赖
 * video 模块的投稿 PO，后续 video 表结构调整时影响面会小很多。</p>
 */
public class AdminVideoInfoVO implements Serializable {

    private String videoId;
    private String videoCover;
    private String videoName;
    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

    private Integer pCategoryId;
    private Integer categoryId;
    private Integer status;
    private String statusName;
    private Integer postType;
    private String originInfo;
    private String tags;
    private String introduction;
    private String interaction;
    private Integer duration;
    private Integer playCount;
    private Integer likeCount;
    private Integer coinCount;
    private Integer danmuCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer recommendType;
    private String nickName;
    private String avatar;
    private String aiSubtitleIndexStatus;
    private String aiSubtitleIndexStatusName;

    public String getStatusName() {
        if (statusName != null) {
            return statusName;
        }
        VideoStatusEnum videoStatus = VideoStatusEnum.getByStatus(status);
        return videoStatus == null ? "" : videoStatus.getDesc();
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getpCategoryId() {
        return pCategoryId;
    }

    public void setpCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public Integer getPCategoryId() {
        return pCategoryId;
    }

    public void setPCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPostType() {
        return postType;
    }

    public void setPostType(Integer postType) {
        this.postType = postType;
    }

    public String getOriginInfo() {
        return originInfo;
    }

    public void setOriginInfo(String originInfo) {
        this.originInfo = originInfo;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getInteraction() {
        return interaction;
    }

    public void setInteraction(String interaction) {
        this.interaction = interaction;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(Integer coinCount) {
        this.coinCount = coinCount;
    }

    public Integer getDanmuCount() {
        return danmuCount;
    }

    public void setDanmuCount(Integer danmuCount) {
        this.danmuCount = danmuCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public Integer getRecommendType() {
        return recommendType;
    }

    public void setRecommendType(Integer recommendType) {
        this.recommendType = recommendType;
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

    public String getAiSubtitleIndexStatus() {
        return aiSubtitleIndexStatus;
    }

    public void setAiSubtitleIndexStatus(String aiSubtitleIndexStatus) {
        this.aiSubtitleIndexStatus = aiSubtitleIndexStatus;
    }

    public String getAiSubtitleIndexStatusName() {
        if (aiSubtitleIndexStatusName != null) {
            return aiSubtitleIndexStatusName;
        }
        return AiSubtitleIndexStatusEnum.getDescByStatus(aiSubtitleIndexStatus);
    }

    public void setAiSubtitleIndexStatusName(String aiSubtitleIndexStatusName) {
        this.aiSubtitleIndexStatusName = aiSubtitleIndexStatusName;
    }
}
