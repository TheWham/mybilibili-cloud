package com.mybilibili.user.controller;

import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.user.consumer.InteractClient;
import com.mybilibili.user.consumer.VideoInfoClient;
import com.mybilibili.user.entity.vo.UCenterVideoDateVO;
import com.mybilibili.user.entity.vo.UCenterVideoWeekCountVO;
import com.mybilibili.user.services.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ucenter")
@LoginInterceptor(checkLogin = true)
public class UCenterController extends ABaseController {


    @Resource
    private VideoInfoClient videoInfoClient;

    @Resource
    private InteractClient interactClient;

    @Resource
    private UserInfoService userInfoService;


    @RequestMapping("loadVideoList")
    public ResponseVO loadVideoList(Integer pageNo, String videoNameFuzzy, Integer status) {
        String userId = getTokenUserInfo().getUserId();
        return getSuccessResponseVO(videoInfoClient.loadUCenterVideoList(pageNo, videoNameFuzzy, status, userId));
    }


    /**
     * 获取视频所处各自状态的数量
     * @return
     */

    @RequestMapping("getVideoCountInfo")
    public ResponseVO getVideoCountInfo() {
        return getSuccessResponseVO(videoInfoClient.getVideoCountInfo(getTokenUserInfo().getUserId()));
    }

    @RequestMapping("getVideoByVideoId")
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        String userId = getTokenUserInfo().getUserId();
        return getSuccessResponseVO(videoInfoClient.getVideoByVideoId(videoId, userId));
    }

    @RequestMapping("/postVideo")
    public ResponseVO postVideo(@Validated VideoInfoPostDTO videoInfoPostDTO) {
        videoInfoPostDTO.setUserId(getTokenUserInfo().getUserId());
        videoInfoClient.postVideo(videoInfoPostDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        TokenUserInfoDTO tokenUserInfo = getTokenUserInfo();
        videoInfoClient.deleteVideo(videoId, tokenUserInfo.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/saveVideoInteraction")
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        videoInfoClient.saveVideoInteraction(videoId, getTokenUserInfo().getUserId(), interaction);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadAllVideo")
    public ResponseVO loadAllVideo() {
        return getSuccessResponseVO(videoInfoClient.loadAllVideo(getTokenUserInfo().getUserId()));
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoId) {
        return getSuccessResponseVO(interactClient.loadComment(pageNo, pageSize, videoId, getTokenUserInfo().getUserId()));
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoId) {
        return getSuccessResponseVO(interactClient.loadDanmu(pageNo, pageSize, videoId, getTokenUserInfo().getUserId()));
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId) {
        interactClient.delComment(commentId, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        interactClient.delDanmu(danmuId, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo()
    {
        UCenterVideoDateVO actualTimeStatisticsInfo = userInfoService.getActualTimeStatisticsInfo(getTokenUserInfo().getUserId());
        return getSuccessResponseVO(actualTimeStatisticsInfo);
    }

    @RequestMapping("/getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo(@NotNull Integer dataType)
    {
        List<UCenterVideoWeekCountVO> uCenterVideoDateVOList = userInfoService.getWeekStatisticsInfo(UserStatsRedisEnum.getEnum(dataType), getTokenUserInfo().getUserId());
        return getSuccessResponseVO(uCenterVideoDateVOList);
    }

}


