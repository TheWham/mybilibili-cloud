package com.mybilibili.admin.controller;

import com.mybilibili.admin.consumer.AdminVideoClient;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/videoInfo")
public class VideoInfoController extends ABaseController {

    @Resource
    private AdminVideoClient adminVideoClient;

    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(Integer pageNo, Integer pageSize, String videoNameFuzzy,
                                    Integer recommendType, Integer categoryId, Integer pCategoryId) {
        return getSuccessResponseVO(adminVideoClient.loadVideoList(
                pageNo, pageSize, videoNameFuzzy, recommendType, categoryId, pCategoryId));
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        return getSuccessResponseVO(adminVideoClient.loadVideoPList(videoId));
    }

    @RequestMapping("/auditVideo")
    public ResponseVO auditVideo(@NotEmpty String videoId, Integer status, String reason) {
        adminVideoClient.auditVideo(videoId, status, reason);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/recommendVideo")
    public ResponseVO recommendVideo(@NotEmpty String videoId) {
        adminVideoClient.recommendVideo(videoId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        adminVideoClient.deleteVideo(videoId);
        return getSuccessResponseVO(null);
    }
}
