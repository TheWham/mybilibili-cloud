package com.mybilibili.base.entity.query;

import java.util.Date;

/**
 * @author amani
 * @since 2026/03/18
 * 用户关注列表
 */
public class UserFocusQuery extends BaseQuery {
	/**
	 * 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * 关注用户id
	 */
	private String userFocusId;
	private String userFocusIdFuzzy;
	/**
	 * 关注时间
	 */
	private Date focusTime;
	private String focusTimeStart;
	private String focusTimeEnd;
	private Boolean isQueryFansDetailInfo;
	private Boolean isQueryFocusDetailInfo;




	public Boolean getQueryFocusDetailInfo() {
		return isQueryFocusDetailInfo;
	}

	public void setQueryFocusDetailInfo(Boolean queryFocusDetailInfo) {
		isQueryFocusDetailInfo = queryFocusDetailInfo;
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
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setUserFocusIdFuzzy(String userFocusIdFuzzy) {
		this.userFocusIdFuzzy = userFocusIdFuzzy;
	}
	public String getUserFocusIdFuzzy() {
		return this.userFocusIdFuzzy;
	}
	public void setFocusTimeStart(String focusTimeStart) {
		this.focusTimeStart = focusTimeStart;
	}
	public String getFocusTimeStart() {
		return this.focusTimeStart;
	}
	public void setFocusTimeEnd(String focusTimeEnd) {
		this.focusTimeEnd = focusTimeEnd;
	}
	public String getFocusTimeEnd() {
		return this.focusTimeEnd;
	}

	public Boolean getQueryFansDetailInfo() {
		return isQueryFansDetailInfo;
	}

	public void setQueryFansDetailInfo(Boolean queryFansDetailInfo) {
		isQueryFansDetailInfo = queryFansDetailInfo;
	}
}