package com.mybilibili.user.entity.po;

import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @date 2026/01/07
 * @description 
 */
public class UserInfo implements Serializable {
	/**
	 * @description 用户id

	 */
    private String userId;


	/**
	 * @description 昵称
	 */
    private String nickName;

	/**
	 * @description 出生日期
	 */
    private String birthday;

	/**
	 * @description 0:女 1:男 2:未知
	 */
    private Integer sex;

	/**
	 * @description 邮箱
	 */
    private String email;

	/**
	 * @description 密码
	 */
    private String password;

	/**
	 * @description 学校
	 */
    private String school;

	/**
	 * @description 个人简介
	 */
    private String personIntroduction;

	/**
	 * @description 注册时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date joinTime;

	/**
	 * @description 最后登录时间
	 */
	@JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

	/**
	 * @description 最后登录ip
	 */
    private String lastLoginIp;

	/**
	 * @description 0:禁用 1:正常
	 */
    private Integer status;

	/**
	 * @description 空间公告
	 */
    private String noticeInfo;

	/**
	 * @description 历史硬币总量
	 */
    private Integer totalCoinCount;

	/**
	 * @description 当前硬币数量
	 */
    private Integer currentCoinCount;

	/**
	 * @description 主题
	 */
    private Integer theme;

	/**
	 * @description 头像
	 */
	private String avatar;


	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getNickName() {
		return this.nickName;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}
	public Integer getSex() {
		return this.sex;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return this.email;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getPassword() {
		return this.password;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getSchool() {
		return this.school;
	}
	public void setPersonIntroduction(String personIntroduction) {
		this.personIntroduction = personIntroduction;
	}
	public String getPersonIntroduction() {
		return this.personIntroduction;
	}
	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}
	public Date getJoinTime() {
		return this.joinTime;
	}
	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	public Date getLastLoginTime() {
		return this.lastLoginTime;
	}
	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}
	public String getLastLoginIp() {
		return this.lastLoginIp;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getStatus() {
		return this.status;
	}
	public void setNoticeInfo(String noticeInfo) {
		this.noticeInfo = noticeInfo;
	}
	public String getNoticeInfo() {
		return this.noticeInfo;
	}
	public void setTotalCoinCount(Integer totalCoinCount) {
		this.totalCoinCount = totalCoinCount;
	}
	public Integer getTotalCoinCount() {
		return this.totalCoinCount;
	}
	public void setCurrentCoinCount(Integer currentCoinCount) {
		this.currentCoinCount = currentCoinCount;
	}
	public Integer getCurrentCoinCount() {
		return this.currentCoinCount;
	}
	public void setTheme(Integer theme) {
		this.theme = theme;
	}
	public Integer getTheme() {
		return this.theme;
	}

	@Override
	public String toString() {
		return "UserInfo{" +
				"userId='" + userId + 
				", nickName='" + nickName + '\'' + 
				", birthday='" + birthday + '\'' +
				", sex='" + sex + '\'' + 
				", email='" + email + '\'' + 
				", password='" + password + '\'' + 
				", school='" + school + '\'' + 
				", personIntroduction='" + personIntroduction + '\'' + 
				", joinTime='" + DateUtils.format(joinTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", lastLoginTime='" + DateUtils.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\'' + 
				", lastLoginIp='" + lastLoginIp + '\'' + 
				", status='" + status + '\'' + 
				", noticeInfo='" + noticeInfo + '\'' + 
				", totalCoinCount='" + totalCoinCount + '\'' + 
				", currentCoinCount='" + currentCoinCount + '\'' + 
				", theme='" + theme + '\'' + 
				'}';
	}

}