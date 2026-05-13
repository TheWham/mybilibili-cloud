package com.mybilibili.interact.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.annotation.LoginInterceptor;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.interact.entity.dto.VideoDanmuDTO;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 播放页弹幕接口。
 */
@RestController
@RequestMapping("danmu")
public class VideoDanmuController extends ABaseController {

    @Resource
    private VideoDanmuService videoDanmuService;

    @RequestMapping("/postDanmu")
    @LoginInterceptor(checkLogin = true)
    public ResponseVO postDanmu(@Validated VideoDanmuDTO videoDanmuDTO) {
        VideoDanmu videoDanmu = BeanUtil.toBean(videoDanmuDTO, VideoDanmu.class);
        videoDanmu.setUserId(getTokenUserInfo().getUserId());
        videoDanmuService.postDanmu(videoDanmu);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(@NotEmpty String fileId, @NotEmpty String videoId) {
        List<VideoDanmu> danmuList = videoDanmuService.loadDanmu(fileId, videoId);
        return getSuccessResponseVO(danmuList);
    }
}
