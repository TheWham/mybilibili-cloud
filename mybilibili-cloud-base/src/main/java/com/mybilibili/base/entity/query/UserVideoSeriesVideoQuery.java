package com.mybilibili.base.entity.query;

/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频
 */
public class UserVideoSeriesVideoQuery extends BaseQuery {
	/**
	 * 列表id
	 */
	private Integer seriesId;

	/**
	 * 列表视频id
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * 排序id
	 */
	private Integer sort;

	private Boolean isQueryVideoInfo;

	public Boolean getQueryVideoInfo() {
		return isQueryVideoInfo;
	}

	public void setQueryVideoInfo(Boolean queryVideoInfo) {
		isQueryVideoInfo = queryVideoInfo;
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
	public void setVideoIdFuzzy(String videoIdFuzzy) {
		this.videoIdFuzzy = videoIdFuzzy;
	}
	public String getVideoIdFuzzy() {
		return this.videoIdFuzzy;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}

}