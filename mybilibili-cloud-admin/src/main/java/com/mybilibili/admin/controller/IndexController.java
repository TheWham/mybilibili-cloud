package com.mybilibili.admin.controller;

import com.mybilibili.admin.consumer.AdminUserClient;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/index")
public class IndexController extends ABaseController {

    @Resource
    private AdminUserClient adminUserClient;

    @RequestMapping("/getActualTimeStatisticsInfo")
    public ResponseVO getActualTimeStatisticsInfo() {
        return getSuccessResponseVO(adminUserClient.getActualTimeStatisticsInfo());
    }

    @RequestMapping("/getWeekStatisticsInfo")
    public ResponseVO getWeekStatisticsInfo(@NotNull Integer dataType) {
        return getSuccessResponseVO(adminUserClient.getWeekStatisticsInfo(dataType));
    }
}
