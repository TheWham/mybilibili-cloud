package com.mybilibili.search.controller;

import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 搜索服务对外入口。
 */
@RestController
@RequestMapping("/search")
public class SearchApiController extends ABaseController {

    @RequestMapping("/ping")
    public ResponseVO ping() {
        return getSuccessResponseVO(Map.of("service", "mybilibili-cloud-search"));
    }
}
