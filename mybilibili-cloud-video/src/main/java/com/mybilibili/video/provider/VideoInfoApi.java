package com.mybilibili.video.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserVideoSeriesDTO;
import com.mybilibili.base.entity.vo.*;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.video.entity.dto.SeriesWithVideoQueryDTO;
import com.mybilibili.video.entity.dto.UserVideoSeriesVideoQueryDTO;
import com.mybilibili.video.entity.enums.VideoOrderTypeEnum;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.services.UserVideoSeriesService;
import com.mybilibili.video.services.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
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
    private UserVideoSeriesService userVideoSeriesService;

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
    public List<UserCollectionVO> loadUserCollection(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                     @RequestParam("userId") String userId
    )
    {

    }

    @GetMapping("/collection/loadVideoInfo")
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
