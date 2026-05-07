package com.mybilibili.video.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.video.services.VideoPlayHistoryService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史Service
 */

@RestController
@RequestMapping("history")
@LoginInterceptor(checkLogin = true)
public class VideoPlayHistoryController extends ABaseController {
    @Resource
    private VideoPlayHistoryService videoPlayHistoryService;

    /**
     * 根据条件分页查询
     */
    @RequestMapping("loadHistory")
    public ResponseVO loadDataList (Integer pageNo) {
        return getSuccessResponseVO(videoPlayHistoryService.loadHistoryByPage(getTokenUserInfo().getUserId(), pageNo));
    }

    @RequestMapping("delHistory")
    public ResponseVO delHistory(@NotEmpty String videoId)
    {
        videoPlayHistoryService.delHistory(videoId, getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("cleanHistory")
    public ResponseVO cleanHistory()
    {
        videoPlayHistoryService.cleanHistory(getTokenUserInfo().getUserId());
        return getSuccessResponseVO(null);
    }

}