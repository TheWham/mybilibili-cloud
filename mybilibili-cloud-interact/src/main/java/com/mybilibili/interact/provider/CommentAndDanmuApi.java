package com.mybilibili.interact.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInteractCountDTO;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import com.mybilibili.base.entity.vo.VideoCommentInUCenterVO;
import com.mybilibili.base.entity.vo.VideoDanmuVO;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.interact.consumer.UserInfoClient;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoCommentQuery;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;
import com.mybilibili.interact.services.UserVideoActionService;
import com.mybilibili.interact.services.VideoCommentService;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    @Resource
    private UserVideoActionService userVideoActionService;
    @Resource
    private VideoInfoClient videoInfoClient;


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
        fillCommentVideoInfo(page.getList());
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
        fillCommentVideoInfo(page.getList());
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
        fillDanmuVideoInfo(page.getList());
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

    /**
     * 汇总用户作为 UP 主收到的互动数据。
     *
     * <p>评论、弹幕和用户行为表都属于 interact 模块，user 服务只通过这个内部接口
     * 拿创作者中心需要展示的结果，避免跨模块读表。</p>
     *
     * @param userId UP 主用户 id
     * @return 互动统计汇总
     */
    @RequestMapping("/countUserInteractByUserId")
    public UserInteractCountDTO countUserInteractByUserId(@RequestParam("userId") String userId) {
        UserInteractCountDTO countDTO = new UserInteractCountDTO();

        VideoCommentQuery commentQuery = new VideoCommentQuery();
        commentQuery.setVideoUserId(userId);
        countDTO.setCommentCount(defaultValue(videoCommentService.findCountByParam(commentQuery)));

        VideoDanmuQuery danmuQuery = new VideoDanmuQuery();
        danmuQuery.setVideoUserId(userId);
        countDTO.setDanmuCount(defaultValue(videoDanmuService.findCountByParam(danmuQuery)));

        countDTO.setCoinCount(defaultValue(userVideoActionService.sumCoinCount(userId)));

        UserActionQuery collectQuery = new UserActionQuery();
        collectQuery.setVideoUserId(userId);
        collectQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
        countDTO.setCollectCount(defaultValue(userVideoActionService.findCountByParam(collectQuery)));
        return countDTO;
    }

    @RequestMapping("/admin/interact/loadComment")
    public PaginationResultVO<VideoCommentInUCenterVO> adminLoadComment(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                                        @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                        @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy) {
        VideoCommentQuery query = new VideoCommentQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setQueryChildren(false);
        query.setQueryUserInfo(true);
        query.setOrderBy("v.post_time desc");

        PaginationResultVO<VideoComment> page = videoCommentService.findListByPage(query);
        fillCommentUserInfo(page.getList());
        fillCommentVideoInfo(page.getList());
        filterCommentByVideoName(page, videoNameFuzzy);
        return copyPage(page, VideoCommentInUCenterVO.class);
    }

    @RequestMapping("/admin/interact/loadDanmu")
    public PaginationResultVO<VideoDanmuVO> adminLoadDanmu(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                          @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy) {
        VideoDanmuQuery query = new VideoDanmuQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setQueryUserInfo(true);
        query.setOrderBy("v.post_time desc");

        PaginationResultVO<VideoDanmu> page = videoDanmuService.findListByPage(query);
        fillUserName4DanmuList(page.getList());
        fillDanmuVideoInfo(page.getList());
        filterDanmuByVideoName(page, videoNameFuzzy);
        return copyPage(page, VideoDanmuVO.class);
    }

    @RequestMapping("/admin/interact/delComment")
    public void adminDelComment(@RequestParam("commentId") Integer commentId) {
        Integer count = videoCommentService.deleteByCommentId(commentId, true, null);
        if (count == null || count == 0) {
            throw new BusinessException("删除评论失败");
        }
    }

    @RequestMapping("/admin/interact/delDanmu")
    public void adminDelDanmu(@RequestParam("danmuId") Integer danmuId) {
        Integer count = videoDanmuService.deleteVideoDanmuByDanmuId(danmuId, true, null);
        if (count == null || count == 0) {
            throw new BusinessException("删除弹幕失败");
        }
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

    private void fillCommentVideoInfo(List<VideoComment> commentList) {
        if (commentList == null || commentList.isEmpty()) {
            return;
        }
        Map<String, UserCollectionVO> videoInfoMap = loadVideoInfoMap(commentList.stream()
                .map(VideoComment::getVideoId)
                .filter(videoId -> videoId != null && !videoId.isBlank())
                .distinct()
                .toList());
        if (videoInfoMap.isEmpty()) {
            return;
        }
        for (VideoComment comment : commentList) {
            fillSingleCommentVideoInfo(comment, videoInfoMap);
        }
    }

    private void fillSingleCommentVideoInfo(VideoComment comment, Map<String, UserCollectionVO> videoInfoMap) {
        if (comment == null || comment.getVideoId() == null) {
            return;
        }
        UserCollectionVO videoInfo = videoInfoMap.get(comment.getVideoId());
        if (videoInfo != null) {
            comment.setVideoName(videoInfo.getVideoName());
            comment.setVideoCover(videoInfo.getVideoCover());
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

    private void fillDanmuVideoInfo(List<VideoDanmu> videoDanmuList) {
        if (videoDanmuList == null || videoDanmuList.isEmpty()) {
            return;
        }
        Map<String, UserCollectionVO> videoInfoMap = loadVideoInfoMap(videoDanmuList.stream()
                .map(VideoDanmu::getVideoId)
                .filter(videoId -> videoId != null && !videoId.isBlank())
                .distinct()
                .toList());
        if (videoInfoMap.isEmpty()) {
            return;
        }
        for (VideoDanmu videoDanmu : videoDanmuList) {
            UserCollectionVO videoInfo = videoInfoMap.get(videoDanmu.getVideoId());
            if (videoInfo != null) {
                videoDanmu.setVideoName(videoInfo.getVideoName());
            }
        }
    }

    private Map<String, UserCollectionVO> loadVideoInfoMap(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserCollectionVO> videoInfoList;
        try {
            videoInfoList = videoInfoClient.loadCollectionVideoInfo(videoIds);
        } catch (Exception e) {
            // 视频标题、封面只是用户中心列表展示字段，video 短暂不可用时保留评论/弹幕主体数据。
            LOGGER.warn("批量查询视频展示信息失败，videoIdCount:{}", videoIds.size(), e);
            return Collections.emptyMap();
        }
        if (videoInfoList == null || videoInfoList.isEmpty()) {
            return Collections.emptyMap();
        }
        return videoInfoList.stream()
                .filter(videoInfo -> videoInfo.getVideoId() != null)
                .collect(Collectors.toMap(UserCollectionVO::getVideoId, videoInfo -> videoInfo, (left, right) -> left));
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

    private Integer defaultValue(Integer value) {
        return value == null ? 0 : value;
    }

    private void filterCommentByVideoName(PaginationResultVO<VideoComment> page, String videoNameFuzzy) {
        if (videoNameFuzzy == null || videoNameFuzzy.isBlank() || page.getList() == null) {
            return;
        }
        page.setList(page.getList().stream()
                .filter(comment -> comment.getVideoName() != null && comment.getVideoName().contains(videoNameFuzzy))
                .toList());
    }

    private void filterDanmuByVideoName(PaginationResultVO<VideoDanmu> page, String videoNameFuzzy) {
        if (videoNameFuzzy == null || videoNameFuzzy.isBlank() || page.getList() == null) {
            return;
        }
        page.setList(page.getList().stream()
                .filter(danmu -> danmu.getVideoName() != null && danmu.getVideoName().contains(videoNameFuzzy))
                .toList());
    }
}
