package com.mybilibili.interact.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 互动服务对外入口。
 *
 * <p>当前先提供最小可用接口，用来验证 gateway -> Nacos -> interact 的路由链路。
 * 后续点赞、收藏、投币、评论、弹幕接口都放在这个模块继续展开。</p>
 */
@RestController
@RequestMapping("/interact")
public class InteractApiController extends ABaseController {

    @RequestMapping("/ping")
    public ResponseVO ping() {
        return getSuccessResponseVO(Map.of("service", "mybilibili-cloud-interact"));
    }
}
