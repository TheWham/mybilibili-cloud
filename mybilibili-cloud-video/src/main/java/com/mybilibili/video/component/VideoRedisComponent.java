package com.mybilibili.video.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.dto.AiSubtitleIndexTaskDTO;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.UploadingFileDTO;
import com.mybilibili.base.entity.dto.VideoHistoryDeleteDTO;
import com.mybilibili.base.entity.event.UserCoinSyncEvent;
import com.mybilibili.base.enums.AiSubtitleIndexStatusEnum;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.common.consumer.AdminSysSettingClient;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.video.constants.VideoRedisKeys;
import com.mybilibili.video.entity.dto.VideoPlayFileMetaDTO;
import com.mybilibili.video.entity.po.CategoryInfo;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * video 服务缓存组件。
 *
 * <p>视频播放链路写入频率高，统一收口在 video 模块，后续做任务同步或限流时边界更清楚。</p>
 */
@Component
public class VideoRedisComponent {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String AI_STATUS_FIELD = "status";
    private static final String AI_STATUS_NAME_FIELD = "statusName";
    private static final String AI_RETRY_COUNT_FIELD = "retryCount";
    private static final String AI_LAST_ERROR_FIELD = "lastError";
    private static final String AI_CREATE_TIME_FIELD = "createTime";
    private static final String AI_UPDATE_TIME_FIELD = "updateTime";
    private static final String AI_TASK_COUNT_FIELD = "taskCount";
    private static final String AI_SUCCESS_COUNT_FIELD = "successCount";
    private static final String AI_FAILED_COUNT_FIELD = "failedCount";

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AdminSysSettingClient adminSysSettingClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

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

    /**
     * 读取播放上报用的分 P 元数据。
     *
     * <p>这里只缓存已发布分 P 的轻量字段，不缓存播放量结果。命中后可以直接组装播放事件，
     * 把高频上报里的数据库查询挡在 Redis 前面。</p>
     *
     * @param fileId 分 P 文件 id
     * @return 分 P 元数据；缓存不存在时返回 null
     */
    public VideoPlayFileMetaDTO getVideoPlayFileMeta(String fileId) {
        Object value = redisUtils.get(VideoRedisKeys.VIDEO_PLAY_FILE_META + fileId);
        if (value == null) {
            return null;
        }
        if (value instanceof VideoPlayFileMetaDTO fileMeta) {
            return fileMeta;
        }
        return JSON.parseObject(JSON.toJSONString(value), VideoPlayFileMetaDTO.class);
    }

