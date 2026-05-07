package com.mybilibili.interact.services.impl;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserActionVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.interact.consumer.UserInfoClient;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.enums.VideoCommentTypeEnum;
import com.mybilibili.interact.entity.po.UserCommentAction;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.query.VideoCommentQuery;
import com.mybilibili.interact.entity.vo.VideoCommentVO;
import com.mybilibili.interact.mappers.UserCommentActionMapper;
import com.mybilibili.interact.mappers.VideoCommentMapper;
import com.mybilibili.interact.services.VideoCommentService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论 Service。
 */
@Service("VideoCommentService")
public class VideoCommentServiceImpl implements VideoCommentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoCommentServiceImpl.class);

    @Resource
    private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;

    @Resource
    private UserCommentActionMapper<UserCommentAction, UserActionQuery> userCommentActionMapper;

    @Resource
    private UserInfoClient userInfoClient;
    @Resource
    private VideoInfoClient videoInfoClient;

    @Override
    public List<VideoComment> findListByParam(VideoCommentQuery param) {
        if (Boolean.TRUE.equals(param.getQueryChildren())) {
            return selectListWithChildren(param);
        }
        return videoCommentMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(VideoCommentQuery param) {
        return videoCommentMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<VideoComment> findListByPage(VideoCommentQuery param) {
        Integer count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);

        List<VideoComment> list = Boolean.TRUE.equals(param.getQueryChildren())
                ? selectListWithChildren(param)
                : videoCommentMapper.selectList(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(VideoComment bean) {
        return videoCommentMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoCommentMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<VideoComment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return videoCommentMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postComment(VideoComment videoComment) {
        // TODO 后续改为调用 video 服务内部接口校验视频是否存在、评论区是否开启，并取得 UP 主 userId。
        // 当前不能信任前端传 videoUserId，否则评论归属和删除权限都会出错，所以先明确阻断写入。
        throw new BusinessException("评论发布依赖 video 服务返回 UP 主信息，后续接入 Feign 后开放");
    }

    @Override
    public List<VideoComment> selectListWithChildren(VideoCommentQuery param) {
        return videoCommentMapper.selectListWithChildren(param);
    }

    @Override
    public VideoCommentVO loadComment(String videoId, Integer pageNo, Integer orderType, TokenUserInfoDTO tokenUserInfo) {

        if (!checkCommentOpen(videoId)) {
            return new VideoCommentVO();
        }

        VideoCommentVO videoCommentVO = new VideoCommentVO();

        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setPCommentId(Constants.ZERO);
        commentQuery.setVideoId(videoId);
        commentQuery.setPageNo(pageNo);
        commentQuery.setQueryChildren(true);
        commentQuery.setOrderBy(orderType == null || orderType == 0 ? "like_count desc, comment_id desc" : "comment_id desc");

        PaginationResultVO<VideoComment> commentData = findListByPage(commentQuery);
        List<VideoComment> allCommentData = commentData.getList();
        List<VideoComment> topComment = getTopComment(videoId);

        if (allCommentData != null && !allCommentData.isEmpty() && topComment != null && !topComment.isEmpty()) {
            List<VideoComment> finalList = allCommentData.stream()
                    .filter(item -> !item.getCommentId().equals(topComment.get(0).getCommentId()))
                    .collect(Collectors.toList());
            finalList.addAll(0, topComment);
            commentData.setList(finalList);
        }

        videoCommentVO.setCommentData(commentData);
        videoCommentVO.setUserActionList(Collections.emptyList());

        if (tokenUserInfo != null) {
            UserActionQuery actionQuery = new UserActionQuery();
            actionQuery.setUserActionTypeList(new Integer[]{
                    UserActionTypeEnum.COMMENT_LIKE.getType(),
                    UserActionTypeEnum.COMMENT_HATE.getType()
            });
            actionQuery.setUserId(tokenUserInfo.getUserId());
            actionQuery.setVideoId(videoId);
            List<UserActionVO> actionVOList = userCommentActionMapper.selectActionTypeList(actionQuery);
            videoCommentVO.setUserActionList(actionVOList);
        }

        fillCommentUserInfo(commentData.getList());
        return videoCommentVO;
    }

    @Override
    public List<VideoComment> loadCommentUCenter(VideoCommentQuery videoCommentQuery) {
        // TODO 后续改为调用 video 服务批量补齐 videoName、videoCover。
        return videoCommentMapper.selectList(videoCommentQuery);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteByCommentId(Integer commentId, Boolean isAdmin, String userId) {
        VideoComment videoComment = Optional.ofNullable(videoCommentMapper.selectByCommentId(commentId))
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

        // TODO 后续调用 video 服务查询 UP 主信息后，补回“UP 主可删除自己视频下评论”的权限。
        boolean canDirectDelete = Boolean.TRUE.equals(isAdmin) || videoComment.getUserId().equals(userId);
        if (!canDirectDelete) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return videoCommentMapper.deleteByCommentId(commentId);
    }

    private List<VideoComment> getTopComment(String videoId) {
        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setTopType(VideoCommentTypeEnum.TOP.getType());
        commentQuery.setVideoId(videoId);
        commentQuery.setQueryChildren(true);
        return findListByParam(commentQuery);
    }

    /**
     * 评论区开关由 video 服务负责判定，interact 不直接读取 video_info。
     * 这里只兜住远程调用异常，评论查询本身的数据库异常继续按原异常链暴露，便于排查真实问题。
     */
    private boolean checkCommentOpen(String videoId) {
        try {
            Boolean commentOpen = videoInfoClient.checkVideoCommentStatusByVideoId(videoId);
            return Boolean.TRUE.equals(commentOpen);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("查询视频评论区状态失败，videoId:{}", videoId, e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     * 批量回填评论区展示用的用户信息。
     * 评论树当前只有一级评论和子评论两层，但这里按递归写，后续扩展楼中楼层级时不用改主流程。
     */
    private void fillCommentUserInfo(List<VideoComment> commentList) {
        if (commentList == null || commentList.isEmpty()) {
            return;
        }

        Set<String> userIds = new HashSet<>();
        for (VideoComment comment : commentList) {
            collectCommentUserIds(comment, userIds);
        }
        if (userIds.isEmpty()) {
            return;
        }

        List<UserInfoDTO> userInfoList;
        try {
            userInfoList = userInfoClient.getUserInfoByIds(new ArrayList<>(userIds));
        } catch (Exception e) {
            // 用户信息只是评论列表的展示增强，user 服务短暂不可用时不应该影响评论内容返回。
            LOGGER.warn("批量查询评论用户信息失败，userIdCount:{}", userIds.size(), e);
            return;
        }

        if (userInfoList == null || userInfoList.isEmpty()) {
            return;
        }

        Map<String, UserInfoDTO> userInfoMap = userInfoList.stream()
                .filter(userInfo -> userInfo.getUserId() != null)
                .collect(Collectors.toMap(UserInfoDTO::getUserId, userInfo -> userInfo, (oldValue, newValue) -> oldValue));
        for (VideoComment comment : commentList) {
            fillSingleCommentUserInfo(comment, userInfoMap);
        }
    }

    /**
     * 同时收集评论发布人和被回复人。
     * replyUserId 只用于展示“回复某人”的昵称，也必须纳入同一次批量查询，避免子评论再触发额外远程调用。
     */
    private void collectCommentUserIds(VideoComment comment, Set<String> userIds) {
        if (comment == null) {
            return;
        }
        addUserId(userIds, comment.getUserId());
        addUserId(userIds, comment.getReplyUserId());

        List<VideoComment> children = comment.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (VideoComment child : children) {
            collectCommentUserIds(child, userIds);
        }
    }

    private void fillSingleCommentUserInfo(VideoComment comment, Map<String, UserInfoDTO> userInfoMap) {
        if (comment == null) {
            return;
        }

        UserInfoDTO userInfo = userInfoMap.get(comment.getUserId());
        if (userInfo != null) {
            comment.setNickName(userInfo.getNickName());
            comment.setAvatar(userInfo.getAvatar());
        }

        UserInfoDTO replyUserInfo = userInfoMap.get(comment.getReplyUserId());
        if (replyUserInfo != null) {
            comment.setReplyNickName(replyUserInfo.getNickName());
        }

        List<VideoComment> children = comment.getChildren();
        if (children == null || children.isEmpty()) {
            return;
        }
        for (VideoComment child : children) {
            fillSingleCommentUserInfo(child, userInfoMap);
        }
    }

    private void addUserId(Set<String> userIds, String userId) {
        if (userId != null && !userId.isBlank()) {
            userIds.add(userId);
        }
    }
}
