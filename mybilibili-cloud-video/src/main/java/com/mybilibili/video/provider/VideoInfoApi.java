package com.mybilibili.video.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserActionSyncDTO;
import com.mybilibili.base.entity.dto.UserVideoSeriesDTO;
import com.mybilibili.base.entity.vo.*;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.consumer.UserVideoActionClient;
import com.mybilibili.video.entity.dto.SeriesWithVideoQueryDTO;
import com.mybilibili.video.entity.dto.UserVideoSeriesVideoQueryDTO;
import com.mybilibili.video.entity.enums.VideoOrderTypeEnum;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.services.UserVideoSeriesService;
import com.mybilibili.video.services.VideoInfoFilePostService;
import com.mybilibili.video.services.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class VideoInfoApi{

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private UserVideoActionClient userVideoActionClient;

    @Resource
    private UserVideoSeriesService userVideoSeriesService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;


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
        PaginationResultVO<UserActionSyncDTO> userCollectionVideoPage = userVideoActionClient.getUserCollectionVideoList(pageNo, userId);
        List<UserActionSyncDTO> list = userCollectionVideoPage.getList();
        Map<String, Date> videoIdTimeMap = list.stream()
        .collect(Collectors.toMap(UserActionSyncDTO::getVideoId, UserActionSyncDTO::getActionTime, (left, right) -> left));
        // 收藏页要保持“收藏时间倒序”的展示顺序，不能直接按数据库 in 查询结果返回。
        List<String> userCollectionIds = list.stream().map(UserActionSyncDTO::getVideoId).collect(Collectors.toList());
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

    @RequestMapping("/collection/loadVideoInfo")
    public List<UserCollectionVO> loadCollectionVideoInfo(@RequestParam("videoIds") List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<VideoInfo> videoInfoList = videoInfoService.selectByIds(videoIds);
        if (videoInfoList == null || videoInfoList.isEmpty()) {
            return Collections.emptyList();
        }
        return BeanUtil.copyToList(videoInfoList, UserCollectionVO.class);
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

        // 当前 video_info 是前台正式视频表；如果后续把审核态也落到这里，再补充状态判断。
        if (videoInfo.getInteraction() != null && videoInfo.getInteraction().contains(Constants.ONE.toString())) {
            return false;
        }
        return true;
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

}
