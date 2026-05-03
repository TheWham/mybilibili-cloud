package com.mybilibili.video.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoHistoryDeleteDTO;
import com.mybilibili.base.entity.po.CategoryInfo;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.video.constants.VideoRedisKeys;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * video 服务缓存组件。
 *
 * <p>视频播放链路写入频率高，统一收口在 video 模块，后续做任务同步或限流时边界更清楚。</p>
 */
@Component
public class VideoRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public void saveCategoryList2Redis(List<CategoryInfo> categoryList) {
        redisUtils.set(VideoRedisKeys.CATEGORY_KEY, categoryList);
    }

    public List<CategoryInfo> getCategoryList() {
        Object value = redisUtils.get(VideoRedisKeys.CATEGORY_KEY);
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof CategoryInfo)) {
            return (List<CategoryInfo>) list;
        }
        return JSON.parseArray(JSON.toJSONString(value), CategoryInfo.class);
    }

    public Integer reportVideoPlayOnline(String fileId, String deviceId) {
        String videoUserOnlineKey = String.format(VideoRedisKeys.VIDEO_PLAY_COUNT_USER, fileId, deviceId);
        String playCountOnlineKey = String.format(VideoRedisKeys.VIDEO_PLAY_COUNT_ONLINE, fileId);
        if (!redisUtils.keyExists(videoUserOnlineKey)) {
            redisUtils.setex(videoUserOnlineKey, fileId, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 8L);
            return redisUtils.incrementex(playCountOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 10L).intValue();
        }
        redisUtils.expire(videoUserOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 8L);
        redisUtils.expire(playCountOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 10L);
        Object count = redisUtils.get(playCountOnlineKey);
        return count == null ? 1 : Integer.parseInt(count.toString());
    }

    public void saveVideoHistory(String videoId, String userId, Integer fileIndex) {
        long timestamp = System.currentTimeMillis();
        String historyKey = VideoRedisKeys.VIDEO_PLAY_HISTORY + userId;
        String fileIndexKey = VideoRedisKeys.VIDEO_PLAY_HISTORY_FILE_INDEX + userId;

        redisUtils.zaddCount4VideoHistory(historyKey, videoId, (double) timestamp);

        Map<String, Object> fileIndexMap = new HashMap<>(1);
        fileIndexMap.put(videoId, fileIndex == null ? 1 : fileIndex);
        redisUtils.hmset(fileIndexKey, fileIndexMap,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_90);

        List<String> historyList = redisUtils.getZSetList(historyKey, -1);
        if (historyList.size() > Constants.LENGTH_1000) {
            List<String> expiredVideoIds = new ArrayList<>(historyList.subList(Constants.LENGTH_1000, historyList.size()));
            redisUtils.zremove(historyKey, expiredVideoIds.toArray());
            redisUtils.hdel(fileIndexKey, expiredVideoIds.toArray());
            for (String expiredVideoId : expiredVideoIds) {
                VideoHistoryDeleteDTO deleteDTO = new VideoHistoryDeleteDTO();
                deleteDTO.setUserId(userId);
                deleteDTO.setVideoId(expiredVideoId);
                addVideoHistoryDeleteQueue(deleteDTO);
            }
        }

        redisUtils.expire(historyKey, (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_90);
        redisUtils.zaddUserId(VideoRedisKeys.DIRTY_HISTORY_USER, userId);
    }

    public boolean saveVideoPlayCount2HLL(String videoId, String userId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = VideoRedisKeys.VIDEO_PLAY_COUNT + dateStr + ":" + videoId;
        Long addCount = redisUtils.saveVideoPlayCount2HLL(key, userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * 2);
        return addCount != null && addCount > 0;
    }

    public boolean saveVideoEffectivePlay(String videoId, String userId) {
        String key = VideoRedisKeys.VIDEO_PLAY_EFFECTIVE + videoId + ":" + userId;
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_VIDEO_PLAY_EFFECTIVE_EXPIRE_MINUTES;
        return redisUtils.setIfAbsent(key, videoId, expireTime);
    }

    public void addVideoPlayCountDelta(String videoId) {
        redisUtils.hincr(VideoRedisKeys.VIDEO_PLAY_COUNT_DELTA, videoId, 1);
        redisUtils.expire(VideoRedisKeys.VIDEO_PLAY_COUNT_DELTA, Constants.REDIS_EXPIRE_TIME_TWO_DAY);
    }

    public Map<String, Integer> getAllVideoPlayCountDelta() {
        Map<Object, Object> valueMap = redisUtils.hmget(VideoRedisKeys.VIDEO_PLAY_COUNT_DELTA);
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

    public void clearVideoPlayCountDelta(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return;
        }
        redisUtils.hdel(VideoRedisKeys.VIDEO_PLAY_COUNT_DELTA, videoIds.toArray());
    }

    public Set<String> getDirtyHistoryUsers() {
        return redisUtils.getSetMembers(VideoRedisKeys.DIRTY_HISTORY_USER);
    }

    public void clearDirtyHistoryUser(String userId) {
        redisUtils.removeSetMember(VideoRedisKeys.DIRTY_HISTORY_USER, userId);
    }

    public List<String> getVideoHistoryList(String userId) {
        return redisUtils.getZSetList(VideoRedisKeys.VIDEO_PLAY_HISTORY + userId, -1);
    }

    public Set<ZSetOperations.TypedTuple<String>> getVideoHistoryWithScores(String userId) {
        return redisUtils.getZSetWithScores(VideoRedisKeys.VIDEO_PLAY_HISTORY + userId, -1);
    }

    public Set<ZSetOperations.TypedTuple<String>> getVideoHistoryWithScoresByPage(String userId, long start, long end) {
        return redisUtils.getZSetWithScoresByRange(VideoRedisKeys.VIDEO_PLAY_HISTORY + userId, start, end);
    }

    public Long getVideoHistoryCount(String userId) {
        return redisUtils.getZSetSize(VideoRedisKeys.VIDEO_PLAY_HISTORY + userId);
    }

    public Map<String, Integer> getVideoHistoryFileIndexMap(String userId) {
        Map<Object, Object> valueMap = redisUtils.hmget(VideoRedisKeys.VIDEO_PLAY_HISTORY_FILE_INDEX + userId);
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

    public Long delHistory(String userId, String videoId) {
        String videoHistoryIndex = VideoRedisKeys.VIDEO_PLAY_HISTORY_FILE_INDEX + userId;
        String videoHistoryPlay = VideoRedisKeys.VIDEO_PLAY_HISTORY + userId;
        return redisUtils.hdel(videoHistoryIndex, videoId) + redisUtils.zremove(videoHistoryPlay, videoId);
    }

    public void cleanHistory(String userId) {
        redisUtils.delete(VideoRedisKeys.VIDEO_PLAY_HISTORY_FILE_INDEX + userId, VideoRedisKeys.VIDEO_PLAY_HISTORY + userId);
    }

    public void addVideoHistoryDeleteQueue(VideoHistoryDeleteDTO deleteDTO) {
        redisUtils.lpush(VideoRedisKeys.VIDEO_HISTORY_DELETE_QUEUE, deleteDTO, 0L);
    }

    public VideoHistoryDeleteDTO getNextVideoHistoryDeleteQueue() {
        return (VideoHistoryDeleteDTO) redisUtils.rpop(VideoRedisKeys.VIDEO_HISTORY_DELETE_QUEUE);
    }
}
