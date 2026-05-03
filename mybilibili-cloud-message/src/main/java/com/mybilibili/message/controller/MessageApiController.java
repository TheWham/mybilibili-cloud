package com.mybilibili.message.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 消息服务对外入口。
 */
@RestController
@RequestMapping("/message")
public class MessageApiController extends ABaseController {

    @RequestMapping("/ping")
    public ResponseVO ping() {
        return getSuccessResponseVO(Map.of("service", "mybilibili-cloud-message"));
    }
}
