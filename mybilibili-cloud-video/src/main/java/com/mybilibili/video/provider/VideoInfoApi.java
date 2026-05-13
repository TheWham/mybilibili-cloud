package com.mybilibili.video.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.*;
import com.mybilibili.base.entity.vo.*;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.VideoStatusEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.consumer.UserVideoActionClient;
import com.mybilibili.video.entity.dto.SeriesWithVideoQueryDTO;
import com.mybilibili.video.entity.dto.UserVideoSeriesVideoQueryDTO;
import com.mybilibili.video.entity.enums.VideoOrderTypeEnum;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.po.VideoInfoPost;
import com.mybilibili.video.entity.query.VideoInfoFilePostQuery;
import com.mybilibili.video.entity.query.VideoInfoPostQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.services.UserVideoSeriesService;
import com.mybilibili.video.services.VideoInfoFilePostService;
import com.mybilibili.video.services.VideoInfoPostService;
import com.mybilibili.video.services.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class VideoInfoApi{

    private static final String CLOSE_COMMENT_INTERACTION = Constants.ONE.toString();
    private static final String CLOSE_DANMU_INTERACTION = Constants.TWO.toString();

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserVideoActionClient userVideoActionClient;

    @Resource
    private UserVideoSeriesService userVideoSeriesService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoPostService videoInfoPostService;


    @RequestMapping( "/loadVideoList")
    PaginationResultVO<VideoInfoUHomeVO> loadVideoList(@RequestParam("userId") String userId,
                             @RequestParam(value = "type", required = false) Integer type,
                             @RequestParam(value = "pageNo", required = false) Integer pageNo,
                             @RequestParam(value = "videoName", required = false) String videoName,
                             @RequestParam(value = "orderType", required = false) Integer orderType)
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setVideoName(videoName);
        if (type != null) {
            videoInfoQuery.setPageSize(PageSize.SIZE10.getSize());
        }

        VideoOrderTypeEnum typeEnum = VideoOrderTypeEnum.getEnum(orderType);
        if (typeEnum == null) {
            typeEnum = VideoOrderTypeEnum.ORDER_POST_TIME;
        }
        videoInfoQuery.setOrderBy(typeEnum.getField() + " desc");

        PaginationResultVO<VideoInfo> listVideo = videoInfoService.findListByPage(videoInfoQuery);
        PaginationResultVO<VideoInfoUHomeVO> videoListVO = new PaginationResultVO<>();
        videoListVO.setPageNo(listVideo.getPageNo());
        videoListVO.setPageSize(listVideo.getPageSize());
        videoListVO.setPageTotal(listVideo.getPageTotal());
        videoListVO.setTotalCount(listVideo.getTotalCount());
        videoListVO.setList(BeanUtil.copyToList(listVideo.getList(), VideoInfoUHomeVO.class));
        return videoListVO;
    }

    @RequestMapping("/series/loadVideoSeriesWithVideo")
    public List<SeriesWithVideoUHomeVO> loadVideoSeriesWithVideo(@RequestParam("userId") String userId) {
        List<SeriesWithVideoQueryDTO> seriesList = userVideoSeriesService.selectVideoSeriesWithVideo(userId);
        if (seriesList == null || seriesList.isEmpty()) {
            return Collections.emptyList();
        }
        return seriesList.stream().map(this::toUHomeVO).collect(Collectors.toList());
    }

    @RequestMapping("/series/loadVideoSeries")
    public List<UserVideoSeriesVO> loadVideoSeries(@RequestParam("userId") String userId) {
        List<UserVideoSeriesDTO> videoSeriesList = userVideoSeriesService.loadVideoSeries(userId);
        if (videoSeriesList == null || videoSeriesList.isEmpty()) {
            return Collections.emptyList();
        }
        return videoSeriesList.stream().map(this::toSeriesVO).collect(Collectors.toList());
    }

    @RequestMapping("/loadUserCollection")
    public PaginationResultVO<UserCollectionVO> loadUserCollection(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                     @RequestParam("userId") String userId
    )
    {
        PaginationResultVO<UserCollectionVO> userCollectionVideoPage = userVideoActionClient.getUserCollectionVideoList(pageNo, userId);
        List<UserCollectionVO> list = userCollectionVideoPage.getList();

        if (list == null || list.isEmpty())
            return new PaginationResultVO<UserCollectionVO>();

        Map<String, Date> videoIdTimeMap = list.stream()
        .collect(Collectors.toMap(UserCollectionVO::getVideoId, UserCollectionVO::getActionTime, (left, right) -> left));
        // 收藏页要保持“收藏时间倒序”的展示顺序，不能直接按数据库 in 查询结果返回。
        List<String> userCollectionIds = list.stream().map(UserCollectionVO::getVideoId).collect(Collectors.toList());
        List<VideoInfo> unOrderVideoInfos = videoInfoService.selectByIds(userCollectionIds);
        Map<String, VideoInfo> videoCollectMap = unOrderVideoInfos.stream().collect(Collectors.toMap(VideoInfo::getVideoId, videoInfo -> videoInfo));
        List<VideoInfo> finalOrderVideoList = userCollectionIds.stream()
                .map(videoCollectMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<UserCollectionVO> userCollectionVOList = BeanUtil.copyToList(finalOrderVideoList, UserCollectionVO.class);
        userCollectionVOList.forEach(userCollectionVO -> userCollectionVO.setActionTime(videoIdTimeMap.get(userCollectionVO.getVideoId())));

        PaginationResultVO<UserCollectionVO> userCollectionPage = new PaginationResultVO<>();
        userCollectionPage.setTotalCount(userCollectionVideoPage.getTotalCount());
        userCollectionPage.setPageTotal(userCollectionVideoPage.getPageTotal());
        userCollectionPage.setPageSize(userCollectionVideoPage.getPageSize());
        userCollectionPage.setPageNo(userCollectionVideoPage.getPageNo());
        userCollectionPage.setList(userCollectionVOList);
        return userCollectionPage;
    }

    /**
     * 用户中心投稿列表。
     *
     * <p>这里返回 base 模块里的 DTO，不把 video 模块的投稿 PO 暴露给 user 服务。
     * 微服务之间只认稳定契约，表字段调整时就不会直接影响调用方。</p>
     */
    @RequestMapping("/ucenter/loadVideoList")
    public PaginationResultVO<VideoInfoPostDTO> loadUCenterVideoList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                                     @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy,
                                                                     @RequestParam(value = "status", required = false) Integer status,
                                                                     @RequestParam("userId") String userId) {
        VideoInfoPostQuery query = new VideoInfoPostQuery();
        query.setUserId(userId);
        query.setPageNo(pageNo);
        query.setVideoNameFuzzy(videoNameFuzzy);
        query.setOrderBy("v.create_time desc");
        query.setQueryCountInfo(true);
        if (status != null && status == -1) {
            query.setExcludeStatusArray(new Integer[]{VideoStatusEnum.STATUS_3.getStatus(), VideoStatusEnum.STATUS_4.getStatus()});
        } else {
            query.setStatus(status);
        }

        PaginationResultVO<VideoInfoPost> page = videoInfoPostService.findListByPage(query);
        return copyPage(page, VideoInfoPostDTO.class);
    }

    /**
     * 查询当前用户投稿在各审核状态下的数量。
     */
    @RequestMapping("/ucenter/getVideoCountInfo")
    public VideoAuditCountVO getVideoCountInfo(@RequestParam("userId") String userId) {
        com.mybilibili.video.entity.vo.VideoAuditCountVO countInfo = videoInfoPostService.getVideoCountInfo(userId);
        return BeanUtil.toBean(countInfo, VideoAuditCountVO.class);
    }

    /**
     * 查询投稿编辑页回显数据。
     *
     * <p>权限校验放在 video 服务内做，避免调用方绕过 Feign 直接拼业务数据。</p>
     */
    @RequestMapping("/ucenter/getVideoByVideoId")
    public VideoInfoPostEditVO getVideoByVideoId(@RequestParam("videoId") String videoId,
                                                 @RequestParam("userId") String userId) {
        VideoInfoPost videoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !userId.equals(videoInfoPost.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

        VideoInfoFilePostQuery fileQuery = new VideoInfoFilePostQuery();
        fileQuery.setVideoId(videoId);
        fileQuery.setUserId(userId);
        fileQuery.setOrderBy("file_index asc");
        List<VideoInfoFilePost> fileList = videoInfoFilePostService.findListByParam(fileQuery);

        VideoInfoPostEditVO editVO = new VideoInfoPostEditVO();
        editVO.setVideoInfo(BeanUtil.toBean(videoInfoPost, VideoInfoPostDTO.class));
        editVO.setVideoInfoFileList(BeanUtil.copyToList(fileList, VideoInfoFilePostDTO.class));
        return editVO;
    }

    /**
     * 保存投稿信息。userId 来自调用方登录态，video 服务只负责落库和投稿业务处理。
     */
    @RequestMapping("/ucenter/postVideo")
    public void postVideo(@RequestBody VideoInfoPostDTO videoInfoPostDTO) {
        videoInfoPostService.savePostVideoInfo(videoInfoPostDTO);
    }

    /**
     * 删除用户自己的投稿视频。
     */
    @RequestMapping("/ucenter/deleteVideo")
    public void deleteVideo(@RequestParam("videoId") String videoId,
                            @RequestParam("userId") String userId) {
        videoInfoFilePostService.deleVideo(videoId, userId, false);
    }

    /**
     * 保存视频互动设置，先校验视频归属，避免跨用户修改。
     */
    @RequestMapping("/ucenter/saveVideoInteraction")
    public void saveVideoInteraction(@RequestParam("videoId") String videoId,
                                     @RequestParam("userId") String userId,
                                     @RequestParam(value = "interaction", required = false) String interaction) {
        VideoInfoPost videoInfoPost = videoInfoPostService.getVideoInfoPostByVideoId(videoId);
        if (videoInfoPost == null || !userId.equals(videoInfoPost.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        videoInfoPost.setInteraction(interaction);
        videoInfoPostService.saveVideoInteraction(videoInfoPost);
    }

    @RequestMapping("/ucenter/getVideoListByIds")
    public List<VideoInfoDTO> getVideoListByIds(@RequestParam("videoIds") List<String> videoIds, @RequestParam(value = "userId") String userId)
    {
        String[] arrayIds = videoIds.toArray(String[]::new);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        videoInfoQuery.setArrayIds(arrayIds);
        List<VideoInfo> videoInfos = videoInfoService.findListByParam(videoInfoQuery);
        List<VideoInfoDTO> videoInfoDTOS = BeanUtil.copyToList(videoInfos, VideoInfoDTO.class);
        return videoInfoDTOS;
    }

    @RequestMapping("/message/selectVideoInfoByIds")
    public List<VideoInfoDTO> selectVideoInfoByIds(@RequestParam("videoIds") List<String> videoIds)
    {
        if (videoIds == null || videoIds.isEmpty())
            return Collections.emptyList();

        String[] arrayIds = videoIds.toArray(String[]::new);
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setArrayIds(arrayIds);
        List<VideoInfo> videoInfos = videoInfoService.findListByParam(videoInfoQuery);
        List<VideoInfoDTO> videoInfoDTOS = BeanUtil.copyToList(videoInfos, VideoInfoDTO.class);
        return videoInfoDTOS;
    }

    /**
     * 查询当前用户的全部公开视频，用于用户中心下拉选择等轻量场景。
     */
    @RequestMapping("/ucenter/loadAllVideo")
    public List<VideoInfoDTO> loadAllVideo(@RequestParam("userId") String userId) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setUserId(userId);
        return BeanUtil.copyToList(videoInfoService.findListByParam(videoInfoQuery), VideoInfoDTO.class);
    }

    /**
     * 分页提供搜索索引源数据。
     *
     * <p>search 服务重建索引时只需要正式视频表的数据，分页拉取可以避免一次性加载过多视频。
     * 这里仍然返回 base 模块 DTO，不把 video 的 PO 暴露给 search。</p>
     */
    @RequestMapping("/search/loadVideoIndexSource")
    public PaginationResultVO<VideoInfoDTO> loadVideoIndexSource(@RequestParam("pageNo") Integer pageNo,
                                                                 @RequestParam("pageSize") Integer pageSize) {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setPageSize(pageSize);
        videoInfoQuery.setOrderBy("v.create_time asc");
        PaginationResultVO<VideoInfo> page = videoInfoService.findListByPage(videoInfoQuery);
        return copyPage(page, VideoInfoDTO.class);
    }

    @RequestMapping("/collection/loadVideoInfo")
    public List<UserCollectionVO> loadCollectionVideoInfo(@RequestParam("videoIds") List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<VideoInfo> unOrderVideoInfos = videoInfoService.selectByIds(videoIds);

        if (unOrderVideoInfos == null || unOrderVideoInfos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, VideoInfo> videoCollectMap = unOrderVideoInfos.stream()
                .collect(Collectors.toMap(VideoInfo::getVideoId, videoInfo -> videoInfo));

        List<VideoInfo> finalOrderVideoList = videoIds.stream()
                .map(videoCollectMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<UserCollectionVO> userCollectionVOList = BeanUtil.copyToList(finalOrderVideoList, UserCollectionVO.class);

        return userCollectionVOList;
    }

    /**
     * 根据视频 id 查询视频基础信息。
     *
     * <p>给 interact 等内部服务补齐视频作者、标题等归属信息使用。这里统一在 video 服务查，
     * 避免调用方直接依赖 video 表结构。</p>
     */
    @RequestMapping("/getVideoInfoByVideoId")
    public VideoInfoDTO getVideoInfoByVideoId(@RequestParam("videoId") String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return BeanUtil.toBean(videoInfo, VideoInfoDTO.class);
    }

    /**
     * 根据视频 id 查询视频信息。
     *
     * @param videoId 视频 id
     * @return 视频状态 以及评论区是否开启
     */
    @RequestMapping("/checkVideoCommentStatusByVideoId")
    public Boolean checkVideoCommentStatusByVideoId(@RequestParam("videoId") String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);

        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // interaction 是投稿侧保存的互动开关，当前约定包含 1 表示关闭评论。
        return !isInteractionClosed(videoInfo.getInteraction(), CLOSE_COMMENT_INTERACTION);
    }

    /**
     * 根据视频 id 查询弹幕开关。
     *
     * <p>弹幕是否允许发送由 video 服务维护，interact 只消费这个结论，
     * 避免多个服务各自解释 interaction 字段。</p>
     *
     * @param videoId 视频 id
     * @return true：允许弹幕；false：关闭弹幕
     */
    @RequestMapping("/checkVideoDanmuStatusByVideoId")
    public Boolean checkVideoDanmuStatusByVideoId(@RequestParam("videoId") String videoId) {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // 当前约定包含 2 表示关闭弹幕，后续如果 interaction 改成结构化配置，只需要改这里。
        return !isInteractionClosed(videoInfo.getInteraction(), CLOSE_DANMU_INTERACTION);
    }

    /**
     * 汇总 UP 主所有公开视频的播放量和点赞量。
     *
     * <p>user 服务只需要展示统计值，不应该直接读取 video_info 表。</p>
     *
     * @param userId UP 主用户 id
     * @return 视频统计汇总
     */
    @RequestMapping("/countVideoInfoByUserId")
    public VideoCountDTO countVideoInfoByUserId(@RequestParam("userId") String userId) {
        VideoCountDTO videoCountDTO = videoInfoService.sumVideoCountByUserId(userId);
        return videoCountDTO == null ? new VideoCountDTO() : videoCountDTO;
    }


    @RequestMapping("/getFilePath")
    public String getFilePath(@RequestParam("fileId") String fileId)
    {
        VideoInfoFilePost filePostByFileId = videoInfoFilePostService.getVideoInfoFilePostByFileId(fileId);
        String filePath = filePostByFileId.getFilePath();
        return filePath;
    }


    private UserVideoSeriesVO toSeriesVO(UserVideoSeriesDTO userVideoSeriesDTO)
    {
        UserVideoSeriesVO bean = BeanUtil.toBean(userVideoSeriesDTO, UserVideoSeriesVO.class);
        return bean;
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

    private SeriesWithVideoUHomeVO toUHomeVO(SeriesWithVideoQueryDTO queryDTO) {
        SeriesWithVideoUHomeVO vo = new SeriesWithVideoUHomeVO();
        vo.setSeriesId(queryDTO.getSeriesId());
        vo.setSeriesName(queryDTO.getSeriesName());

        List<UserVideoSeriesVideoQueryDTO> videoList = queryDTO.getVideoInfoList();
        if (videoList == null || videoList.isEmpty()) {
            vo.setVideoInfoList(Collections.emptyList());
            return vo;
        }
        vo.setVideoInfoList(videoList.stream().map(this::toVideoVO).collect(Collectors.toList()));
        return vo;
    }

    private UserVideoSeriesVideoVO toVideoVO(UserVideoSeriesVideoQueryDTO queryDTO) {
        UserVideoSeriesVideoVO vo = new UserVideoSeriesVideoVO();
        vo.setSeriesId(queryDTO.getSeriesId());
        vo.setVideoId(queryDTO.getVideoId());
        vo.setUserId(queryDTO.getUserId());
        vo.setSort(queryDTO.getSort());
        vo.setVideoName(queryDTO.getVideoName());
        vo.setVideoCover(queryDTO.getVideoCover());
        vo.setPlayCount(queryDTO.getPlayCount());
        vo.setCreateTime(queryDTO.getCreateTime());
        return vo;
    }

    private boolean isInteractionClosed(String interaction, String closeType) {
        return interaction != null && closeType != null && interaction.contains(closeType);
    }

}
