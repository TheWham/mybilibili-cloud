package com.mybilibili.base.entity.query;

import java.util.Date;

/**
 * @author amani
 * @date 2026/03/09
 * @description 用户行为  点赞,评论
 */
public class UserActionQuery extends BaseQuery {
	/**
	 * @description 自增id
	 */
	private Integer actionId;

	/**
	 * @description 视频Id
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * @description 视频用户id
	 */
	private String videoUserId;
	private String videoUserIdFuzzy;
	/**
	 * @description 评论id
	 */
	private Integer commentId;

	/**
	 * @description 0:评论喜欢点赞 1:讨厌评论 2:视频点赞 3:视频收藏 4:视频投币
	 */
	private Integer actionType;

	/**
	 * @description 数量
	 */
	private Integer actionCount;

	/**
	 * @description 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * @description 操作时间
	 */
	private Date actionTime;
	private String actionTimeStart;
	private String actionTimeEnd;

	/**
	 * 用户操作类型(点赞,投币,收藏)
	 */
	private Integer[] userActionTypeList;

	public Integer[] getUserActionTypeList() {
		return userActionTypeList;
	}

	public void setUserActionTypeList(Integer[] userActionTypeList) {
		this.userActionTypeList = userActionTypeList;
	}

	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	public Integer getActionId() {
		return this.actionId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getVideoId() {
		return this.videoId;
	}
	public void setVideoUserId(String videoUserId) {
		this.videoUserId = videoUserId;
	}
	public String getVideoUserId() {
		return this.videoUserId;
	}
	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}
	public Integer getCommentId() {
		return this.commentId;
	}
	public void setActionType(Integer actionType) {
		this.actionType = actionType;
	}
	public Integer getActionType() {
		return this.actionType;
	}
	public void setActionCount(Integer actionCount) {
		this.actionCount = actionCount;
	}
	public Integer getActionCount() {
		return this.actionCount;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setActionTime(Date actionTime) {
		this.actionTime = actionTime;
	}
	public Date getActionTime() {
		return this.actionTime;
	}
	public void setVideoIdFuzzy(String videoIdFuzzy) {
		this.videoIdFuzzy = videoIdFuzzy;
	}
	public String getVideoIdFuzzy() {
		return this.videoIdFuzzy;
	}
	public void setVideoUserIdFuzzy(String videoUserIdFuzzy) {
		this.videoUserIdFuzzy = videoUserIdFuzzy;
	}
	public String getVideoUserIdFuzzy() {
		return this.videoUserIdFuzzy;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setActionTimeStart(String actionTimeStart) {
		this.actionTimeStart = actionTimeStart;
	}
	public String getActionTimeStart() {
		return this.actionTimeStart;
	}
	public void setActionTimeEnd(String actionTimeEnd) {
		this.actionTimeEnd = actionTimeEnd;
	}
	public String getActionTimeEnd() {
		return this.actionTimeEnd;
	}

}