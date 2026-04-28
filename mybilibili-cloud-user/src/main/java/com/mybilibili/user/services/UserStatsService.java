package com.mybilibili.user.services;

import com.mybilibili.base.entity.query.UserStatsQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.user.entity.po.UserStats;
import com.mybilibili.base.entity.vo.AdminIndexStatisticsVO;
import com.mybilibili.base.entity.vo.AdminWeekCountVO;
import com.mybilibili.base.enums.AdminStatsTypeEnum;

import java.util.List;

public interface UserStatsService {

    List<UserStats> findListByParam(UserStatsQuery param);
    Integer findCountByParam(UserStatsQuery param);
    PaginationResultVO<UserStats> findListByPage(UserStatsQuery param);
    Integer add(UserStats bean);
    Integer addBatch(List<UserStats> listBean);
    Integer addOrUpdateBatch(List<UserStats> listBean);
    UserStats getUserStatsByUserId(String userId);
    Integer updateUserStatsByUserId(UserStats bean, String userId);
    Integer deleteUserStatsByUserId(String userId);
    AdminIndexStatisticsVO getAdminActualTimeStatisticsInfo();
    List<AdminWeekCountVO> getAdminWeekStatisticsInfo(AdminStatsTypeEnum statsType);
}
