package com.mybilibili.video.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.AiSubtitleIndexTaskDTO;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.UploadingFileDTO;
import com.mybilibili.base.entity.dto.VideoHistoryDeleteDTO;
import com.mybilibili.common.consumer.AdminSysSettingClient;
import com.mybilibili.video.entity.po.CategoryInfo;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.video.constants.VideoRedisKeys;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
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
    @Resource
    private AdminSysSettingClient adminSysSettingClient;

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
        //显示用户最近1000条的历史记录
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

        //每个用户历史记录显示最近3个月的
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

    public Map<String, Integer> getVideoActionCountDelta(String videoId) {
        Map<Object, Object> valueMap = redisUtils.hmget(VideoRedisKeys.VIDEO_ACTION_COUNT_DELTA + videoId);
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

    public SysSettingDTO getSysSetting() {
        Object sysSetting = redisUtils.get(VideoRedisKeys.SYS_SETTING_KEY);
        if (sysSetting instanceof SysSettingDTO dto) {
            return dto;
        }
        if (sysSetting != null) {
            return JSON.parseObject(JSON.toJSONString(sysSetting), SysSettingDTO.class);
        }

        SysSettingDTO sysSettingDTO;
        try {
            sysSettingDTO = adminSysSettingClient.getSysSetting();
        } catch (Exception e) {
            // video 不直接查配置表。admin 不可用时只做短期兜底，避免投稿/弹幕限制读取直接失败。
            return SysSettingDTO.createDefault();
        }
        if (sysSettingDTO == null) {
            return SysSettingDTO.createDefault();
        }
        redisUtils.set(VideoRedisKeys.SYS_SETTING_KEY, sysSettingDTO);
        return sysSettingDTO;
    }

    public void addVideoAuditReward(String userId, String videoId, Integer rewardCoinCount) {
        Map<String, Object> rewardMap = new HashMap<>(3);
        rewardMap.put("userId", userId);
        rewardMap.put("videoId", videoId);
        rewardMap.put("rewardCoinCount", rewardCoinCount);
        redisUtils.lpush(VideoRedisKeys.VIDEO_AUDIT_REWARD_QUEUE, rewardMap, 0L);
    }

    public void addFileList2DelQueue(String videoId, List<String> filePathList) {
        redisUtils.lpushAll(VideoRedisKeys.DEL_FILE_QUEUE + videoId,
                filePathList,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public List<String> getDelFilePathsQueue(String videoId) {
        return redisUtils.getQueueList(VideoRedisKeys.DEL_FILE_QUEUE + videoId);
    }

    public void cleanDelFilePaths(String videoId) {
        redisUtils.delete(VideoRedisKeys.DEL_FILE_QUEUE + videoId);
    }

    public void addFileList2TransferQueue(List<VideoInfoFilePost> addList) {
        redisUtils.lpushAll(VideoRedisKeys.TRANSFER_FILE_QUEUE, addList, 0L);
    }

    public UploadingFileDTO getUploadFileInfo(String key) {
        return (UploadingFileDTO) redisUtils.get(key);
    }

    public void delUploadVideoInfo(String userId, String uploadId) {
        redisUtils.delete(VideoRedisKeys.UPLOADING_FILE_INFO_KEY + userId + uploadId);
    }

    public void addAiSubtitleIndexTask(AiSubtitleIndexTaskDTO task) {
        redisUtils.lpush(VideoRedisKeys.AI_SUBTITLE_INDEX_QUEUE, task, 0L);
    }

    public Long incrementUserStats(String userId, String field, long count) {
        String key = VideoRedisKeys.USER_STATS_KEY + userId;
        Long value = redisUtils.hincr(key, field, count);
        redisUtils.expire(key, (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
        return value;
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
