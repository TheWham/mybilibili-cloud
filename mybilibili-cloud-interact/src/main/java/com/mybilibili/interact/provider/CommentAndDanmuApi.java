package com.mybilibili.interact.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoCommentInUCenterVO;
import com.mybilibili.base.entity.vo.VideoDanmuVO;
import com.mybilibili.interact.consumer.UserInfoClient;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoCommentQuery;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;
import com.mybilibili.interact.services.VideoCommentService;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.nacos.client.utils.EnvUtil.LOGGER;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class CommentAndDanmuApi {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    @Resource
    private UserInfoClient userInfoClient;


    /**
     * 用户中心评论列表。
     *
     * <p>只返回 base 模块中的展示 VO，user 服务不需要知道 interact 的评论 PO。</p>
     */
    @RequestMapping("/loadComment")
    public PaginationResultVO<VideoCommentInUCenterVO> loadComment(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                                   @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                   @RequestParam(value = "videoId", required = false) String videoId,
                                                                   @RequestParam("userId") String userId) {
        VideoCommentQuery query = new VideoCommentQuery();
        query.setVideoId(videoId);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(userId);
        query.setQueryChildren(false);
        query.setQueryUserInfo(true);
        query.setOrderBy("v.comment_id desc");
        PaginationResultVO<VideoComment> page = videoCommentService.findListByPage(query);
        fillCommentUserInfo(page.getList());
        return copyPage(page, VideoCommentInUCenterVO.class);
    }

    @GetMapping("/loadCommentInUCenter")
    public PaginationResultVO<VideoCommentInUCenterVO> loadCommentInUCenter(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                                            @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                            @RequestParam(value = "videoId", required = false) String videoId,
                                                                            @RequestParam("userId") String userId
    ) {
        VideoCommentQuery videoCommentQuery = new VideoCommentQuery();
        videoCommentQuery.setVideoId(videoId);
        videoCommentQuery.setPageNo(pageNo);
        videoCommentQuery.setVideoUserId(userId);
        videoCommentQuery.setQueryUserInfo(false);
        videoCommentQuery.setPageSize(pageSize);
        videoCommentQuery.setOrderBy("v.comment_id desc");
        PaginationResultVO<VideoComment> page = videoCommentService.findListByPage(videoCommentQuery);
        fillCommentUserInfo(page.getList());
        return copyPage(page, VideoCommentInUCenterVO.class);
    }

    /**
     * 用户中心弹幕列表。
     */
    @RequestMapping("/loadDanmu")
    public PaginationResultVO<VideoDanmuVO> loadDanmu(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                      @RequestParam(value = "videoId", required = false) String videoId,
                                                      @RequestParam("userId") String userId) {
        VideoDanmuQuery query = new VideoDanmuQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setVideoId(videoId);
        query.setVideoUserId(userId);
        query.setQueryUserInfo(true);
        query.setOrderBy("v.time asc");
        PaginationResultVO<VideoDanmu> page = videoDanmuService.findListByPage(query);
        fillUserName4DanmuList(page.getList());
        return copyPage(page, VideoDanmuVO.class);
    }

    /**
     * 删除当前用户自己的评论。
     */
    @RequestMapping("/delComment")
    public void delComment(@RequestParam("commentId") Integer commentId,
                           @RequestParam("userId") String userId) {
        videoCommentService.deleteByCommentId(commentId, false, userId);
    }

    /**
     * 删除当前用户自己的弹幕。
     */
    @RequestMapping("/delDanmu")
    public void delDanmu(@RequestParam("danmuId") Integer danmuId,
                         @RequestParam("userId") String userId) {
        videoDanmuService.deleteVideoDanmuByDanmuId(danmuId, false, userId);
    }

    private <S, T> PaginationResultVO<T> copyPage(PaginationResultVO<S> sourcePage, Class<T> targetClass) {
        PaginationResultVO<T> targetPage = new PaginationResultVO<>();
        targetPage.setTotalCount(sourcePage.getTotalCount());
        targetPage.setPageSize(sourcePage.getPageSize());
        targetPage.setPageNo(sourcePage.getPageNo());
        targetPage.setPageTotal(sourcePage.getPageTotal());
        targetPage.setList(BeanUtil.copyToList(sourcePage.getList(), targetClass));
        return targetPage;
    }

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

    void fillUserName4DanmuList(List<VideoDanmu> videoDanmuList)
    {
        if (videoDanmuList == null || videoDanmuList.isEmpty()) {
            return;
        }

        List<String> userIds = videoDanmuList.stream()
                .map(VideoDanmu::getUserId)
                .filter(userId -> userId != null && !userId.isBlank())
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return;
        }

        List<UserInfoDTO> userInfoByIds;
        try {
            userInfoByIds = userInfoClient.getUserInfoByIds(userIds);
        } catch (Exception e) {
            // 昵称只是用户中心弹幕列表的展示字段，user 服务异常时不影响弹幕主体数据返回。
            LOGGER.warn("批量查询弹幕用户信息失败，userIdCount:{}", userIds.size(), e);
            return;
        }

        if (userInfoByIds == null || userInfoByIds.isEmpty()) {
            return;
        }

        Map<String, UserInfoDTO> userIdsMap =  userInfoByIds.stream().filter(userInfoDTO -> userInfoDTO.getUserId() != null)
                .collect(Collectors.toMap(UserInfoDTO::getUserId, userInfoDTO -> userInfoDTO, (item1, item2)->item1));

        for (VideoDanmu videoDanmu : videoDanmuList) {
            fillSingleDanmu(videoDanmu, userIdsMap);
        }
    }

    private void fillSingleDanmu(VideoDanmu videoDanmu, Map<String, UserInfoDTO> userIdsMap)
    {
        if (videoDanmu == null || videoDanmu.getUserId() == null) {
            return;
        }
        UserInfoDTO userInfo = userIdsMap.get(videoDanmu.getUserId());
        if (userInfo != null) {
            videoDanmu.setNickName(userInfo.getNickName());
        }
    }
}
