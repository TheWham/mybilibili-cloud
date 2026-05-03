package com.mybilibili.media.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 媒体服务对外入口。
 */
@RestController
@RequestMapping("/media")
public class MediaApiController extends ABaseController {

    @RequestMapping("/ping")
    public ResponseVO ping() {
        return getSuccessResponseVO(Map.of("service", "mybilibili-cloud-media"));
    }
}
