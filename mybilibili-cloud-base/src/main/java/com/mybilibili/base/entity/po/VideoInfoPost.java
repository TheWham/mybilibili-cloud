package com.mybilibili.base.entity.po;

import com.alibaba.fastjson2.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.base.enums.VideoStatusEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @date 2026/02/13
 * @description 视频信息
 */
public class VideoInfoPost extends VideoInfo implements Serializable {
	/**
	 * @description 视频ID
	 */
    private String videoId;

	/**
	 * @description 视频封面
	 */
    private String videoCover;

	/**
	 * @description 视频名称
	 */
    private String videoName;

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

	/**
	 * @description 标签
	 */
    private String tags;

	/**
	 * @description 简介
	 */
    private String introduction;

	/**
	 * @description 互动设置
	 */
    private String interaction;

	/**
	 * @description 持续时间(秒)
	 */
    private Integer duration;

	/**
	 * 状态名称
	 */
	private String statusName;

	public String getStatusName() {
		VideoStatusEnum videoStatus = VideoStatusEnum.getByStatus(this.status);
		return videoStatus==null?"":videoStatus.getDesc();
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Integer getpCategoryId() {
		return pCategoryId;
	}

	public void setpCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
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

	@Override
	public String toString() {
		return "VideoInfoPost{" +
				"videoId='" + videoId + 
				", videoCover='" + videoCover + '\'' + 
				", videoName='" + videoName + '\'' + 
				", userId='" + userId + '\'' + 
				", createTime='" + DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", lastUpdateTime='" + DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", pCategoryId='" + pCategoryId + '\'' + 
				", categoryId='" + categoryId + '\'' + 
				", status='" + status + '\'' + 
				", postType='" + postType + '\'' + 
				", originInfo='" + originInfo + '\'' + 
				", tags='" + tags + '\'' + 
				", introduction='" + introduction + '\'' + 
				", interaction='" + interaction + '\'' + 
				", duration='" + duration + '\'' + 
				'}';
	}

}
