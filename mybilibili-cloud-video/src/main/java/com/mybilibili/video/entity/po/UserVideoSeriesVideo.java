package com.mybilibili.video.entity.po;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频
 */
public class UserVideoSeriesVideo implements Serializable {
	/**
	 * 列表id
	 */
    private Integer seriesId;

	/**
	 * 列表视频id
	 */
    private String videoId;

	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 排序id
	 */
    private Integer sort;
	private String videoName;
	private String videoCover;
	private Integer playCount;
	private Date createTime;

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public void setSeriesId(Integer seriesId) {
		this.seriesId = seriesId;
	}
	public Integer getSeriesId() {
		return this.seriesId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public Integer getSort() {
		return this.sort;
	}

	@Override
	public String toString() {
		return "UserVideoSeriesVideo{" +
				"seriesId='" + seriesId + 
				", videoId='" + videoId + '\'' + 
				", userId='" + userId + '\'' + 
				", sort='" + sort + '\'' + 
				'}';
	}

}