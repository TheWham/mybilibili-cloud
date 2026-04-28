package com.mybilibili.user.component;

import com.mybilibili.base.entity.query.UserFocusQuery;
import com.mybilibili.base.entity.query.UserInfoQuery;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.common.component.RedisComponent;
import com.mybilibili.user.entity.po.UserFocus;
import com.mybilibili.user.entity.po.UserInfo;
import com.mybilibili.user.entity.vo.UserCountVO;
import com.mybilibili.user.mappers.UserFocusMapper;
import com.mybilibili.user.mappers.UserInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class UserStatsCacheAsyncComponent {

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private UserFocusMapper<UserFocus, UserFocusQuery> userFocusMapper;
    //TODO 迁移至interact模块: UserVideoActionMapper
    //TODO 接入video模块
    // @Resource
    // private VideoCommentMapper<VideoComment, VideoCommentQuery> videoCommentMapper;
    // @Resource
    // private VideoDanmuMapper<VideoDanmu, VideoDanmuQuery> videoDanmuMapper;
    // @Resource
    // private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;

    @Async("userStatsCacheExecutor")
    public void refreshRealtimeUserStatsCache(String userId) {
        if (userId == null) {
            return;
        }
        HashMap<String, Integer> cacheMap = redisComponent.getRealtimeUserStatsInfo(userId);
        if (cacheMap != null && !cacheMap.isEmpty()) {
            redisComponent.refreshRealtimeUserStatsExpire(userId);
            return;
        }
        HashMap<String, Integer> statsMap = buildRealtimeUserStatsSnapshot(userId);
        if (statsMap == null || statsMap.isEmpty()) {
            return;
        }
        redisComponent.saveRealtimeUserStatsInfo(userId, statsMap);
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
        //TODO 接入Video模块: 需要 VideoCommentMapper, VideoDanmuMapper
        statsMap.put(UserStatsRedisEnum.USER_COMMENT_COUNT.getField(), 0);
        statsMap.put(UserStatsRedisEnum.VIDEO_DANMU.getField(), 0);
        //TODO 迁移至interact模块: 需要 UserVideoActionMapper.sumCoinCount / selectCount
        statsMap.put(UserStatsRedisEnum.VIDEO_COIN.getField(), 0);
        statsMap.put(UserStatsRedisEnum.USER_COLLECT_COUNT.getField(), 0);
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
        //TODO 需要feign调用video服务
        userCountVO.setLikeCount(0);
        userCountVO.setPlayCount(0);
        return userCountVO;
    }

    //TODO 迁移至interact模块: countVideoCollect需要UserVideoActionMapper
    // private Integer countVideoCollect(String userId) {
    //     UserActionQuery userActionQuery = new UserActionQuery();
    //     userActionQuery.setVideoUserId(userId);
    //     userActionQuery.setActionType(UserActionTypeEnum.VIDEO_COLLECT.getType());
    //     return userVideoActionMapper.selectCount(userActionQuery);
    // }

    private Integer defaultValue(Integer value) {
        return value == null ? 0 : value;
    }
}
