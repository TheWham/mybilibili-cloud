package com.mybilibili.interact.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserActionSyncDTO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.interact.constants.InteractRedisKeys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * interact 服务缓存组件。
 *
 * <p>点赞、收藏、投币这类操作需要 Redis 做幂等和计数原子更新，
 * 后续同步 MySQL 的队列也放在 interact 边界内维护。</p>
 */
@Component
public class InteractRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public void saveVideoActionStatus(String userId, String videoId, Integer actionType, Integer actionCount) {
        long ttl = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_ACTION_STATUS_CACHE_TTL_MINUTES;
        redisUtils.setex(buildVideoActionStatusKey(userId, videoId, actionType), actionCount, ttl);
    }

    public boolean hasVideoActionStatus(String userId, String videoId, Integer actionType) {
        return redisUtils.keyExists(buildVideoActionStatusKey(userId, videoId, actionType));
    }

    public Integer getVideoActionStatus(String userId, String videoId, Integer actionType) {
        Object value = redisUtils.get(buildVideoActionStatusKey(userId, videoId, actionType));
        return value == null ? null : Integer.parseInt(value.toString());
    }

    public void removeVideoActionStatus(String userId, String videoId, Integer actionType) {
        redisUtils.delete(buildVideoActionStatusKey(userId, videoId, actionType));
    }

    public void saveCommentActionStatus(String userId, Integer commentId, Integer actionType) {
        redisUtils.set(buildCommentActionStatusKey(userId, commentId), actionType);
    }

    public Integer getCommentActionStatus(String userId, Integer commentId) {
        Object value = redisUtils.get(buildCommentActionStatusKey(userId, commentId));
        return value == null ? null : Integer.parseInt(value.toString());
    }

    public void removeCommentActionStatus(String userId, Integer commentId) {
        redisUtils.delete(buildCommentActionStatusKey(userId, commentId));
    }

    public Long executeVideoToggleAction(String userId, String videoUserId, String videoId, Integer actionType, Integer actionCount, String statsField) {
        String actionStatusKey = buildVideoActionStatusKey(userId, videoId, actionType);
        String ownerStatsKey = InteractRedisKeys.USER_STATS_KEY + videoUserId;
        long statsTtl = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS;
        long actionStatusTtl = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_ACTION_STATUS_CACHE_TTL_MINUTES;

        return redisUtils.executeLongScript(
                InteractRedisKeys.LUA_VIDEO_TOGGLE_ACTION,
                Arrays.asList(actionStatusKey, ownerStatsKey),
                statsField,
                actionCount,
                statsTtl,
                actionStatusTtl
        );
    }

    public Long executeVideoCoinAction(String userId, String videoUserId, String videoId, Integer actionType, Integer actionCount) {
        String actionStatusKey = buildVideoActionStatusKey(userId, videoId, actionType);
        String senderStatsKey = InteractRedisKeys.USER_STATS_KEY + userId;
        String receiverStatsKey = InteractRedisKeys.USER_STATS_KEY + videoUserId;
        long statsTtl = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS;

        return redisUtils.executeLongScript(
                InteractRedisKeys.LUA_VIDEO_COIN,
                Arrays.asList(actionStatusKey, senderStatsKey, receiverStatsKey),
                UserStatsRedisEnum.USER_COIN.getField(),
                UserStatsRedisEnum.VIDEO_COIN.getField(),
                actionCount,
                statsTtl
        );
    }

    public void addUserActionQueue(String queueKey, UserActionSyncDTO actionSyncDTO) {
        redisUtils.lpush(queueKey, actionSyncDTO, 0L);
    }

    public UserActionSyncDTO getNextUserActionQueue(String queueKey) {
        return (UserActionSyncDTO) redisUtils.rpop(queueKey);
    }

    public void addVideoAuditReward(String userId, String videoId, Integer rewardCoinCount) {
        if (userId == null || rewardCoinCount == null || rewardCoinCount <= 0) {
            return;
        }
        incrementUserStats(userId, UserStatsRedisEnum.USER_COIN.getField(), rewardCoinCount);

        UserActionSyncDTO rewardDTO = new UserActionSyncDTO();
        rewardDTO.setVideoId(videoId);
        rewardDTO.setVideoUserId(userId);
        rewardDTO.setActionCount(rewardCoinCount);
        rewardDTO.setActionTime(new Date());
        addUserActionQueue(InteractRedisKeys.VIDEO_AUDIT_REWARD_QUEUE, rewardDTO);
    }

    public Long incrementUserStats(String userId, String field, long count) {
        String key = InteractRedisKeys.USER_STATS_KEY + userId;
        Long value = redisUtils.hincr(key, field, count);
        redisUtils.expire(key, (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
        return value;
    }

    public Integer getUserStatsValue(String userId, String field) {
        Object value = redisUtils.hget(InteractRedisKeys.USER_STATS_KEY + userId, field);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    public void setUserStatsValue(String userId, String field, Integer value) {
        Map<String, Object> statsMap = new HashMap<>(1);
        statsMap.put(field, value);
        redisUtils.hmset(InteractRedisKeys.USER_STATS_KEY + userId,
                statsMap,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public Long addVideoActionCountDelta(String videoId, String field, long delta) {
        String key = InteractRedisKeys.VIDEO_ACTION_COUNT_DELTA + videoId;
        Long value = redisUtils.hincr(key, field, delta);
        redisUtils.expire(key, Constants.REDIS_EXPIRE_TIME_TWO_DAY);
        return value;
    }

    public Map<String, Integer> getVideoActionCountDelta(String videoId) {
        Map<Object, Object> valueMap = redisUtils.hmget(InteractRedisKeys.VIDEO_ACTION_COUNT_DELTA + videoId);
        if (valueMap == null || valueMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> resultMap = new HashMap<>(valueMap.size());
        for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                resultMap.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
            }
        }
        return resultMap;
    }

    private String buildVideoActionStatusKey(String userId, String videoId, Integer actionType) {
        return InteractRedisKeys.VIDEO_ACTION_STATUS_KEY + userId + ":" + videoId + ":" + actionType;
    }

    private String buildCommentActionStatusKey(String userId, Integer commentId) {
        return InteractRedisKeys.COMMENT_ACTION_STATUS_KEY + userId + ":" + commentId;
    }
}
