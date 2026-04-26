package com.mybilibili.video.entity.query;

import com.mybilibili.base.entity.query.BaseQuery;

import java.util.Date;

/**
 * @author amani
 * @date 2026/02/13
 * @description 视频信息
 */
public class VideoInfoPostQuery extends BaseQuery {
	/**
	 * @description 视频ID
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * @description 视频封面
	 */
	private String videoCover;
	private String videoCoverFuzzy;
	/**
	 * @description 视频名称
	 */
	private String videoName;
	private String videoNameFuzzy;
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
	 * @description 父级分类id
	 */
	private Integer pCategoryId;

	/**
	 * @description 分类id
	 */
	private Integer categoryId;

	/**
	 * @description 0:转码中, 1:转码失败, 2: 待审核, 3:审核成功, 4:审核失败
	 */
	private Integer status;

	/**
	 * @description 0:自制作, 1:转载
	 */
	private Integer postType;

	/**
	 * @description 原资源说明
	 */
	private String originInfo;
	private String originInfoFuzzy;
	/**
	 * @description 标签
	 */
	private String tags;
	private String tagsFuzzy;
	/**
	 * @description 简介
	 */
	private String introduction;
	private String introductionFuzzy;
	/**
	 * @description 互动设置
	 */
	private String interaction;
	private String interactionFuzzy;
	/**
	 * @description 持续时间(秒)
	 */
	private Integer duration;

	/**
	 * @description 排除其他状态
	 * @param excludeStatusArray
	 */

	private Integer [] excludeStatusArray;

	/**
	 * @description 是否查询数量
	 * @param isQueryCountInfo
	 */
	private Boolean isQueryCountInfo;

	/**
	 * @description 是否查询用户信息
	 */
	private Boolean isQueryUserInfo;

	private Integer recommendType;

	public Integer getRecommendType() {
		return recommendType;
	}

	public void setRecommendType(Integer recommendType) {
		this.recommendType = recommendType;
	}

	public Boolean getQueryUserInfo() {
		return isQueryUserInfo;
	}

	public void setQueryUserInfo(Boolean queryUserInfo) {
		isQueryUserInfo = queryUserInfo;
	}

	public Boolean getQueryCountInfo() {
		return isQueryCountInfo;
	}

	public void setQueryCountInfo(Boolean queryCountInfo) {
		isQueryCountInfo = queryCountInfo;
	}

	public Integer getpCategoryId() {
		return pCategoryId;
	}

	public void setpCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
	}

	public Integer[] getExcludeStatusArray() {
		return excludeStatusArray;
	}

	public void setExcludeStatusArray(Integer[] excludeStatusArray) {
		this.excludeStatusArray = excludeStatusArray;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setVideoCover(String videoCover) {
		this.videoCover = videoCover;
	}
	public String getVideoCover() {
		return this.videoCover;
	}
	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}
	public String getVideoName() {
		return this.videoName;
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
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getStatus() {
		return this.status;
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
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}
	public String getInteraction() {
		return this.interaction;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Integer getDuration() {
		return this.duration;
	}
	public void setVideoIdFuzzy(String videoIdFuzzy) {
		this.videoIdFuzzy = videoIdFuzzy;
	}
	public String getVideoIdFuzzy() {
		return this.videoIdFuzzy;
	}
	public void setVideoCoverFuzzy(String videoCoverFuzzy) {
		this.videoCoverFuzzy = videoCoverFuzzy;
	}
	public String getVideoCoverFuzzy() {
		return this.videoCoverFuzzy;
	}
	public void setVideoNameFuzzy(String videoNameFuzzy) {
		this.videoNameFuzzy = videoNameFuzzy;
	}
	public String getVideoNameFuzzy() {
		return this.videoNameFuzzy;
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

}