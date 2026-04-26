package com.mybilibili.base.entity.po;


import com.alibaba.fastjson2.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/04/22
 * 
 */
public class SysSetting implements Serializable {
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
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

	/**
	 * 最后修改人
	 */
    private String updateBy;

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

	@Override
	public String toString() {
		return "SysSetting{" +
				"id='" + id + 
				", registerCoinCount='" + registerCoinCount + '\'' + 
				", postVideoCoinCount='" + postVideoCoinCount + '\'' + 
				", videoSize='" + videoSize + '\'' + 
				", videoPCount='" + videoPCount + '\'' + 
				", videoCount='" + videoCount + '\'' + 
				", commentCount='" + commentCount + '\'' + 
				", danmuCount='" + danmuCount + '\'' + 
				", updateTime='" + DateUtils.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", updateBy='" + updateBy + '\'' + 
				'}';
	}

}
