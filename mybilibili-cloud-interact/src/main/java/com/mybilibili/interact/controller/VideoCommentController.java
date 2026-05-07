package com.mybilibili.interact.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.interact.entity.dto.VideoCommentDTO;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.vo.VideoCommentVO;
import com.mybilibili.interact.services.VideoCommentService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 播放页评论接口。
 */
@RestController
@RequestMapping("comment")
public class VideoCommentController extends ABaseController {

    @Resource
    private VideoCommentService videoCommentService;

    /**
     * 加载视频评论。
     *
     * @param videoId   视频 id
     * @param pageNo    页码
     * @param orderType 0 或空按点赞数排序，其他值按发布时间排序
     * @return 评论分页数据和当前登录用户的评论动作
     */
    @RequestMapping("loadComment")
    public ResponseVO loadComment(@NotEmpty String videoId, Integer pageNo, @NotNull Integer orderType) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        VideoCommentVO videoCommentVO = videoCommentService.loadComment(videoId, pageNo, orderType, tokenUserInfo);
        return getSuccessResponseVO(videoCommentVO);
    }

    /**
     * 发布评论。
     */
    @RequestMapping("postComment")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO postComment(@Validated VideoCommentDTO videoCommentDTO) {
        VideoComment videoComment = BeanUtil.toBean(videoCommentDTO, VideoComment.class);
        videoComment.setUserId(getTokenUserInfo().getUserId());
        videoComment.setPostTime(new Date());
        videoCommentService.postComment(videoComment);
        return getSuccessResponseVO(videoComment);
    }

    @RequestMapping("/userDelComment")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO userDelComment(@NotNull Integer commentId) {
        videoCommentService.deleteByCommentId(commentId, false, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }
}
