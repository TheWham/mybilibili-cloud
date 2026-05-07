package com.mybilibili.interact.entity.vo;

import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.interact.entity.po.VideoComment;

import java.util.List;

public class VideoCommentVO {

    private PaginationResultVO<VideoComment> commentData;
    private List<UserActionVO> userActionList;
    private Boolean showReply;

    public PaginationResultVO<VideoComment> getCommentData() {
        return commentData;
    }

    public void setCommentData(PaginationResultVO<VideoComment> commentData) {
        this.commentData = commentData;
    }

    public List<UserActionVO> getUserActionList() {
        return userActionList;
    }

    public void setUserActionList(List<UserActionVO> userActionList) {
        this.userActionList = userActionList;
    }

    public Boolean getShowReply() {
        return showReply;
    }

    public void setShowReply(Boolean showReply) {
        this.showReply = showReply;
    }
}
