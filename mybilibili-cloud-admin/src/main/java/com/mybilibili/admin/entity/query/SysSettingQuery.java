package com.mybilibili.admin.entity.query;

import com.mybilibili.base.entity.query.BaseQuery;

import java.util.Date;

/**
 * @author amani
 * @since 2026/04/22
 * 
 */
public class SysSettingQuery extends BaseQuery {
	/**
	 * 
	 */
	private Long id;

	/**
	 * 注册送硬币数
	 */
	private Integer registerCoinCount;

	/**
	 * 发布视频送硬币数
	 */
	private Integer postVideoCoinCount;

	/**
	 * 单个视频大小MB
	 */
	private Integer videoSize;

	/**
	 * 最大分P数
	 */
	private Integer videoPCount;

	/**
	 * 每天发布视频数
	 */
	private Integer videoCount;

	/**
	 * 每天评论数
	 */
	private Integer commentCount;

	/**
	 * 每天弹幕数
	 */
	private Integer danmuCount;

	/**
	 * 
	 */
	private Date updateTime;
	private String updateTimeStart;
	private String updateTimeEnd;
	/**
	 * 最后修改人
	 */
	private String updateBy;
	private String updateByFuzzy;
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return this.id;
	}
	public void setRegisterCoinCount(Integer registerCoinCount) {
		this.registerCoinCount = registerCoinCount;
	}
	public Integer getRegisterCoinCount() {
		return this.registerCoinCount;
	}
	public void setPostVideoCoinCount(Integer postVideoCoinCount) {
		this.postVideoCoinCount = postVideoCoinCount;
	}
	public Integer getPostVideoCoinCount() {
		return this.postVideoCoinCount;
	}
	public void setVideoSize(Integer videoSize) {
		this.videoSize = videoSize;
	}
	public Integer getVideoSize() {
		return this.videoSize;
	}
	public void setVideoPCount(Integer videoPCount) {
		this.videoPCount = videoPCount;
	}
	public Integer getVideoPCount() {
		return this.videoPCount;
	}
	public void setVideoCount(Integer videoCount) {
		this.videoCount = videoCount;
	}
	public Integer getVideoCount() {
		return this.videoCount;
	}
	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}
	public Integer getCommentCount() {
		return this.commentCount;
	}
	public void setDanmuCount(Integer danmuCount) {
		this.danmuCount = danmuCount;
	}
	public Integer getDanmuCount() {
		return this.danmuCount;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public Date getUpdateTime() {
		return this.updateTime;
	}
	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	public String getUpdateBy() {
		return this.updateBy;
	}
	public void setUpdateTimeStart(String updateTimeStart) {
		this.updateTimeStart = updateTimeStart;
	}
	public String getUpdateTimeStart() {
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd) {
		this.updateTimeEnd = updateTimeEnd;
	}
	public String getUpdateTimeEnd() {
		return this.updateTimeEnd;
	}
	public void setUpdateByFuzzy(String updateByFuzzy) {
		this.updateByFuzzy = updateByFuzzy;
	}
	public String getUpdateByFuzzy() {
		return this.updateByFuzzy;
	}

}
