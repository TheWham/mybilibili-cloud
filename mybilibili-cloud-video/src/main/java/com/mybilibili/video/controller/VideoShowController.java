package com.mybilibili.video.controller;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.entity.vo.VideoSearchResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.SearchOrderTypeEnum;
import com.mybilibili.base.enums.VideoRecommendEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.video.consumer.SearchVideoClient;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.po.VideoInfoFile;
import com.mybilibili.video.entity.query.VideoInfoFileQuery;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.entity.vo.VideoInfoResultVO;
import com.mybilibili.video.services.VideoInfoFileService;
import com.mybilibili.video.services.VideoInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/video")
public class VideoShowController extends ABaseController {

    @Resource
    private VideoInfoService videoInfoService;

    @Resource
    private VideoInfoFileService videoInfoFileService;
    @Resource
    private SearchVideoClient searchVideoClient;

    @RequestMapping("/loadVideo")
    public ResponseVO loadVideo(Integer pageNo, Integer pCategoryId, Integer categoryId)
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setPCategoryId(pCategoryId);
        videoInfoQuery.setCategoryId(categoryId);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setRecommendType(VideoRecommendEnum.NO_RECOMMEND.getStatus());
        PaginationResultVO<VideoInfo> listByPage = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(listByPage);
    }

    @RequestMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo()
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setOrderBy("create_time desc");
        videoInfoQuery.setRecommendType(VideoRecommendEnum.RECOMMEND.getStatus());
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setQueryCountInfo(true);
        List<VideoInfo> recommendVideoList = videoInfoService.findListByParam(videoInfoQuery);
        return getSuccessResponseVO(recommendVideoList);
    }

    @RequestMapping("/getVideoInfo")
    public ResponseVO getVideoInfo(@NotEmpty String videoId){

        VideoInfoResultVO videoInfoResultVO = videoInfoService.getVideoInfoResultVO(videoId, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(videoInfoResultVO);
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId)
    {
        VideoInfo videoInfo = videoInfoService.getVideoInfoByVideoId(videoId);
        if (videoInfo == null)
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        VideoInfoFileQuery fileQuery = new VideoInfoFileQuery();
        fileQuery.setVideoId(videoId);
        fileQuery.setOrderBy("file_index asc");
        List<VideoInfoFile> pList = videoInfoFileService.findListByParam(fileQuery);
        return getSuccessResponseVO(pList);
    }

    @RequestMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId, @NotEmpty String deviceId)
    {
        Integer count = videoInfoService.reportVideoPlayOnline(fileId, deviceId);
        return getSuccessResponseVO(count);
    }


    @RequestMapping("/getVideoRecommend")
    public ResponseVO getVideoRecommend(@NotEmpty String keyword, @NotEmpty String videoId)
    {
        List<VideoSearchResultVO> search = searchVideoClient.searchVideo(false, keyword,
                SearchOrderTypeEnum.VIDEO_PLAY.getStatus(), 1, PageSize.SIZE30.getSize()).getList();
        List<VideoSearchResultVO> list = search.stream().filter(video -> !video.getVideoId().equals(videoId)).collect(Collectors.toList());
        return getSuccessResponseVO(list);
    }

    @RequestMapping("/loadHotVideoList")
    public ResponseVO loadHotVideoList(Integer pageNo)
    {
        VideoInfoQuery videoInfoQuery = new VideoInfoQuery();
        videoInfoQuery.setPageNo(pageNo);
        videoInfoQuery.setQueryUserInfo(true);
        videoInfoQuery.setOrderBy("play_count desc");
        videoInfoQuery.setLastPlayHour(Constants.HOUR_24);
        PaginationResultVO<VideoInfo> list = videoInfoService.findListByPage(videoInfoQuery);
        return getSuccessResponseVO(list);
    }

}

