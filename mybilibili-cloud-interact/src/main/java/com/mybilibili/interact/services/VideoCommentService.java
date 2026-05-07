package com.mybilibili.interact.services;

import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.query.VideoCommentQuery;
import com.mybilibili.interact.entity.vo.VideoCommentVO;

import java.util.List;

/**
 * 评论 Service。
 */
public interface VideoCommentService {

    List<VideoComment> findListByParam(VideoCommentQuery param);

    Integer findCountByParam(VideoCommentQuery param);

    PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param);

    Integer add(VideoComment bean);

    Integer addBatch(List<VideoComment> listBean);

    Integer addOrUpdateBatch(List<VideoComment> listBean);

    void postComment(VideoComment videoComment);

    List<VideoComment> selectListWithChildren(VideoCommentQuery param);

    VideoCommentVO loadComment(String videoId, Integer pageNo, Integer orderType, TokenUserInfoDTO tokenUserInfoDTO);

    List<VideoComment> loadCommentUCenter(VideoCommentQuery videoCommentQuery);

    Integer deleteByCommentId(Integer commentId, Boolean isAdmin, String userId);
}
