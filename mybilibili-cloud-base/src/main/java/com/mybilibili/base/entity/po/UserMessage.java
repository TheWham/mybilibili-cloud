package com.mybilibili.base.entity.po;


import com.alibaba.fastjson2.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/04/12
 * 用户消息表
 */
public class UserMessage implements Serializable {
	/**
	 * 消息id自增
	 */
    private Integer messageId;

	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 消息类型
	 */
    private Integer messageType;

	/**
	 * 发送人id
	 */
    private String sendUserId;

	/**
	 * 主体id
	 */
    private String videoId;

	/**
	 * 0:未读, 1:已读
	 */
    private Integer readType;

	/**
	 * 创建时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

	/**
	 * 扩展信息
	 */
    private String extendJson;

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}
	public Integer getMessageId() {
		return this.messageId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}
	public Integer getMessageType() {
		return this.messageType;
	}
	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}
	public String getSendUserId() {
		return this.sendUserId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setReadType(Integer readType) {
		this.readType = readType;
	}
	public Integer getReadType() {
		return this.readType;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getCreateTime() {
		return this.createTime;
	}
	public void setExtendJson(String extendJson) {
		this.extendJson = extendJson;
	}
	public String getExtendJson() {
		return this.extendJson;
	}

	@Override
	public String toString() {
		return "UserMessage{" +
				"messageId='" + messageId + 
				", userId='" + userId + '\'' + 
				", messageType='" + messageType + '\'' + 
				", sendUserId='" + sendUserId + '\'' + 
				", videoId='" + videoId + '\'' + 
				", readType='" + readType + '\'' + 
				", createTime='" + DateUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", extendJson='" + extendJson + '\'' + 
				'}';
	}

}