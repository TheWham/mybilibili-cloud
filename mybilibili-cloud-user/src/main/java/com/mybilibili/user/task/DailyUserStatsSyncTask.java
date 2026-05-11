package com.mybilibili.user.task;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.query.UserStatsQuery;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.user.component.UserRedisComponent;
import com.mybilibili.user.component.UserStatsCacheAsyncComponent;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.entity.po.UserStats;
import com.mybilibili.user.mappers.UserInfoMapper;
import com.mybilibili.user.mappers.UserStatsMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户每日统计刷盘任务。
 *
 * <p>实时统计仍然放在 Redis，凌晨先冻结昨日快照，再写入 user_stats。
 * admin 看板只读 user_stats，不需要自己跑统计任务。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class DailyUserStatsSyncTask {

    private static final Logger log = LoggerFactory.getLogger(DailyUserStatsSyncTask.class);
    private static final int SNAPSHOT_BATCH_SIZE = 200;

    @Resource
    private UserRedisComponent userRedisComponent;
    @Resource
    private UserStatsCacheAsyncComponent userStatsCacheAsyncComponent;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserStatsMapper<UserStats, UserStatsQuery> userStatsMapper;

    @Scheduled(cron = "0 0 0 * * *")
    public void freezeYesterdayUserStatsSnapshot() {
        String statsDay = LocalDate.now().minusDays(Constants.ONE).toString();
        int snapshotCount = freezeUserStatsSnapshot(statsDay);
        if (snapshotCount > 0) {
            log.info("freezeYesterdayUserStatsSnapshot finished, statsDay={}, count={}", statsDay, snapshotCount);
        }
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void syncYesterdayUserStats() {
        String statsDay = LocalDate.now().minusDays(Constants.ONE).toString();
        Set<String> keySet = userRedisComponent.getUserStatsSnapshotKeys(statsDay);
        if (keySet == null || keySet.isEmpty()) {
            int snapshotCount = freezeUserStatsSnapshot(statsDay);
            if (snapshotCount <= 0) {
                return;
            }
            keySet = userRedisComponent.getUserStatsSnapshotKeys(statsDay);
            if (keySet == null || keySet.isEmpty()) {
                return;
            }
        }

        List<UserStats> saveList = new ArrayList<>(keySet.size());
        for (String key : keySet) {
            String userId = key.substring(key.lastIndexOf(":") + 1);
            Map<String, Integer> statsMap = userRedisComponent.getUserStatsSnapshot(userId, statsDay);
            if (statsMap == null || statsMap.isEmpty()) {
                continue;
            }
            saveList.add(buildUserStats(userId, statsDay, statsMap));
        }
        if (saveList.isEmpty()) {
            return;
        }
        userStatsMapper.insertOrUpdateBatch(saveList);
        log.info("syncYesterdayUserStats finished, statsDay={}, count={}", statsDay, saveList.size());
    }

    private int freezeUserStatsSnapshot(String statsDay) {
        Integer totalCount = userInfoMapper.selectCount(new UserInfoQuery());
        if (totalCount == null || totalCount == 0) {
            return 0;
        }

        int snapshotCount = 0;
        int pageTotal = (totalCount + SNAPSHOT_BATCH_SIZE - 1) / SNAPSHOT_BATCH_SIZE;
        for (int pageNo = 1; pageNo <= pageTotal; pageNo++) {
            UserInfoQuery userInfoQuery = new UserInfoQuery();
            userInfoQuery.setOrderBy("user_id asc");
            userInfoQuery.setSimplePage(new SimplePage(pageNo, totalCount, SNAPSHOT_BATCH_SIZE));

            List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQuery);
            if (userInfoList == null || userInfoList.isEmpty()) {
                break;
            }
            for (UserInfo userInfo : userInfoList) {
                Map<String, Integer> statsMap = buildSnapshotMap(userInfo.getUserId());
                if (statsMap == null || statsMap.isEmpty()) {
                    continue;
                }
                userRedisComponent.saveUserStatsSnapshot(userInfo.getUserId(), statsDay, new HashMap<>(statsMap));
                snapshotCount++;
            }
        }
        return snapshotCount;
    }

    private Map<String, Integer> buildSnapshotMap(String userId) {
        Map<String, Integer> statsMap = userRedisComponent.getRealtimeUserStatsInfo(userId);
        if (statsMap != null && !statsMap.isEmpty()) {
            return statsMap;
        }

        HashMap<String, Integer> snapshotMap = userStatsCacheAsyncComponent.buildRealtimeUserStatsSnapshot(userId);
        if (snapshotMap != null && !snapshotMap.isEmpty()) {
            userRedisComponent.saveRealtimeUserStatsInfo(userId, snapshotMap);
        }
        return snapshotMap;
    }

    private UserStats buildUserStats(String userId, String statsDay, Map<String, Integer> statsMap) {
        UserStats userStats = new UserStats();
        userStats.setUserId(userId);
        userStats.setStatsDay(Date.valueOf(statsDay));
        userStats.setPlayCount(statsMap.get(UserStatsRedisEnum.VIDEO_PLAY.getField()));
        userStats.setLikeCount(statsMap.get(UserStatsRedisEnum.VIDEO_LIKE.getField()));
        userStats.setCurrentCoinCount(statsMap.get(UserStatsRedisEnum.USER_COIN.getField()));
        userStats.setFocusCount(statsMap.get(UserStatsRedisEnum.USER_FOCUS.getField()));
        userStats.setFansCount(statsMap.get(UserStatsRedisEnum.USER_FANS.getField()));
        userStats.setCollectCount(statsMap.get(UserStatsRedisEnum.USER_COLLECT_COUNT.getField()));
        userStats.setCommentCount(statsMap.get(UserStatsRedisEnum.USER_COMMENT_COUNT.getField()));
        userStats.setDanmuCount(statsMap.get(UserStatsRedisEnum.VIDEO_DANMU.getField()));
        userStats.setCoinCount(statsMap.get(UserStatsRedisEnum.VIDEO_COIN.getField()));
        return userStats;
    }
}
