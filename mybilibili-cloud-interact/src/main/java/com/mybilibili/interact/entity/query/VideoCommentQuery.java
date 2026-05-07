package com.mybilibili.interact.entity.query;

import com.mybilibili.base.entity.query.BaseQuery;

import java.util.Date;

/**
 * 评论查询条件。
 */
public class VideoCommentQuery extends BaseQuery {

    private Integer commentId;
    private Integer pCommentId;
    private String videoId;
    private String videoIdFuzzy;
    private String videoUserId;
    private String videoUserIdFuzzy;
    private String content;
    private String contentFuzzy;
    private String imgPath;
    private String imgPathFuzzy;
    private String userId;
    private String userIdFuzzy;
    private String replyUserId;
    private String replyUserIdFuzzy;
    private Integer topType;
    private Date postTime;
    private String postTimeStart;
    private String postTimeEnd;
    private Integer likeCount;
    private Integer hateCount;
    private Boolean isQueryChildren;
    private Boolean isQueryUserInfo;

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getpCommentId() {
        return pCommentId;
    }

    public void setpCommentId(Integer pCommentId) {
        this.pCommentId = pCommentId;
    }

    public Integer getPCommentId() {
        return pCommentId;
    }

    public void setPCommentId(Integer pCommentId) {
        this.pCommentId = pCommentId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoIdFuzzy() {
        return videoIdFuzzy;
    }

    public void setVideoIdFuzzy(String videoIdFuzzy) {
        this.videoIdFuzzy = videoIdFuzzy;
    }

    public String getVideoUserId() {
        return videoUserId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public String getVideoUserIdFuzzy() {
        return videoUserIdFuzzy;
    }

    public void setVideoUserIdFuzzy(String videoUserIdFuzzy) {
        this.videoUserIdFuzzy = videoUserIdFuzzy;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentFuzzy() {
        return contentFuzzy;
    }

    public void setContentFuzzy(String contentFuzzy) {
        this.contentFuzzy = contentFuzzy;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getImgPathFuzzy() {
        return imgPathFuzzy;
    }

    public void setImgPathFuzzy(String imgPathFuzzy) {
        this.imgPathFuzzy = imgPathFuzzy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserIdFuzzy() {
        return userIdFuzzy;
    }

    public void setUserIdFuzzy(String userIdFuzzy) {
        this.userIdFuzzy = userIdFuzzy;
    }

    public String getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(String replyUserId) {
        this.replyUserId = replyUserId;
    }

    public String getReplyUserIdFuzzy() {
        return replyUserIdFuzzy;
    }

    public void setReplyUserIdFuzzy(String replyUserIdFuzzy) {
        this.replyUserIdFuzzy = replyUserIdFuzzy;
    }

    public Integer getTopType() {
        return topType;
    }

    public void setTopType(Integer topType) {
        this.topType = topType;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public String getPostTimeStart() {
        return postTimeStart;
    }

    public void setPostTimeStart(String postTimeStart) {
        this.postTimeStart = postTimeStart;
    }

    public String getPostTimeEnd() {
        return postTimeEnd;
    }

    public void setPostTimeEnd(String postTimeEnd) {
        this.postTimeEnd = postTimeEnd;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getHateCount() {
        return hateCount;
    }

    public void setHateCount(Integer hateCount) {
        this.hateCount = hateCount;
    }

    public Boolean getQueryChildren() {
        return isQueryChildren;
    }

    public void setQueryChildren(Boolean queryChildren) {
        isQueryChildren = queryChildren;
    }

    public Boolean getQueryUserInfo() {
        return isQueryUserInfo;
    }

    public void setQueryUserInfo(Boolean queryUserInfo) {
        isQueryUserInfo = queryUserInfo;
    }
}
