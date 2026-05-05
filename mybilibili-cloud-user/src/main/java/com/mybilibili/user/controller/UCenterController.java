package com.mybilibili.user.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.user.consumer.VideoInfoClient;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@LoginInterceptor(checkLogin = true)
@RequestMapping("/uhome")
public class UCenterController extends ABaseController {
    @Resource
    private VideoInfoClient videoInfoClient;

    @RequestMapping("/loadVideoList")
    public ResponseVO loadUHomeVideoList(@RequestParam("userId") String userId,
                                         @RequestParam(value = "type", required = false) Integer type,
                                         @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                         @RequestParam(value = "videoName", required = false) String videoName,
                                         @RequestParam(value = "orderType", required = false) Integer orderType)
    {
        return getSuccessResponseVO(videoInfoClient.loadVideoList(userId, type, pageNo, videoName, orderType));
    }

    @RequestMapping("/series/loadVideoSeriesWithVideo")
    public ResponseVO loadVideoSeriesWithVideo(@NotEmpty String userId)
    {
        return getSuccessResponseVO(videoInfoClient.loadVideoSeriesWithVideo(userId));
    }
}
