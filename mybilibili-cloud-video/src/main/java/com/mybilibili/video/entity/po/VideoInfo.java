package com.mybilibili.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息
 */
public class VideoInfo implements Serializable {
	/**
	 * @description 视频ID
	 */
    private String videoId;

	/**
	 * @description 文件名称
	 */
    private String videoName;

	/**
	 * @description 视频封面
	 */
    private String videoCover;

	/**
	 * @description 用户id
	 */
    private String userId;

	/**
	 * @description 创建时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

	/**
	 * @description 最后更新时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

	/**
	 * @description 父级分类ID
	 */
    private Integer pCategoryId;

	/**
	 * @description 分类ID
	 */
    private Integer categoryId;

	/**
	 * @description 0:自制作 1:转载
	 */
    private Integer postType;

	/**
	 * @description 原资源说明
	 */
    private String originInfo;

	/**
	 * @description 视频标签
	 */
    private String tags;

	/**
	 * @description 视频简介
	 */
    private String introduction;

	/**
	 * @description 持续时间(秒)
	 */
    private Integer duration;

	/**
	 * @description 互动设置
	 */
    private String interaction;

	/**
	 * @description 播放数量
	 */
    private Integer playCount;

	/**
	 * @description 点赞数量
	 */
    private Integer likeCount;

	/**
	 * @description 投币数量
	 */
    private Integer coinCount;

	/**
	 * @description 弹幕数量
	 */
    private Integer danmuCount;

	/**
	 * @description 评论数量
	 */
    private Integer commentCount;

	/**
	 * @description 收藏数量
	 */
    private Integer collectCount;

	/**
	 * @description 是否推荐 0:未推荐 1:已推荐
	 */
    private Integer recommendType;

	/**
	 * @description 最后播放时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastPlayTime;

	/**
	 * 用户昵称
	 */
	private String nickName;

	/**
	 * 用户头像
	 */
	private String avatar;


	public Integer getpCategoryId() {
		return pCategoryId;
	}

	public void setpCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
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

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}
	public String getVideoName() {
		return this.videoName;
	}
	public void setVideoCover(String videoCover) {
		this.videoCover = videoCover;
	}
	public String getVideoCover() {
		return this.videoCover;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getCreateTime() {
		return this.createTime;
	}
	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public Date getLastUpdateTime() {
		return this.lastUpdateTime;
	}
	public void setPCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
	}
	public Integer getPCategoryId() {
		return this.pCategoryId;
	}
	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	public Integer getCategoryId() {
		return this.categoryId;
	}
	public void setPostType(Integer postType) {
		this.postType = postType;
	}
	public Integer getPostType() {
		return this.postType;
	}
	public void setOriginInfo(String originInfo) {
		this.originInfo = originInfo;
	}
	public String getOriginInfo() {
		return this.originInfo;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getTags() {
		return this.tags;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public String getIntroduction() {
		return this.introduction;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Integer getDuration() {
		return this.duration;
	}
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}
	public String getInteraction() {
		return this.interaction;
	}
	public void setPlayCount(Integer playCount) {
		this.playCount = playCount;
	}
	public Integer getPlayCount() {
		return this.playCount;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	public Integer getLikeCount() {
		return this.likeCount;
	}
	public void setCoinCount(Integer coinCount) {
		this.coinCount = coinCount;
	}
	public Integer getCoinCount() {
		return this.coinCount;
	}
	public void setDanmuCount(Integer danmuCount) {
		this.danmuCount = danmuCount;
	}
	public Integer getDanmuCount() {
		return this.danmuCount;
	}
	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}
	public Integer getCommentCount() {
		return this.commentCount;
	}
	public void setCollectCount(Integer collectCount) {
		this.collectCount = collectCount;
	}
	public Integer getCollectCount() {
		return this.collectCount;
	}
	public void setRecommendType(Integer recommendType) {
		this.recommendType = recommendType;
	}
	public Integer getRecommendType() {
		return this.recommendType;
	}
	public void setLastPlayTime(Date lastPlayTime) {
		this.lastPlayTime = lastPlayTime;
	}
	public Date getLastPlayTime() {
		return this.lastPlayTime;
	}

	@Override
	public String toString() {
		return "VideoInfo{" +
				"videoId='" + videoId + 
				", videoName='" + videoName + '\'' + 
				", videoCover='" + videoCover + '\'' + 
				", userId='" + userId + '\'' + 
				", createTime='" + DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", lastUpdateTime='" + DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", pCategoryId='" + pCategoryId + '\'' + 
				", categoryId='" + categoryId + '\'' + 
				", postType='" + postType + '\'' + 
				", originInfo='" + originInfo + '\'' + 
				", tags='" + tags + '\'' + 
				", introduction='" + introduction + '\'' + 
				", duration='" + duration + '\'' + 
				", interaction='" + interaction + '\'' + 
				", playCount='" + playCount + '\'' + 
				", likeCount='" + likeCount + '\'' + 
				", coinCount='" + coinCount + '\'' + 
				", danmuCount='" + danmuCount + '\'' + 
				", commentCount='" + commentCount + '\'' + 
				", collectCount='" + collectCount + '\'' + 
				", recommendType='" + recommendType + '\'' + 
				", lastPlayTime='" + DateUtils.format(lastPlayTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				'}';
	}

}