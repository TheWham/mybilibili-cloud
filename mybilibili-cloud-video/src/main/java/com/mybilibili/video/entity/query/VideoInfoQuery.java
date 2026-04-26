package com.mybilibili.video.entity.query;

import com.mybilibili.base.entity.query.BaseQuery;

import java.util.Date;

/**
 * @author amani
 * @date 2026/02/09
 * @description 视频信息
 */
public class VideoInfoQuery extends BaseQuery {
	/**
	 * @description 视频ID
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * @description 文件名称
	 */
	private String videoName;
	private String videoNameFuzzy;
	/**
	 * @description 视频封面
	 */
	private String videoCover;
	private String videoCoverFuzzy;
	/**
	 * @description 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * @description 创建时间
	 */
	private Date createTime;
	private String createTimeStart;
	private String createTimeEnd;
	/**
	 * @description 最后更新时间
	 */
	private Date lastUpdateTime;
	private String lastUpdateTimeStart;
	private String lastUpdateTimeEnd;
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
	private String originInfoFuzzy;
	/**
	 * @description 视频标签
	 */
	private String tags;
	private String tagsFuzzy;
	/**
	 * @description 视频简介
	 */
	private String introduction;
	private String introductionFuzzy;
	/**
	 * @description 持续时间(秒)
	 */
	private Integer duration;

	/**
	 * @description 互动设置
	 */
	private String interaction;
	private String interactionFuzzy;
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
	private Date lastPlayTime;
	private String lastPlayTimeStart;
	private String lastPlayTimeEnd;
	private boolean isQueryUserInfo;
	private String[] arrayIds;
	private Integer lastPlayHour;
	/**
	 * @description 是否查询数量
	 * @param isQueryCountInfo
	 */
	private Boolean isQueryCountInfo;

	public Integer getpCategoryId() {
		return pCategoryId;
	}

	public void setpCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
	}

	public Boolean getQueryCountInfo() {
		return isQueryCountInfo;
	}

	public void setQueryCountInfo(Boolean queryCountInfo) {
		isQueryCountInfo = queryCountInfo;
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
	public void setVideoIdFuzzy(String videoIdFuzzy) {
		this.videoIdFuzzy = videoIdFuzzy;
	}
	public String getVideoIdFuzzy() {
		return this.videoIdFuzzy;
	}
	public void setVideoNameFuzzy(String videoNameFuzzy) {
		this.videoNameFuzzy = videoNameFuzzy;
	}
	public String getVideoNameFuzzy() {
		return this.videoNameFuzzy;
	}
	public void setVideoCoverFuzzy(String videoCoverFuzzy) {
		this.videoCoverFuzzy = videoCoverFuzzy;
	}
	public String getVideoCoverFuzzy() {
		return this.videoCoverFuzzy;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}
	public String getCreateTimeStart() {
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}
	public String getCreateTimeEnd() {
		return this.createTimeEnd;
	}
	public void setLastUpdateTimeStart(String lastUpdateTimeStart) {
		this.lastUpdateTimeStart = lastUpdateTimeStart;
	}
	public String getLastUpdateTimeStart() {
		return this.lastUpdateTimeStart;
	}
	public void setLastUpdateTimeEnd(String lastUpdateTimeEnd) {
		this.lastUpdateTimeEnd = lastUpdateTimeEnd;
	}
	public String getLastUpdateTimeEnd() {
		return this.lastUpdateTimeEnd;
	}
	public void setOriginInfoFuzzy(String originInfoFuzzy) {
		this.originInfoFuzzy = originInfoFuzzy;
	}
	public String getOriginInfoFuzzy() {
		return this.originInfoFuzzy;
	}
	public void setTagsFuzzy(String tagsFuzzy) {
		this.tagsFuzzy = tagsFuzzy;
	}
	public String getTagsFuzzy() {
		return this.tagsFuzzy;
	}
	public void setIntroductionFuzzy(String introductionFuzzy) {
		this.introductionFuzzy = introductionFuzzy;
	}
	public String getIntroductionFuzzy() {
		return this.introductionFuzzy;
	}
	public void setInteractionFuzzy(String interactionFuzzy) {
		this.interactionFuzzy = interactionFuzzy;
	}
	public String getInteractionFuzzy() {
		return this.interactionFuzzy;
	}
	public void setLastPlayTimeStart(String lastPlayTimeStart) {
		this.lastPlayTimeStart = lastPlayTimeStart;
	}
	public String getLastPlayTimeStart() {
		return this.lastPlayTimeStart;
	}
	public void setLastPlayTimeEnd(String lastPlayTimeEnd) {
		this.lastPlayTimeEnd = lastPlayTimeEnd;
	}
	public String getLastPlayTimeEnd() {
		return this.lastPlayTimeEnd;
	}

    public void setQueryUserInfo(boolean queryUserInfo) {
        this.isQueryUserInfo = queryUserInfo;
    }

    public boolean isQueryUserInfo() {
        return isQueryUserInfo;
    }

    public void setArrayIds(String[] arrayIds) {
        this.arrayIds = arrayIds;
    }

    public String[] getArrayIds() {
        return arrayIds;
    }

    public void setLastPlayHour(Integer lastPlayHour) {
        this.lastPlayHour = lastPlayHour;
    }

    public Integer getLastPlayHour() {
        return lastPlayHour;
    }
}