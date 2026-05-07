package com.mybilibili.interact.controller;

import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoCommentQuery;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;
import com.mybilibili.interact.services.VideoCommentService;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户中心里的互动管理接口。
 */
@RestController
@LoginInterceptor(checkLogin = true)
@RequestMapping("/ucenter")
public class InteractUCenterController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoId) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setQueryChildren(false);
        videoCommentQuery.setQueryUserInfo(false);
        videoCommentQuery.setPageSize(pageSize);
        videoCommentQuery.setUserId(getTokenUserInfo().getUserId());
        videoCommentQuery.setOrderBy("v.comment_id desc");
        PaginationResultVO<VideoComment> listByPage = videoCommentService.findListByPage(videoCommentQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoId) {
        VideoDanmuQuery videoDanmuQuery = new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setPageSize(pageSize);
        videoDanmuQuery.setUserId(getTokenUserInfo().getUserId());
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuQuery.setQueryUserInfo(false);
        videoDanmuQuery.setOrderBy("v.time asc");
        PaginationResultVO<VideoDanmu> list = videoDanmuService.findListByPage(videoDanmuQuery);
        return getSuccessResponseVO(list);
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId) {
        videoCommentService.deleteByCommentId(commentId, false, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        videoDanmuService.deleteVideoDanmuByDanmuId(danmuId, false, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadCommentByVideo")
    public ResponseVO loadCommentByVideo(Integer pageNo, Integer pageSize, @NotEmpty String videoId) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setPageSize(pageSize);
        videoCommentQuery.setQueryChildren(false);
        videoCommentQuery.setQueryUserInfo(false);
        videoCommentQuery.setOrderBy("v.comment_id desc");
        return getSuccessResponseVO(videoCommentService.findListByPage(videoCommentQuery));
    }
}
