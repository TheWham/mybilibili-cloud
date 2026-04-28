package com.mybilibili.user.services.impl;

import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.query.UserStatsQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.user.entity.po.UserStats;
import com.mybilibili.base.entity.vo.AdminIndexStatisticsVO;
import com.mybilibili.base.entity.vo.AdminTotalCountInfoVO;
import com.mybilibili.base.entity.vo.AdminWeekCountVO;
import com.mybilibili.base.enums.AdminStatsTypeEnum;
import com.mybilibili.user.mappers.UserInfoMapper;
import com.mybilibili.user.mappers.UserStatsMapper;
import com.mybilibili.user.services.UserStatsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service("UserStatsService")
public class UserStatsServiceImpl implements UserStatsService {
    @Resource
    private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Override
    public List<UserStats> findListByParam(UserStatsQuery param) {
        return this.userStatsMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(UserStatsQuery param) {
        return this.userStatsMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<UserStats> findListByPage(UserStatsQuery param) {
        Integer count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserStats> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(UserStats bean) {
        return this.userStatsMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<UserStats> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userStatsMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<UserStats> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userStatsMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public UserStats getUserStatsByUserId(String userId) {
        return this.userStatsMapper.selectByUserId(userId);
    }

    @Override
    public Integer updateUserStatsByUserId(UserStats bean, String userId) {
        return this.userStatsMapper.updateByUserId(bean, userId);
    }

    @Override
    public Integer deleteUserStatsByUserId(String userId) {
        return this.userStatsMapper.deleteByUserId(userId);
    }

    @Override
    public AdminIndexStatisticsVO getAdminActualTimeStatisticsInfo() {
        AdminIndexStatisticsVO statisticsInfo = new AdminIndexStatisticsVO();
        statisticsInfo.setTotalCountInfo(new AdminTotalCountInfoVO(0, 0, 0, 0, 0, 0, 0));
        statisticsInfo.setPreDayData(initAdminStatsArray());

        java.util.Date latestStatsDay = userStatsMapper.selectLatestStatsDay();
        if (latestStatsDay == null) {
            return statisticsInfo;
        }

        UserStats latestSummary = userStatsMapper.selectSummaryByStatsDay(latestStatsDay);
        if (latestSummary != null) {
            statisticsInfo.setTotalCountInfo(buildAdminTotalCountInfo(latestSummary));
        }
        statisticsInfo.getTotalCountInfo().setUserCount(Optional.ofNullable(userInfoMapper.selectTotalUserCount()).orElse(0));

        LocalDate latestLocalDate = toLocalDate(latestStatsDay);
        UserStats preDaySummary = userStatsMapper.selectSummaryByStatsDay(Date.valueOf(latestLocalDate.minusDays(1)));
        statisticsInfo.setPreDayData(buildAdminPreDayData(preDaySummary));
        statisticsInfo.getPreDayData()[AdminStatsTypeEnum.USER_COUNT.getType()] =
                Optional.ofNullable(userInfoMapper.selectRegisterCountByDate(Date.valueOf(latestLocalDate.minusDays(1)))).orElse(0);
        return statisticsInfo;
    }

    @Override
    public List<AdminWeekCountVO> getAdminWeekStatisticsInfo(AdminStatsTypeEnum statsType) {
        if (statsType == null) {
            throw new BusinessException("统计类型不存在");
        }

        java.util.Date latestStatsDay = userStatsMapper.selectLatestStatsDay();
        LocalDate endDate = latestStatsDay == null
                ? LocalDate.now()
                : toLocalDate(latestStatsDay);
        LocalDate startDate = endDate.minusDays(6);

        Map<LocalDate, Integer> summaryMap = new HashMap<>();
        if (AdminStatsTypeEnum.USER_COUNT.equals(statsType)) {
            List<Map<String, Object>> registerCountList = userInfoMapper.selectDailyRegisterCountByDateRange(
                    Date.valueOf(startDate), Date.valueOf(endDate));
            for (Map<String, Object> row : registerCountList) {
                LocalDate statsDay = toLocalDate((java.util.Date) row.get("statsDay"));
                Integer userCount = row.get("userCount") == null ? 0 : Integer.parseInt(row.get("userCount").toString());
                summaryMap.put(statsDay, userCount);
            }
        } else {
            List<UserStats> summaryList = userStatsMapper.selectSummaryListByDateRange(
                    Date.valueOf(startDate.minusDays(1)), Date.valueOf(endDate));
            for (UserStats userStats : summaryList) {
                if (userStats.getStatsDay() == null) {
                    continue;
                }
                LocalDate statsDay = toLocalDate(userStats.getStatsDay());
                summaryMap.put(statsDay, getAdminStatsCountByType(userStats, statsType));
            }
        }

        List<AdminWeekCountVO> result = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            AdminWeekCountVO weekCountVO = new AdminWeekCountVO();
            weekCountVO.setStatisticsDate(Date.valueOf(currentDate));
            if (AdminStatsTypeEnum.USER_COUNT.equals(statsType)) {
                weekCountVO.setStatisticsCount(summaryMap.getOrDefault(currentDate, 0));
            } else {
                int currentTotal = summaryMap.getOrDefault(currentDate, 0);
                int previousTotal = summaryMap.getOrDefault(currentDate.minusDays(1), 0);
                weekCountVO.setStatisticsCount(Math.max(currentTotal - previousTotal, 0));
            }
            result.add(weekCountVO);
        }
        return result;
    }

    private AdminTotalCountInfoVO buildAdminTotalCountInfo(UserStats userStats) {
        AdminTotalCountInfoVO totalCountInfoVO = new AdminTotalCountInfoVO();
        totalCountInfoVO.setUserCount(Optional.ofNullable(userStats.getUserCount()).orElse(0));
        totalCountInfoVO.setPlayCount(Optional.ofNullable(userStats.getPlayCount()).orElse(0));
        totalCountInfoVO.setCommentCount(Optional.ofNullable(userStats.getCommentCount()).orElse(0));
        totalCountInfoVO.setDanmuCount(Optional.ofNullable(userStats.getDanmuCount()).orElse(0));
        totalCountInfoVO.setLikeCount(Optional.ofNullable(userStats.getLikeCount()).orElse(0));
        totalCountInfoVO.setCoinCount(Optional.ofNullable(userStats.getCoinCount()).orElse(0));
        totalCountInfoVO.setCollectCount(Optional.ofNullable(userStats.getCollectCount()).orElse(0));
        return totalCountInfoVO;
    }

    private Integer[] buildAdminPreDayData(UserStats userStats) {
        Integer[] preDayData = initAdminStatsArray();
        if (userStats == null) {
            return preDayData;
        }
        for (AdminStatsTypeEnum statsEnum : AdminStatsTypeEnum.values()) {
            preDayData[statsEnum.getType()] = getAdminStatsCountByType(userStats, statsEnum);
        }
        return preDayData;
    }

    private Integer[] initAdminStatsArray() {
        Integer[] preDayData = new Integer[AdminStatsTypeEnum.values().length];
        for (int i = 0; i < preDayData.length; i++) {
            preDayData[i] = 0;
        }
        return preDayData;
    }

    private Integer getAdminStatsCountByType(UserStats userStats, AdminStatsTypeEnum statsType) {
        if (userStats == null || statsType == null) {
            return 0;
        }
        return switch (statsType) {
            case LIKE_COUNT -> Optional.ofNullable(userStats.getLikeCount()).orElse(0);
            case PLAY_COUNT -> Optional.ofNullable(userStats.getPlayCount()).orElse(0);
            case DANMU_COUNT -> Optional.ofNullable(userStats.getDanmuCount()).orElse(0);
            case COIN_COUNT -> Optional.ofNullable(userStats.getCoinCount()).orElse(0);
            case USER_COUNT -> Optional.ofNullable(userStats.getUserCount()).orElse(0);
            case COMMENT_COUNT -> Optional.ofNullable(userStats.getCommentCount()).orElse(0);
            case COLLECT_COUNT -> Optional.ofNullable(userStats.getCollectCount()).orElse(0);
        };
    }

    private LocalDate toLocalDate(java.util.Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
