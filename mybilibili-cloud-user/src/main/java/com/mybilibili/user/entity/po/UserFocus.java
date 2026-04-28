package com.mybilibili.user.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表
 */
public class UserFocus implements Serializable {
	/**
	 * 用户id
	 */
    private String userId;

	/**
	 * 关注用户id
	 */
    private String userFocusId;

	/**
	 * 关注时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date focusTime;
	/**
	 * 粉丝昵称
	 */
	private String otherNickName;
	/**
	 * 粉丝头像
	 */
	private String otherAvatar;

	/**
	 * 粉丝用户id
	 */
	private String otherUserId;

	/**
	 * 关注类型 0表示为关注, 1表示关注
	 */
	private Integer focusType;

	public Integer getFocusType() {
		return focusType;
	}

	public void setFocusType(Integer focusType) {
		this.focusType = focusType;
	}

	public String getOtherUserId() {
		return otherUserId;
	}

	public void setOtherUserId(String otherUserId) {
		this.otherUserId = otherUserId;
	}

	/**
	 * 粉丝个人简介
	 */
	private String otherPersonIntroduction;

	public String getOtherNickName() {
		return otherNickName;
	}

	public void setOtherNickName(String otherNickName) {
		this.otherNickName = otherNickName;
	}

	public String getOtherAvatar() {
		return otherAvatar;
	}

	public void setOtherAvatar(String otherAvatar) {
		this.otherAvatar = otherAvatar;
	}

	public String getOtherPersonIntroduction() {
		return otherPersonIntroduction;
	}

	public void setOtherPersonIntroduction(String otherPersonIntroduction) {
		this.otherPersonIntroduction = otherPersonIntroduction;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setUserFocusId(String userFocusId) {
		this.userFocusId = userFocusId;
	}
	public String getUserFocusId() {
		return this.userFocusId;
	}
	public void setFocusTime(Date focusTime) {
		this.focusTime = focusTime;
	}
	public Date getFocusTime() {
		return this.focusTime;
	}

	@Override
	public String toString() {
		return "UserFocus{" +
				"userId='" + userId + 
				", userFocusId='" + userFocusId + '\'' + 
				", focusTime='" + DateUtils.format(focusTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				'}';
	}

}