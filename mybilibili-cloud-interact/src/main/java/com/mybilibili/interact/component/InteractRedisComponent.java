package com.mybilibili.interact.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserActionSyncDTO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.base.utils.JsonUtils;
import com.mybilibili.interact.constants.InteractRedisKeys;
import com.mybilibili.interact.entity.po.VideoDanmu;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * interact 服务缓存组件。
 *
 * <p>点赞、收藏、投币这类操作需要 Redis 做幂等和计数原子更新，
 * 后续同步 MySQL 的队列也放在 interact 边界内维护。</p>
 */
@Component
public class InteractRedisComponent {

    /**
     * 单个视频分片最多缓存的弹幕数量。
     *
     * <p>热点视频弹幕量很大，ZSet 不能无限长；超过上限后按时间轴裁掉最早的一段，
     * 保住当前播放页最常访问的热数据。</p>
     */
    private static final int DANMU_CACHE_MAX_SIZE = 5000;

    /**
     * 弹幕热缓存有效期。
     *
     * <p>播放页会频繁刷新弹幕，30 分钟能覆盖大多数热点观看窗口；
     * 冷视频缓存过期后再从 MySQL 回源即可。</p>
     */
    private static final long DANMU_CACHE_TTL = 30L * Constants.REDIS_EXPIRE_TIME_ONE_MINUTE;

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

        return redisUtils.executeLongScriptWithStringArgs(
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

        return redisUtils.executeLongScriptWithStringArgs(
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

    public String saveVideoDanmuCache(VideoDanmu videoDanmu) {
        if (videoDanmu == null || videoDanmu.getVideoId() == null || videoDanmu.getFileId() == null) {
            return null;
        }
        String cacheKey = buildVideoDanmuCacheKey(videoDanmu.getVideoId(), videoDanmu.getFileId());
        String cacheValue = JsonUtils.convertObj2Json(videoDanmu);
        // score 使用弹幕出现时间，loadDanmu 读取时就不用再在 Java 里额外排序。
        boolean success = redisUtils.zadd(cacheKey, cacheValue, videoDanmu.getTime(), DANMU_CACHE_TTL);
        if (!success) {
            return null;
        }
        trimDanmuCache(cacheKey);
        return cacheValue;
    }

    public List<VideoDanmu> loadVideoDanmuCache(String videoId, String fileId) {
        Set<Object> cacheValueSet = redisUtils.zrange(buildVideoDanmuCacheKey(videoId, fileId), 0, -1);
        if (cacheValueSet == null || cacheValueSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<VideoDanmu> resultList = new ArrayList<>(cacheValueSet.size());
        for (Object cacheValue : cacheValueSet) {
            if (cacheValue == null) {
                continue;
            }
            // Redis 里存完整弹幕 JSON，避免播放页回源时还要拼装多个字段。
            VideoDanmu videoDanmu = JsonUtils.convertJson2Obj(cacheValue.toString(), VideoDanmu.class);
            if (videoDanmu != null) {
                resultList.add(videoDanmu);
            }
        }
        return resultList;
    }

    public void rebuildVideoDanmuCache(String videoId, String fileId, List<VideoDanmu> danmuList) {
        String cacheKey = buildVideoDanmuCacheKey(videoId, fileId);
        // 回源重建前先删旧缓存，避免 MySQL 老数据和刚查询结果混在一个分片里。
        redisUtils.delete(cacheKey);
        if (danmuList == null || danmuList.isEmpty()) {
            return;
        }
        for (VideoDanmu videoDanmu : danmuList) {
            saveVideoDanmuCache(videoDanmu);
        }
    }

    public void removeVideoDanmuCache(String videoId, String fileId, String cacheValue) {
        if (cacheValue == null) {
            return;
        }
        redisUtils.zremove(buildVideoDanmuCacheKey(videoId, fileId), cacheValue);
    }

    public void deleteVideoDanmuCache(String videoId, String fileId) {
        redisUtils.delete(buildVideoDanmuCacheKey(videoId, fileId));
    }

    private void trimDanmuCache(String cacheKey) {
        Long cacheSize = redisUtils.getZSetSize(cacheKey);
        if (cacheSize == null || cacheSize <= DANMU_CACHE_MAX_SIZE) {
            return;
        }
        // ZSet 正序排列，rank 小的是较早时间点的弹幕，优先裁掉这部分冷数据。
        redisUtils.zremRangeByRank(cacheKey, 0, cacheSize - DANMU_CACHE_MAX_SIZE - 1);
    }

    private String buildVideoActionStatusKey(String userId, String videoId, Integer actionType) {
        return InteractRedisKeys.VIDEO_ACTION_STATUS_KEY + userId + ":" + videoId + ":" + actionType;
    }

    private String buildCommentActionStatusKey(String userId, Integer commentId) {
        return InteractRedisKeys.COMMENT_ACTION_STATUS_KEY + userId + ":" + commentId;
    }

    private String buildVideoDanmuCacheKey(String videoId, String fileId) {
        return InteractRedisKeys.VIDEO_DANMU_CACHE + videoId + ":" + fileId;
    }
}
