package com.mybilibili.video.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史
 */
public class VideoPlayHistory implements Serializable {
	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 视频id
	 */
    private String videoId;

	/**
	 * 文件索引
	 */
    private Integer fileIndex;

	/**
	 * 最后更新时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;

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

	@Override
	public String toString() {
		return "VideoPlayHistory{" +
				"userId='" + userId + 
				", videoId='" + videoId + '\'' + 
				", fileIndex='" + fileIndex + '\'' + 
				", lastUpdateTime='" + DateUtils.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				'}';
	}

}