package com.mybilibili.user.controller;

import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.user.entity.vo.UCenterVideoDateVO;
import com.mybilibili.user.entity.vo.UCenterVideoWeekCountVO;
import com.mybilibili.user.services.UCenterService;
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
    private UCenterService uCenterService;

    @Resource
    private UserInfoService userInfoService;


    @RequestMapping("loadVideoList")
    public ResponseVO loadVideoList(Integer pageNo, String videoNameFuzzy, Integer status) {
        String userId = getTokenUserInfo().getUserId();
        return getSuccessResponseVO(uCenterService.loadVideoList(pageNo, videoNameFuzzy, status, userId));
    }

    /**
     * 获取视频所处各自状态的数量
     * @return
     */

    @RequestMapping("getVideoCountInfo")
    public ResponseVO getVideoCountInfo() {
        return getSuccessResponseVO(uCenterService.getVideoCountInfo(getTokenUserInfo().getUserId()));
    }

    @RequestMapping("getVideoByVideoId")
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        String userId = getTokenUserInfo().getUserId();
        return getSuccessResponseVO(uCenterService.getVideoByVideoId(videoId, userId));
    }

    @RequestMapping("/postVideo")
    public ResponseVO postVideo(@Validated VideoInfoPostDTO videoInfoPostDTO) {
        uCenterService.postVideo(videoInfoPostDTO, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        uCenterService.deleteVideo(videoId, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/saveVideoInteraction")
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        uCenterService.saveVideoInteraction(videoId, interaction, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadAllVideo")
    public ResponseVO loadAllVideo() {
        return getSuccessResponseVO(uCenterService.loadAllVideo(getTokenUserInfo().getUserId()));
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoId) {
        return getSuccessResponseVO(uCenterService.loadComment(pageNo, pageSize, videoId, getTokenUserInfo().getUserId()));
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoId) {
        return getSuccessResponseVO(uCenterService.loadDanmu(pageNo, pageSize, videoId, getTokenUserInfo().getUserId()));
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId) {
        uCenterService.delComment(commentId, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        uCenterService.delDanmu(danmuId, getTokenUserInfo().getUserId());
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

