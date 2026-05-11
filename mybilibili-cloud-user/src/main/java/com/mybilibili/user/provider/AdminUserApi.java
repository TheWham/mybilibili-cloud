package com.mybilibili.user.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.vo.AdminIndexStatisticsVO;
import com.mybilibili.base.entity.vo.AdminUserInfoVO;
import com.mybilibili.base.entity.vo.AdminWeekCountVO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.AdminStatsTypeEnum;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.services.UserInfoService;
import com.mybilibili.user.services.UserStatsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class AdminUserApi {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserStatsService userStatsService;

    /**
     * 后台用户管理列表。
     *
     * <p>用户数据归 user 服务维护，admin 只通过这个内部接口拿展示 VO，
     * 不跨模块依赖 user_info 表或 Mapper。</p>
     */
    @RequestMapping("/admin/user/loadUser")
    public PaginationResultVO<AdminUserInfoVO> loadUser(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                       @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                       @RequestParam(value = "nickNameFuzzy", required = false) String nickNameFuzzy,
                                                       @RequestParam(value = "status", required = false) Integer status) {
        UserInfoQuery query = new UserInfoQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setNickNameFuzzy(nickNameFuzzy);
        query.setStatus(status);
        query.setOrderBy("join_time desc");

        PaginationResultVO<UserInfo> page = userInfoService.findListByPage(query);
        return copyPage(page, AdminUserInfoVO.class);
    }

    @RequestMapping("/admin/user/changeStatus")
    public void changeStatus(@RequestParam("userId") String userId, @RequestParam("status") Integer status) {
        userInfoService.changeStatus(userId, status);
    }

    @RequestMapping("/admin/index/getActualTimeStatisticsInfo")
    public AdminIndexStatisticsVO getActualTimeStatisticsInfo() {
        return userStatsService.getAdminActualTimeStatisticsInfo();
    }

    @RequestMapping("/admin/index/getWeekStatisticsInfo")
    public List<AdminWeekCountVO> getWeekStatisticsInfo(@RequestParam("dataType") Integer dataType) {
        return userStatsService.getAdminWeekStatisticsInfo(AdminStatsTypeEnum.getEnum(dataType));
    }

    private <S, T> PaginationResultVO<T> copyPage(PaginationResultVO<S> sourcePage, Class<T> targetClass) {
        PaginationResultVO<T> targetPage = new PaginationResultVO<>();
        targetPage.setTotalCount(sourcePage.getTotalCount());
        targetPage.setPageSize(sourcePage.getPageSize());
        targetPage.setPageNo(sourcePage.getPageNo());
        targetPage.setPageTotal(sourcePage.getPageTotal());
        targetPage.setList(BeanUtil.copyToList(sourcePage.getList(), targetClass));
        return targetPage;
    }
}
