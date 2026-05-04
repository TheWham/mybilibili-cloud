package com.mybilibili.video.entity.po;

import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/09
 * 视频弹幕
 */
public class VideoDanmu implements Serializable {
	/**
	 * 弹幕id
	 */
    private Integer danmuId;

	/**
	 * 视频id
	 */
    private String videoId;

	/**
	 * 唯一id
	 */
    private String fileId;

	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 发布时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

	/**
	 * 弹幕内容
	 */
    private String text;

	/**
	 * 展示位置
	 */
    private Integer mode;

	/**
	 * 颜色
	 */
    private String color;

	/**
	 * 展示时间
	 */
    private Integer time;
	private String nickName;
	private String videoName;

	public String getVideoName() {
		return videoName;
	}

	public void setVideoName(String videoName) {
		this.videoName = videoName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public void setDanmuId(Integer danmuId) {
		this.danmuId = danmuId;
	}
	public Integer getDanmuId() {
		return this.danmuId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public String getFileId() {
		return this.fileId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setPostTime(Date postTime) {
		this.postTime = postTime;
	}
	public Date getPostTime() {
		return this.postTime;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getText() {
		return this.text;
	}
	public void setMode(Integer mode) {
		this.mode = mode;
	}
	public Integer getMode() {
		return this.mode;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getColor() {
		return this.color;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public Integer getTime() {
		return this.time;
	}

	@Override
	public String toString() {
		return "VideoDanmu{" +
				"danmuId='" + danmuId + 
				", videoId='" + videoId + '\'' + 
				", fileId='" + fileId + '\'' + 
				", userId='" + userId + '\'' + 
				", postTime='" + DateUtils.format(postTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", text='" + text + '\'' + 
				", mode='" + mode + '\'' + 
				", color='" + color + '\'' + 
				", time='" + time + '\'' + 
				'}';
	}

}