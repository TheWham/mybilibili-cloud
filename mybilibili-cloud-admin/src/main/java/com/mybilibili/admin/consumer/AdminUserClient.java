package com.mybilibili.admin.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.AdminIndexStatisticsVO;
import com.mybilibili.base.entity.vo.AdminUserInfoVO;
import com.mybilibili.base.entity.vo.AdminWeekCountVO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "adminUserClient", name = Constants.CLOUD_USER_NAME)
public interface AdminUserClient {

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/user/loadUser")
    PaginationResultVO<AdminUserInfoVO> loadUser(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                 @RequestParam(value = "nickNameFuzzy", required = false) String nickNameFuzzy,
                                                 @RequestParam(value = "status", required = false) Integer status);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/user/changeStatus")
    void changeStatus(@RequestParam("userId") String userId, @RequestParam("status") Integer status);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/index/getActualTimeStatisticsInfo")
    AdminIndexStatisticsVO getActualTimeStatisticsInfo();

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/index/getWeekStatisticsInfo")
    List<AdminWeekCountVO> getWeekStatisticsInfo(@RequestParam("dataType") Integer dataType);
}
