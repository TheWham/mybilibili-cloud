package com.mybilibili.base.entity.query;

import java.util.Date;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史
 */
public class VideoPlayHistoryQuery extends BaseQuery {
	/**
	 * 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * 视频id
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * 文件索引
	 */
	private Integer fileIndex;

	/**
	 * 最后更新时间
	 */
	private Date lastUpdateTime;
	private String lastUpdateTimeStart;
	private String lastUpdateTimeEnd;
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setFileIndex(Integer fileIndex) {
		this.fileIndex = fileIndex;
	}
	public Integer getFileIndex() {
		return this.fileIndex;
	}
	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public Date getLastUpdateTime() {
		return this.lastUpdateTime;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setVideoIdFuzzy(String videoIdFuzzy) {
		this.videoIdFuzzy = videoIdFuzzy;
	}
	public String getVideoIdFuzzy() {
		return this.videoIdFuzzy;
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

}