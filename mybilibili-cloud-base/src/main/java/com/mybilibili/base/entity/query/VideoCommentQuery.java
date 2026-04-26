package com.mybilibili.base.entity.query;

import java.util.Date;

/**
 * @author amani
 * @date 2026/03/09
 * @description 评论
 */
public class VideoCommentQuery extends BaseQuery {
	/**
	 * @description 评论id
	 */
	private Integer commentId;

	/**
	 * @description 父级评论id
	 */
	private Integer pCommentId;

	/**
	 * @description 视频id
	 */
	private String videoId;
	private String videoIdFuzzy;
	/**
	 * @description 视频用户id
	 */
	private String videoUserId;
	private String videoUserIdFuzzy;
	/**
	 * @description 回复内容
	 */
	private String content;
	private String contentFuzzy;
	/**
	 * @description 图片路径
	 */
	private String imgPath;
	private String imgPathFuzzy;
	/**
	 * @description 用户id
	 */
	private String userId;
	private String userIdFuzzy;
	/**
	 * @description 回复人id
	 */
	private String replyUserId;
	private String replyUserIdFuzzy;
	/**
	 * @description 0:未置顶, 1:置顶
	 */
	private Integer topType;

	/**
	 * @description 发布时间
	 */
	private Date postTime;
	private String postTimeStart;
	private String postTimeEnd;
	/**
	 * @description 喜欢数量
	 */
	private Integer likeCount;

	/**
	 * @description 讨厌数量
	 */
	private Integer hateCount;

	/**
	 * isQueryChildren 是否查询子评论
	 */
	private Boolean isQueryChildren;

	private Boolean isQueryUserInfo;

	public Boolean getQueryUserInfo() {
		return isQueryUserInfo;
	}

	public void setQueryUserInfo(Boolean queryUserInfo) {
		isQueryUserInfo = queryUserInfo;
	}

	public Integer getpCommentId() {
		return pCommentId;
	}

	public void setpCommentId(Integer pCommentId) {
		this.pCommentId = pCommentId;
	}

	public Boolean getQueryChildren() {
		return isQueryChildren;
	}

	public void setQueryChildren(Boolean queryChildren) {
		isQueryChildren = queryChildren;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}
	public Integer getCommentId() {
		return this.commentId;
	}
	public void setPCommentId(Integer pCommentId) {
		this.pCommentId = pCommentId;
	}
	public Integer getPCommentId() {
		return this.pCommentId;
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
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return this.content;
	}
	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}
	public String getImgPath() {
		return this.imgPath;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setReplyUserId(String replyUserId) {
		this.replyUserId = replyUserId;
	}
	public String getReplyUserId() {
		return this.replyUserId;
	}
	public void setTopType(Integer topType) {
		this.topType = topType;
	}
	public Integer getTopType() {
		return this.topType;
	}
	public void setPostTime(Date postTime) {
		this.postTime = postTime;
	}
	public Date getPostTime() {
		return this.postTime;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	public Integer getLikeCount() {
		return this.likeCount;
	}
	public void setHateCount(Integer hateCount) {
		this.hateCount = hateCount;
	}
	public Integer getHateCount() {
		return this.hateCount;
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
	public void setContentFuzzy(String contentFuzzy) {
		this.contentFuzzy = contentFuzzy;
	}
	public String getContentFuzzy() {
		return this.contentFuzzy;
	}
	public void setImgPathFuzzy(String imgPathFuzzy) {
		this.imgPathFuzzy = imgPathFuzzy;
	}
	public String getImgPathFuzzy() {
		return this.imgPathFuzzy;
	}
	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}
	public String getUserIdFuzzy() {
		return this.userIdFuzzy;
	}
	public void setReplyUserIdFuzzy(String replyUserIdFuzzy) {
		this.replyUserIdFuzzy = replyUserIdFuzzy;
	}
	public String getReplyUserIdFuzzy() {
		return this.replyUserIdFuzzy;
	}
	public void setPostTimeStart(String postTimeStart) {
		this.postTimeStart = postTimeStart;
	}
	public String getPostTimeStart() {
		return this.postTimeStart;
	}
	public void setPostTimeEnd(String postTimeEnd) {
		this.postTimeEnd = postTimeEnd;
	}
	public String getPostTimeEnd() {
		return this.postTimeEnd;
	}

}