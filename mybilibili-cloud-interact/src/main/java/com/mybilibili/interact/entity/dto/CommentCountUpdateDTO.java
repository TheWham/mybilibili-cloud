package com.mybilibili.interact.entity.dto;

/**
 * 评论点赞、点踩数量批量更新项。
 */
public class CommentCountUpdateDTO {

    private Integer commentId;
    private Integer likeDiff;
    private Integer hateDiff;

    public CommentCountUpdateDTO() {
    }

    public CommentCountUpdateDTO(Integer commentId, Integer likeDiff, Integer hateDiff) {
        this.commentId = commentId;
        this.likeDiff = likeDiff;
        this.hateDiff = hateDiff;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getLikeDiff() {
        return likeDiff;
    }

    public void setLikeDiff(Integer likeDiff) {
        this.likeDiff = likeDiff;
    }

    public Integer getHateDiff() {
        return hateDiff;
    }

    public void setHateDiff(Integer hateDiff) {
        this.hateDiff = hateDiff;
    }
}
