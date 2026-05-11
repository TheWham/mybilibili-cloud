package com.mybilibili.user.component;


import com.mybilibili.base.entity.dto.UserInteractCountDTO;
import com.mybilibili.base.entity.dto.VideoCountDTO;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.entity.vo.UserCountVO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.user.consumer.InteractClient;
import com.mybilibili.user.consumer.VideoInfoClient;
import com.mybilibili.user.entity.po.UserFocus;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.entity.query.UserFocusQuery;
import com.mybilibili.user.mappers.UserFocusMapper;
import com.mybilibili.user.mappers.UserInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class UserStatsCacheAsyncComponent {

    @Resource
    private UserRedisComponent userRedisComponent;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
    @Resource
    private VideoInfoClient videoInfoClient;
    @Resource
    private InteractClient interactClient;

    @Async("userStatsCacheExecutor")
    public void refreshRealtimeUserStatsCache(String userId) {
        if (userId == null) {
            return;
        }
        HashMap<String, Integer> cacheMap = userRedisComponent.getRealtimeUserStatsInfo(userId);
        if (cacheMap != null && !cacheMap.isEmpty()) {
            userRedisComponent.refreshRealtimeUserStatsExpire(userId);
            return;
        }
        HashMap<String, Integer> statsMap = buildRealtimeUserStatsSnapshot(userId);
        if (statsMap == null || statsMap.isEmpty()) {
            return;
        }
        userRedisComponent.saveRealtimeUserStatsInfo(userId, statsMap);
    }

    public HashMap<String, Integer> buildRealtimeUserStatsSnapshot(String userId) {
        if (userId == null) {
            return null;
        }
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        if (userInfo == null) {
            return null;
        }
        return buildRealtimeUserStatsMap(userId, buildUserCountVO(userId, userInfo));
    }

    private HashMap<String, Integer> buildRealtimeUserStatsMap(String userId, UserCountVO userCountVO) {
        HashMap<String, Integer> statsMap = new HashMap<>();
        statsMap.put(UserStatsRedisEnum.USER_FOCUS.getField(), defaultValue(userCountVO.getFocusCount()));
        statsMap.put(UserStatsRedisEnum.USER_FANS.getField(), defaultValue(userCountVO.getFansCount()));
        statsMap.put(UserStatsRedisEnum.USER_COIN.getField(), defaultValue(userCountVO.getCurrentCoinCount()));
        statsMap.put(UserStatsRedisEnum.VIDEO_LIKE.getField(), defaultValue(userCountVO.getLikeCount()));
        statsMap.put(UserStatsRedisEnum.VIDEO_PLAY.getField(), defaultValue(userCountVO.getPlayCount()));
        fillInteractStats(userId, statsMap);
        return statsMap;
    }

    private UserCountVO buildUserCountVO(String userId, UserInfo userInfo) {
        UserCountVO userCountVO = new UserCountVO();
        userCountVO.setCurrentCoinCount(defaultValue(userInfo.getCurrentCoinCount()));

        UserFocusQuery focusQuery = new UserFocusQuery();
        focusQuery.setUserId(userId);
        userCountVO.setFocusCount(defaultValue(userFocusMapper.selectCount(focusQuery)));

        UserFocusQuery fansQuery = new UserFocusQuery();
        fansQuery.setUserFocusId(userId);
        userCountVO.setFansCount(defaultValue(userFocusMapper.selectCount(fansQuery)));

        VideoCountDTO videoCountDTO = loadVideoCount(userId);
        if (videoCountDTO == null) {
            userCountVO.setLikeCount(0);
            userCountVO.setPlayCount(0);
            return userCountVO;
        }
        userCountVO.setLikeCount(defaultValue(videoCountDTO.getTotalLikeCount()));
        userCountVO.setPlayCount(defaultValue(videoCountDTO.getTotalPlayCount()));
        return userCountVO;
    }

    private void fillInteractStats(String userId, HashMap<String, Integer> statsMap) {
        UserInteractCountDTO interactCountDTO = loadInteractCount(userId);
        if (interactCountDTO == null) {
            statsMap.put(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), 0);
            statsMap.put(UserStatsRedisEnum.VIDEO_DANMU.getField(), 0);
            statsMap.put(UserStatsRedisEnum.VIDEO_COIN.getField(), 0);
            statsMap.put(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), 0);
            return;
        }

        // interact 只返回用户作为 UP 主收到的互动数，这里统一写入 user 实时统计缓存。
        statsMap.put(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), defaultValue(interactCountDTO.getCommentCount()));
        statsMap.put(UserStatsRedisEnum.VIDEO_DANMU.getField(), defaultValue(interactCountDTO.getDanmuCount()));
        statsMap.put(UserStatsRedisEnum.VIDEO_COIN.getField(), defaultValue(interactCountDTO.getCoinCount()));
        statsMap.put(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), defaultValue(interactCountDTO.getCollectCount()));
    }

    private VideoCountDTO loadVideoCount(String userId) {
        try {
            // 视频统计归 video 服务维护，user 冷启动缓存时只通过 Feign 拉取汇总结果。
            return videoInfoClient.countVideoInfoByUserId(userId);
        } catch (Exception e) {
            // 统计缓存是展示型数据，远程服务短暂异常时降级为 0，避免影响用户主页主流程。
            log.warn("查询用户视频统计失败，userId:{}", userId, e);
            return null;
        }
    }

    private UserInteractCountDTO loadInteractCount(String userId) {
        try {
            return interactClient.countUserInteractByUserId(userId);
        } catch (Exception e) {
            // 评论、弹幕等统计也按展示数据处理，失败后下一次缓存刷新会重新拉取。
            log.warn("查询用户互动统计失败，userId:{}", userId, e);
            return null;
        }
    }

    private Integer defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}
