package com.mybilibili.user.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/21
 * 用户数量统计表
 */
public class UserStats implements Serializable {
	/**
	 * 用户id 关联user_info
	 */
    private String userId;

	/**
	 * 播放数量
	 */
    private Integer playCount;

	/**
	 * 点赞数量
	 */
    private Integer likeCount;

	/**
	 * 当前硬币数量
	 */
    private Integer currentCoinCount;

	/**
	 * 关注数量
	 */
    private Integer focusCount;

	/**
	 * 粉丝数量
	 */
    private Integer fansCount;
	/**
	 * 收藏量
	 */
	private Integer collectCount;
	/**
	 * 评论量
	 */
	private Integer commentCount;
	/**
	 * 弹幕量
	 */
	private Integer danmuCount;

	/**
	 * 统计数量时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date statsDay;

	/**
	 * 用户投币量
	 */
	private Integer coinCount;

	/**
	 * 管理端统计时使用的用户数。
	 * user_stats 明细表本身没有这个字段，这里只接收全站汇总 SQL 的别名结果。
	 */
	private Integer userCount;

	public Integer getUserCount() {
		return userCount;
	}

	public void setUserCount(Integer userCount) {
		this.userCount = userCount;
	}

	public Integer getCoinCount() {
		return coinCount;
	}

	public void setCoinCount(Integer coinCount) {
		this.coinCount = coinCount;
	}

	public Date getStatsDay() {
		return statsDay;
	}

	public void setStatsDay(Date statsDay) {
		this.statsDay = statsDay;
	}

	public Integer getCollectCount() {
		return collectCount;
	}

	public void setCollectCount(Integer collectCount) {
		this.collectCount = collectCount;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public Integer getDanmuCount() {
		return danmuCount;
	}

	public void setDanmuCount(Integer danmuCount) {
		this.danmuCount = danmuCount;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return this.userId;
	}
	public void setPlayCount(Integer playCount) {
		this.playCount = playCount;
	}
	public Integer getPlayCount() {
		return this.playCount;
	}
	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}
	public Integer getLikeCount() {
		return this.likeCount;
	}
	public void setCurrentCoinCount(Integer currentCoinCount) {
		this.currentCoinCount = currentCoinCount;
	}
	public Integer getCurrentCoinCount() {
		return this.currentCoinCount;
	}
	public void setFocusCount(Integer focusCount) {
		this.focusCount = focusCount;
	}
	public Integer getFocusCount() {
		return this.focusCount;
	}
	public void setFansCount(Integer fansCount) {
		this.fansCount = fansCount;
	}
	public Integer getFansCount() {
		return this.fansCount;
	}

	@Override
	public String toString() {
		return "UserStats{" +
				"userId='" + userId + 
				", playCount='" + playCount + '\'' + 
				", likeCount='" + likeCount + '\'' + 
				", currentCoinCount='" + currentCoinCount + '\'' + 
				", focusCount='" + focusCount + '\'' + 
				", fansCount='" + fansCount + '\'' + 
				'}';
	}

}
