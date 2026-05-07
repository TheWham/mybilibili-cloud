package com.mybilibili.interact.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybilibili.base.enums.DateTimePatternEnum;
import com.mybilibili.common.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 视频评论。
 */
public class VideoComment implements Serializable {

    private Integer commentId;

    @JsonProperty("pCommentId")
    private Integer pCommentId;

    private String videoId;
    private String videoUserId;
    private String content;
    private String imgPath;
    private String userId;
    private String replyUserId;
    private Integer topType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date postTime;

    private Integer likeCount;
    private Integer hateCount;

    private String avatar;
    private String nickName;
    private String replyNickName;
    private Integer replyCommentId;
    private String videoName;
    private String videoCover;
    private List<VideoComment> children;

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

    public String getVideoUserId() {
        return videoUserId;
    }

    public void setVideoUserId(String videoUserId) {
        this.videoUserId = videoUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(String replyUserId) {
        this.replyUserId = replyUserId;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getReplyNickName() {
        return replyNickName;
    }

    public void setReplyNickName(String replyNickName) {
        this.replyNickName = replyNickName;
    }

    public Integer getReplyCommentId() {
        return replyCommentId;
    }

    public void setReplyCommentId(Integer replyCommentId) {
        this.replyCommentId = replyCommentId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public List<VideoComment> getChildren() {
        return children;
    }

    public void setChildren(List<VideoComment> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "VideoComment{"
                + "commentId='" + commentId
                + ", pCommentId='" + pCommentId + '\''
                + ", videoId='" + videoId + '\''
                + ", videoUserId='" + videoUserId + '\''
                + ", content='" + content + '\''
                + ", imgPath='" + imgPath + '\''
                + ", userId='" + userId + '\''
                + ", replyUserId='" + replyUserId + '\''
                + ", topType='" + topType + '\''
                + ", postTime='" + DateUtils.format(postTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()) + '\''
                + ", likeCount='" + likeCount + '\''
                + ", hateCount='" + hateCount + '\''
                + '}';
    }
}
