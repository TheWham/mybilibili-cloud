package com.mybilibili.base.entity.query;

import java.util.Date;
import java.util.List;

/**
 * @author amani
 * @version  2026/01/07
 * @description 
 */
public class UserInfoQuery extends BaseQuery {
	/**
	 * @description 用户id

	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * @description 昵称
	 */
	private String nickName;
	private String nickNameFuzzy;
	/**
	 * @description 出生日期
	 */
	private Date birthday;
	private String birthdayStart;
	private String birthdayEnd;
	/**
	 * @description 0:女 1:男 2:未知
	 */
	private Integer sex;

	/**
	 * @description 邮箱
	 */
	private String email;
	private String emailFuzzy;
	/**
	 * @description 密码
	 */
	private String password;
	private String passwordFuzzy;
	/**
	 * @description 学校
	 */
	private String school;
	private String schoolFuzzy;
	/**
	 * @description 个人简介
	 */
	private String personIntroduction;
	private String personIntroductionFuzzy;
	/**
	 * @description 注册时间
	 */
	private Date joinTime;
	private String joinTimeStart;
	private String joinTimeEnd;
	/**
	 * @description 最后登录时间
	 */
	private Date lastLoginTime;
	private String lastLoginTimeStart;
	private String lastLoginTimeEnd;
	/**
	 * @description 最后登录ip
	 */
	private String lastLoginIp;
	private String lastLoginIpFuzzy;
	/**
	 * @description 0:禁用 1:正常
	 */
	private Integer status;

	/**
	 * @description 空间公告
	 */
	private String noticeInfo;
	private String noticeInfoFuzzy;
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
	private String avatar;
	private String avatarFuzzy;
	private List<String> userIds;

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}

	public String getAvatarFuzzy() {
		return avatarFuzzy;
	}

	public void setAvatarFuzzy(String avatarFuzzy) {
		this.avatarFuzzy = avatarFuzzy;
	}

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
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public Date getBirthday() {
		return this.birthday;
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
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setNickNameFuzzy(String nickNameFuzzy) {
		this.nickNameFuzzy = nickNameFuzzy;
	}
	public String getNickNameFuzzy() {
		return this.nickNameFuzzy;
	}
	public void setBirthdayStart(String birthdayStart) {
		this.birthdayStart = birthdayStart;
	}
	public String getBirthdayStart() {
		return this.birthdayStart;
	}
	public void setBirthdayEnd(String birthdayEnd) {
		this.birthdayEnd = birthdayEnd;
	}
	public String getBirthdayEnd() {
		return this.birthdayEnd;
	}
	public void setEmailFuzzy(String emailFuzzy) {
		this.emailFuzzy = emailFuzzy;
	}
	public String getEmailFuzzy() {
		return this.emailFuzzy;
	}
	public void setPasswordFuzzy(String passwordFuzzy) {
		this.passwordFuzzy = passwordFuzzy;
	}
	public String getPasswordFuzzy() {
		return this.passwordFuzzy;
	}
	public void setSchoolFuzzy(String schoolFuzzy) {
		this.schoolFuzzy = schoolFuzzy;
	}
	public String getSchoolFuzzy() {
		return this.schoolFuzzy;
	}
	public void setPersonIntroductionFuzzy(String personIntroductionFuzzy) {
		this.personIntroductionFuzzy = personIntroductionFuzzy;
	}
	public String getPersonIntroductionFuzzy() {
		return this.personIntroductionFuzzy;
	}
	public void setJoinTimeStart(String joinTimeStart) {
		this.joinTimeStart = joinTimeStart;
	}
	public String getJoinTimeStart() {
		return this.joinTimeStart;
	}
	public void setJoinTimeEnd(String joinTimeEnd) {
		this.joinTimeEnd = joinTimeEnd;
	}
	public String getJoinTimeEnd() {
		return this.joinTimeEnd;
	}
	public void setLastLoginTimeStart(String lastLoginTimeStart) {
		this.lastLoginTimeStart = lastLoginTimeStart;
	}
	public String getLastLoginTimeStart() {
		return this.lastLoginTimeStart;
	}
	public void setLastLoginTimeEnd(String lastLoginTimeEnd) {
		this.lastLoginTimeEnd = lastLoginTimeEnd;
	}
	public String getLastLoginTimeEnd() {
		return this.lastLoginTimeEnd;
	}
	public void setLastLoginIpFuzzy(String lastLoginIpFuzzy) {
		this.lastLoginIpFuzzy = lastLoginIpFuzzy;
	}
	public String getLastLoginIpFuzzy() {
		return this.lastLoginIpFuzzy;
	}
	public void setNoticeInfoFuzzy(String noticeInfoFuzzy) {
		this.noticeInfoFuzzy = noticeInfoFuzzy;
	}
	public String getNoticeInfoFuzzy() {
		return this.noticeInfoFuzzy;
	}

}