    /**
     * 缓存播放上报用的分 P 元数据。
     *
     * <p>TTL 控制在 1 天，既能覆盖热门视频的重复上报，又能在视频信息变更后自然过期。
     * 播放有效性仍由 {@link #saveVideoEffectivePlay(String, String)} 按 videoId + userId 判断。</p>
     *
     * @param fileMeta 分 P 元数据
     */
    public void saveVideoPlayFileMeta(VideoPlayFileMetaDTO fileMeta) {
        if (fileMeta == null || fileMeta.getFileId() == null || fileMeta.getFileId().isEmpty()) {
            return;
        }
        redisUtils.setex(VideoRedisKeys.VIDEO_PLAY_FILE_META + fileMeta.getFileId(),
                fileMeta,
                Constants.REDIS_EXPIRE_TIME_ONE_DAY);
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

    public Long addVideoActionCountDelta(String videoId, String field, long delta) {
        String key = VideoRedisKeys.VIDEO_ACTION_COUNT_DELTA + videoId;
        Long value = redisUtils.hincr(key, field, delta);
        redisUtils.expire(key, Constants.REDIS_EXPIRE_TIME_TWO_DAY);
        return value;
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
        if (userId == null || rewardCoinCount == null || rewardCoinCount <= 0) {
            return;
        }
        // 审核奖励要先写 Redis 实时统计，用户刷新个人中心时能马上看到硬币变化。
        incrementUserStats(userId, UserStatsRedisEnum.USER_COIN.getField(), rewardCoinCount);

        UserCoinSyncEvent rewardEvent = new UserCoinSyncEvent();
        rewardEvent.setEventId(UUID.randomUUID().toString().replace("-", ""));
        rewardEvent.setVideoUserId(userId);
        rewardEvent.setVideoId(videoId);
        rewardEvent.setActionCount(rewardCoinCount);
        rewardEvent.setAuditReward(true);
        rewardEvent.setActionTime(new Date());
        rabbitTemplate.convertAndSend(MqConstants.USER_ACTION_EXCHANGE,
                MqConstants.USER_COIN_SYNC_ROUTING_KEY,
                rewardEvent);
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

    public UploadingFileDTO getUploadFileInfo(String key) {
        return (UploadingFileDTO) redisUtils.get(key);
    }

    public void delUploadVideoInfo(String userId, String uploadId) {
        redisUtils.delete(VideoRedisKeys.UPLOADING_FILE_INFO_KEY + userId + uploadId);
    }

    public void addAiSubtitleIndexTask(AiSubtitleIndexTaskDTO task) {
        if (task == null) {
            return;
        }
        // Python worker 使用 json.loads 消费队列，这里必须写入原始 JSON 字符串，不能走 RedisTemplate JSON 序列化。
        stringRedisTemplate.opsForList().leftPush(VideoRedisKeys.AI_SUBTITLE_INDEX_QUEUE, JSON.toJSONString(task));
    }

    /**
     * 初始化 AI 字幕向量化状态。
     *
     * <p>状态要先于任务入队写入 Redis。worker 可能一启动就消费，如果先入队再写状态，
     * 很容易出现 worker 已经处理完，后台状态却被 video 服务重新覆盖成排队中的情况。</p>
     *
     * @param videoId   视频 ID
     * @param taskCount 本次需要处理的分 P 数量
     */
    public void initAiSubtitleIndexStatus(String videoId, int taskCount) {
        if (videoId == null || videoId.isBlank() || taskCount <= 0) {
            return;
        }
        String now = now();
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put(AI_STATUS_FIELD, AiSubtitleIndexStatusEnum.PENDING.getStatus());
        statusMap.put(AI_STATUS_NAME_FIELD, AiSubtitleIndexStatusEnum.PENDING.getDesc());
        statusMap.put(AI_RETRY_COUNT_FIELD, "0");
        statusMap.put(AI_LAST_ERROR_FIELD, "");
        statusMap.put(AI_CREATE_TIME_FIELD, now);
        statusMap.put(AI_UPDATE_TIME_FIELD, now);
        statusMap.put(AI_TASK_COUNT_FIELD, String.valueOf(taskCount));
        statusMap.put(AI_SUCCESS_COUNT_FIELD, "0");
        statusMap.put(AI_FAILED_COUNT_FIELD, "0");
        stringRedisTemplate.opsForHash().putAll(buildAiSubtitleIndexStatusKey(videoId), statusMap);
    }

    /**
     * 读取后台列表展示用的 AI 字幕索引状态。
     *
     * <p>没有状态记录时返回“未投递”，这样后台可以把审核状态和 AI 增强链路状态分开看。</p>
     *
     * @param videoId 视频 ID
     * @return AI 字幕索引状态码
     */
    public String getAiSubtitleIndexStatus(String videoId) {
        if (videoId == null || videoId.isBlank()) {
            return AiSubtitleIndexStatusEnum.UN_SUBMITTED.getStatus();
        }
        Object status = stringRedisTemplate.opsForHash().get(buildAiSubtitleIndexStatusKey(videoId), AI_STATUS_FIELD);
        if (status == null) {
            return AiSubtitleIndexStatusEnum.UN_SUBMITTED.getStatus();
        }
        return String.valueOf(status);
    }

    private String buildAiSubtitleIndexStatusKey(String videoId) {
        return VideoRedisKeys.AI_SUBTITLE_INDEX_STATUS + videoId;
    }

    private String now() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
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

