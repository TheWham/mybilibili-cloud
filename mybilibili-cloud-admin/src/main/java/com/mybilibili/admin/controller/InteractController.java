package com.mybilibili.admin.controller;

import com.mybilibili.admin.consumer.AdminInteractClient;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/interact")
public class InteractController extends ABaseController {

    @Resource
    private AdminInteractClient adminInteractClient;

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoNameFuzzy) {
        return getSuccessResponseVO(adminInteractClient.loadComment(pageNo, pageSize, videoNameFuzzy));
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoNameFuzzy) {
        return getSuccessResponseVO(adminInteractClient.loadDanmu(pageNo, pageSize, videoNameFuzzy));
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotNull Integer commentId) {
        adminInteractClient.delComment(commentId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotNull Integer danmuId) {
        adminInteractClient.delDanmu(danmuId);
        return getSuccessResponseVO(null);
    }
}
