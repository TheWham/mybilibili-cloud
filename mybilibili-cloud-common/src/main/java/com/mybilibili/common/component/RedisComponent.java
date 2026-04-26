package com.mybilibili.common.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.*;
import com.mybilibili.base.entity.po.CategoryInfo;
import com.mybilibili.base.entity.po.SysSetting;
import com.mybilibili.base.entity.po.UserMessage;
import com.mybilibili.base.entity.po.VideoInfoFilePost;
import com.mybilibili.base.entity.vo.UserInfoVO;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.common.services.SysSettingService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mybilibili.base.constants.Constants.REDIS_EXPIRE_TIME_DAY_COUNT;
import static com.mybilibili.base.constants.Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT;


@Component("redisComponent")
public class RedisComponent {
    private static final long SYS_SETTING_ID = 1L;

    @Resource
    public RedisUtils redisUtils;
    @Resource
    private SysSettingService sysSettingService;

//TODO redisComponent 需要拆分各自模块中 目前暂时融合在一起

    public String saveCode(String code){
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_CHECK_CODE_KEY + checkCodeKey, code, Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * REDIS_EXPIRE_TIME_MINUTE_COUNT);
        return checkCodeKey;
    }

    public String getCode(String checkCodeKey)
    {
        String aCheckCodeKey = Constants.REDIS_CHECK_CODE_KEY + checkCodeKey;
        Object ans = redisUtils.get(aCheckCodeKey);
        if (Objects.isNull(ans))
            throw new BusinessException("验证码失效");
        return ans.toString();
    }

    public void cleanCheckCode(String checkCodeKey)
    {
        if (Objects.isNull(this.getCode(checkCodeKey)))
            return;
        redisUtils.delete(checkCodeKey);
    }

    public void saveTokenUserInfo(TokenUserInfoDTO tokenUserInfoDTO) {
        // 存放登录信息
        String tokenId = UUID.randomUUID().toString();
        tokenUserInfoDTO.setExpireAt(System.currentTimeMillis() + Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
        tokenUserInfoDTO.setTokenId(tokenId);
        redisUtils.setex(Constants.REDIS_WEB_TOKEN_KEY + tokenId, tokenUserInfoDTO, Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public String saveToken4Admin(String account) {
        // 存放登录信息
        String tokenId = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_ADMIN_TOKEN_KEY + tokenId, account, Constants.REDIS_EXPIRE_TIME_ONE_DAY);
        return tokenId;
    }


    public void cleanWebToken(String tokenId) {
        // 清除web端token信息
        String webTokenKey = Constants.REDIS_WEB_TOKEN_KEY + tokenId;
        redisUtils.delete(webTokenKey);
    }

    public void cleanAdminToken(String tokenId) {
        // 清除admin端token信息
        String webTokenKey = Constants.REDIS_ADMIN_TOKEN_KEY + tokenId;
        redisUtils.delete(webTokenKey);
    }

    public TokenUserInfoDTO getTokenInfo(String tokenId) {
        String tokenKey = Constants.REDIS_WEB_TOKEN_KEY + tokenId;
        return (TokenUserInfoDTO) redisUtils.get(tokenKey);
    }

    //清除已经登录token实现单点登录
    public void cleanExistToken(String userId)
    {
        String userTokenKey = Constants.REDIS_USER_TOKEN_KEY + userId;
        redisUtils.delete(userTokenKey);
    }

    public void cleanUserLoginToken(String userId) {
        // 用户被后台禁用时，需要把 userId -> tokenId 的映射和真正的 token 都清掉，
        // 否则旧 token 在过期前依然可以继续访问接口。
        String tokenId = getTokenIdByUserId(userId);
        if (tokenId != null) {
            cleanWebToken(tokenId);
        }
        cleanExistToken(userId);
    }

    public String getTokenIdByUserId(String userId)
    {
        String userTokenKey = Constants.REDIS_USER_TOKEN_KEY + userId;
        Object value = redisUtils.get(userTokenKey);
        if (Objects.isNull(value))
            return null;
        return value.toString();
    }

    public void saveTokenIdByUserId(String userId, String token) {
        // 通过userId存放token
        redisUtils.setex(Constants.REDIS_USER_TOKEN_KEY + userId, token, Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public Object getTokenInfo4Admin(String tokenId) {
        String tokenKey = Constants.REDIS_ADMIN_TOKEN_KEY + tokenId;
        return redisUtils.get(tokenKey);
    }

    public void saveCategoryList2Redis(List<CategoryInfo> categoryList) {
        String categoryListKey = Constants.REDIS_ADMIN_CATEGORY_KEY;
        redisUtils.set(categoryListKey, categoryList);
    }

    public List<CategoryInfo> getCategoryList()
    {

        Object value = redisUtils.get(Constants.REDIS_ADMIN_CATEGORY_KEY);
        if (Objects.isNull(value)) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty() || list.get(0) instanceof CategoryInfo) {
                @SuppressWarnings("unchecked")
                List<CategoryInfo> result = (List<CategoryInfo>) list;
                return result;
            }
        }
        return JSON.parseArray(JSON.toJSONString(value), CategoryInfo.class);
    }

    public void saveFileInfo(String userId, UploadingFileDTO uploadingFileDto) {
        String redisUploadFileKey = Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + userId + uploadingFileDto.getUploadId();
        redisUtils.setex(redisUploadFileKey, uploadingFileDto, Constants.REDIS_EXPIRE_TIME_ONE_DAY);
    }
    public UploadingFileDTO getUploadFileInfo(String key)
    {
       return (UploadingFileDTO) redisUtils.get(key);
    }

    public SysSettingDTO getSysSetting()
    {
        Object sysSetting = redisUtils.get(Constants.REDIS_SYS_SETTING_KEY);
        if (sysSetting != null) {
            if (sysSetting instanceof SysSettingDTO dto) {
                return dto;
            }
            return JSON.parseObject(JSON.toJSONString(sysSetting), SysSettingDTO.class);
        }

        SysSetting sysSettingDb = sysSettingService.getSysSettingById(SYS_SETTING_ID);
        if (sysSettingDb == null) {
            // sys_setting 是全局单行配置表，空表时主动落一条默认记录，
            // 后续所有读写都围绕 id=1 这条记录展开，避免每次读取都走兜底分支。
            SysSetting initSetting = SysSettingDTO.createDefault().toSysSetting();
            initSetting.setId(SYS_SETTING_ID);
            sysSettingService.add(initSetting);
            sysSettingDb = sysSettingService.getSysSettingById(SYS_SETTING_ID);
        }
        SysSettingDTO sysSettingDTO = SysSettingDTO.fromSysSetting(sysSettingDb);
        redisUtils.set(Constants.REDIS_SYS_SETTING_KEY, sysSettingDTO);
        return sysSettingDTO;
    }

    public void delUploadVideoInfo(String userId, @NotEmpty String uploadId) {
        String key = Constants.REDIS_WEB_UPLOADING_FILE_INFO_KEY + userId + uploadId;
        redisUtils.delete(key);
    }

    public void addFileList2DelQueue(String videoId, List<String> filePathList) {
        String key = Constants.REDIS_WEB_ADD_DEL_QUEUE_KEY + videoId;
        redisUtils.lpushAll(key, filePathList, Constants.REDIS_EXPIRE_TIME_ONE_DAY*7);
    }

    public void addFileList2TransferQueue(List<VideoInfoFilePost> addList) {
        String key = Constants.REDIS_WEB_ADD_TRANSFER_QUEUE_KEY;
        redisUtils.lpushAll(key, addList, 0);
    }

    public VideoInfoFilePost getTransferVideoInfo4Queue() {
        String key = Constants.REDIS_WEB_ADD_TRANSFER_QUEUE_KEY;
        return (VideoInfoFilePost)redisUtils.rpop(key);
    }

    public VideoInfoFilePost getTransferVideoInfo4QueueBlock() {
        String key = Constants.REDIS_WEB_ADD_TRANSFER_QUEUE_KEY;
        // 转码队列空闲时直接阻塞等待，避免任务线程一直轮询 Redis。
        return (VideoInfoFilePost) redisUtils.brpop(key, Constants.REDIS_QUEUE_BLOCK_SECONDS, TimeUnit.SECONDS);
    }

    public List<String> getDelFilePathsQueue(String videoId) {
        String key = Constants.REDIS_WEB_ADD_DEL_QUEUE_KEY + videoId;
        List list = redisUtils.getQueueList(key);
        return list;
    }

    public void cleanDelFilePaths(String videoId){
        String key = Constants.REDIS_WEB_ADD_DEL_QUEUE_KEY + videoId;
        redisUtils.delete(key);
    }

    public Integer reportVideoPlayOnline(String fileId, String deviceId) {
        String videoUserOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER, fileId, deviceId);
        String playCountOnlineKey = String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE, fileId);

        Integer count = null;
        if (!redisUtils.keyExists(videoUserOnlineKey))
        {
            redisUtils.setex(videoUserOnlineKey, fileId,Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 8);
            count = redisUtils.incrementex(playCountOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 10).intValue();
            return count;
        }

        redisUtils.expire(videoUserOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 8);
        redisUtils.expire(playCountOnlineKey, Constants.REDIS_EXPIRE_TIME_ONE_SECOND * 10);
        count = (Integer)redisUtils.get(playCountOnlineKey);
        count = count == null ? 1 : count;
        return count;
    }

    public void updateTokenUserInfo(TokenUserInfoDTO tokenUserInfoDTO) {
        redisUtils.setex(Constants.REDIS_WEB_TOKEN_KEY + tokenUserInfoDTO.getTokenId(), tokenUserInfoDTO, Constants.REDIS_EXPIRE_TIME_ONE_DAY * REDIS_EXPIRE_TIME_DAY_COUNT);
    }

    public UserInfoVO getUserInfoVOInRedis(String userId) {
        Object o = redisUtils.get(Constants.REDIS_WEB_USER_INFO_KEY + userId);
        if (o == null)
            return null;
        return (UserInfoVO) o;
    }

    public void saveUserInfoVOInRedis(UserInfoVO userInfoVO) {
        // 存放登录信息
        redisUtils.setex(Constants.REDIS_WEB_USER_INFO_KEY + userInfoVO.getUserId(), userInfoVO, Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * REDIS_EXPIRE_TIME_MINUTE_COUNT * 6);
    }

    public void delUserInfoInRedis(String userId) {
        redisUtils.delete(Constants.REDIS_WEB_USER_INFO_KEY + userId);
    }

    public void saveUserStatsInfo(String userId, Map<String, Integer> userStatsInfo, String date) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_KEY + date + ":" + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    //设置过期时间为两天避免昨天数据过期
    public void flashUserStatsExpire(String userId, String date) {
        redisUtils.expire(Constants.REDIS_WEB_USER_STATS_KEY + date + ":" + userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public HashMap<String, Integer> getUserStatsInfo(String userId, String date) {
        return (HashMap<String, Integer>) redisUtils.hmget(Constants.REDIS_WEB_USER_STATS_KEY + date + ":" + userId);
    }

    public void saveVideoActionStatus(String userId, String videoId, Integer actionType, Integer actionCount) {
        long ttl = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_ACTION_STATUS_CACHE_TTL_MINUTES;
        redisUtils.setex(buildVideoActionStatusKey(userId, videoId, actionType), actionCount, ttl);
    }

    public boolean hasVideoActionStatus(String userId, String videoId, Integer actionType) {
        return redisUtils.keyExists(buildVideoActionStatusKey(userId, videoId, actionType));
    }

    public Integer getVideoActionStatus(String userId, String videoId, Integer actionType) {
        Object value = redisUtils.get(buildVideoActionStatusKey(userId, videoId, actionType));
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public void removeVideoActionStatus(String userId, String videoId, Integer actionType) {
        redisUtils.delete(buildVideoActionStatusKey(userId, videoId, actionType));
    }

    public void saveCommentActionStatus(String userId, Integer commentId, Integer actionType) {
        redisUtils.set(buildCommentActionStatusKey(userId, commentId), actionType);
    }

    public Long executeVideoToggleAction(String userId, String videoUserId, String videoId, Integer actionType, Integer actionCount, String statsField) {
        String actionStatusKey = buildVideoActionStatusKey(userId, videoId, actionType);
        String ownerStatsKey = Constants.REDIS_WEB_USER_STATS_KEY + videoUserId;
        long statsTtl = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS;
        long actionStatusTtl = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_ACTION_STATUS_CACHE_TTL_MINUTES;

        // 点赞和收藏都是“存在则取消，不存在则新增”的切换型动作。
        // 这里统一走一套 Lua，把状态切换和计数增减一次做完，避免并发下先查再改出现抖动。
        return redisUtils.executeLongScript(
                Constants.REDIS_LUA_VIDEO_TOGGLE_ACTION,
                Arrays.asList(actionStatusKey, ownerStatsKey),
                statsField,
                actionCount,
                statsTtl,
                actionStatusTtl
        );
    }

    public Long executeVideoCoinAction(String userId, String videoUserId, String videoId, Integer actionType, Integer actionCount) {
        String actionStatusKey = buildVideoActionStatusKey(userId, videoId, actionType);
        String senderStatsKey = Constants.REDIS_WEB_USER_STATS_KEY + userId;
        String receiverStatsKey = Constants.REDIS_WEB_USER_STATS_KEY + videoUserId;
        long statsTtl = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS;

        // 投币是“查状态 + 扣自己硬币 + 加对方硬币 + 记视频投币数”的组合操作。
        // 这几步拆开执行时最容易在高并发下出现重复投币或硬币扣错，所以这里用 Lua 一次做完。
        return redisUtils.executeLongScript(
                Constants.REDIS_LUA_VIDEO_COIN,
                Arrays.asList(actionStatusKey, senderStatsKey, receiverStatsKey),
                UserStatsRedisEnum.USER_COIN.getField(),
                UserStatsRedisEnum.VIDEO_COIN.getField(),
                actionCount,
                statsTtl
        );
    }

    public Integer getCommentActionStatus(String userId, Integer commentId) {
        Object value = redisUtils.get(buildCommentActionStatusKey(userId, commentId));
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public void removeCommentActionStatus(String userId, Integer commentId) {
        redisUtils.delete(buildCommentActionStatusKey(userId, commentId));
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

        // 审核奖励属于“平台单边发币”，这里只加作者自己的当前硬币数，
        // totalCoinCount 留给异步任务一起补进 MySQL，避免前台刚审核通过时页面还是旧值。
        incrementUserStats(userId, UserStatsRedisEnum.USER_COIN.getField(), rewardCoinCount);

        UserActionSyncDTO rewardDTO = new UserActionSyncDTO();
        rewardDTO.setVideoId(videoId);
        rewardDTO.setVideoUserId(userId);
        rewardDTO.setActionCount(rewardCoinCount);
        rewardDTO.setActionTime(new Date());
        addUserActionQueue(Constants.REDIS_WEB_ACTION_VIDEO_AUDIT_REWARD_QUEUE_KEY, rewardDTO);
    }

    public void addUserMessageQueue(UserMessage userMessage) {
        redisUtils.lpush(Constants.REDIS_WEB_USER_MESSAGE_QUEUE_KEY, userMessage, 0L);
    }

    public UserMessage getNextUserMessageQueue() {
        return (UserMessage) redisUtils.rpop(Constants.REDIS_WEB_USER_MESSAGE_QUEUE_KEY);
    }

    public void addVideoHistoryDeleteQueue(VideoHistoryDeleteDTO deleteDTO) {
        redisUtils.lpush(Constants.REDIS_WEB_VIDEO_HISTORY_DELETE_QUEUE_KEY, deleteDTO, 0L);
    }

    public VideoHistoryDeleteDTO getNextVideoHistoryDeleteQueue() {
        // 这里只是从“已产生的删除队列”里取下一条待删记录。
        // 真正把视频放进删除队列的时机，是 saveVideoHistory() 里发现用户历史超过 1000 条并发生裁剪的时候。
        return (VideoHistoryDeleteDTO) redisUtils.rpop(Constants.REDIS_WEB_VIDEO_HISTORY_DELETE_QUEUE_KEY);
    }

    public Long incrementUserStats(String userId, String field, long count) {
        String key = Constants.REDIS_WEB_USER_STATS_KEY + userId;
        Long value = redisUtils.hincr(key, field, count);
        redisUtils.expire(key, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
        return value;
    }

    public Integer getUserStatsValue(String userId, String field) {
        Object value = redisUtils.hget(Constants.REDIS_WEB_USER_STATS_KEY + userId, field);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

    public void setUserStatsValue(String userId, String field, Integer value) {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put(field, value);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_KEY + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public void saveRealtimeUserStatsInfo(String userId, Map<String, Integer> userStatsInfo) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_KEY + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public HashMap<String, Integer> getRealtimeUserStatsInfo(String userId) {
        return (HashMap<String, Integer>) redisUtils.hmget(Constants.REDIS_WEB_USER_STATS_KEY + userId);
    }

    public void refreshRealtimeUserStatsExpire(String userId) {
        redisUtils.expire(Constants.REDIS_WEB_USER_STATS_KEY + userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public void saveUserStatsSnapshot(String userId, String statsDay, Map<String, Integer> userStatsInfo) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(Constants.REDIS_WEB_USER_STATS_SNAPSHOT_KEY + statsDay + ":" + userId, statsMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public HashMap<String, Integer> getUserStatsSnapshot(String userId, String statsDay) {
        return (HashMap<String, Integer>) redisUtils.hmget(Constants.REDIS_WEB_USER_STATS_SNAPSHOT_KEY + statsDay + ":" + userId);
    }

    public Set<String> getUserStatsSnapshotKeys(String statsDay) {
        return redisUtils.getByKeyPrefix(Constants.REDIS_WEB_USER_STATS_SNAPSHOT_KEY + statsDay + ":");
    }

    private String buildVideoActionStatusKey(String userId, String videoId, Integer actionType) {
        return Constants.REDIS_WEB_ACTION_VIDEO_STATUS_KEY + userId + ":" + videoId + ":" + actionType;
    }

    private String buildCommentActionStatusKey(String userId, Integer commentId) {
        return Constants.REDIS_WEB_ACTION_COMMENT_STATUS_KEY + userId + ":" + commentId;
    }

    public void saveKeyword(String keyword)
    {
        redisUtils.zaddCount(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, keyword);
    }

    public List<String> getSearchKeywordTop(Integer top) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_SEARCH_COUNT, top - 1);
    }

    /**
     * 保存播放历史记录
     * @param videoId 视频id
     * @param userId 观看用户id
     * @param fileIndex 视频批号
     */
    public void saveVideoHistory(String videoId, String userId, Integer fileIndex) {
        // ZSet 只保存视频本身和最后观看时间，用来做“最近观看”排序。
        // fileIndex 这类附加信息单独放 Hash，这样重复观看同一视频时就可以直接更新，不会新增脏数据。
        long timestamp = System.currentTimeMillis();
        String historyKey = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId;
        String fileIndexKey = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY_FILE_INDEX + userId;

        // member 直接用 videoId，重复看同一个视频时 Redis 会更新 score，而不是再插一条新历史。
        redisUtils.zaddCount4VideoHistory(historyKey, videoId, (double) timestamp);

        Map<String, Object> fileIndexMap = new HashMap<>(1);
        fileIndexMap.put(videoId, fileIndex == null ? 1 : fileIndex);
        redisUtils.hmset(fileIndexKey, fileIndexMap, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_90);

        // Redis 以最近观看时间倒序保留前 1000 条，多余的视频从排序结构和 fileIndex 明细里一起删掉。
        List<String> historyList = redisUtils.getZSetList(historyKey, -1);
        if (historyList.size() > Constants.LENGTH_1000) {
            List<String> expiredVideoIds = new ArrayList<>(historyList.subList(Constants.LENGTH_1000, historyList.size()));
            redisUtils.zremove(historyKey, expiredVideoIds.toArray());
            redisUtils.hdel(fileIndexKey, expiredVideoIds.toArray());

            // 这里淘汰掉的就是 Redis 已经不再保留的旧历史，后面再异步删数据库即可。
            for (String expiredVideoId : expiredVideoIds) {
                VideoHistoryDeleteDTO deleteDTO = new VideoHistoryDeleteDTO();
                deleteDTO.setUserId(userId);
                deleteDTO.setVideoId(expiredVideoId);
                addVideoHistoryDeleteQueue(deleteDTO);
            }
        }

        // 两份结构共用同一份保留周期，过期后这批历史会整体淘汰。
        redisUtils.expire(historyKey, Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_90);

        // 把该 userId 放入一个待同步 Set 集合中
        redisUtils.zaddUserId(Constants.REDIS_KEY_DIRTY_HISTORY_USER, userId);
    }

    /**
     * 保存播放量到hll
     * @param videoId 视频id
     * @param userId 观看用户id
     */
    public boolean saveVideoPlayCount2HLL(String videoId, String userId)
    {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String key = Constants.REDIS_KEY_VIDEO_PLAY_COUNT + dateStr + ":" + videoId;
        Long addCount = redisUtils.saveVideoPlayCount2HLL(key, userId, Constants.REDIS_EXPIRE_TIME_ONE_DAY * 2);
        return addCount != null && addCount > 0;
    }

    public boolean saveVideoEffectivePlay(String videoId, String userId) {
        String key = Constants.REDIS_KEY_VIDEO_PLAY_EFFECTIVE + videoId + ":" + userId;
        long expireTime = Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_VIDEO_PLAY_EFFECTIVE_EXPIRE_MINUTES;
        return redisUtils.setIfAbsent(key, videoId, expireTime);
    }

    public void addVideoPlayCountDelta(String videoId) {
        redisUtils.hincr(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA, videoId, 1);
        redisUtils.expire(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA, Constants.REDIS_EXPIRE_TIME_TWO_DAY);
    }

    public Map<String, Integer> getAllVideoPlayCountDelta() {
        Map<Object, Object> valueMap = redisUtils.hmget(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA);
        if (valueMap == null || valueMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> resultMap = new HashMap<>(valueMap.size());
        for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            resultMap.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
        }
        return resultMap;
    }

    public void clearVideoPlayCountDelta(List<String> videoIds) {
        if (videoIds == null || videoIds.isEmpty()) {
            return;
        }
        redisUtils.hdel(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_DELTA, videoIds.toArray());
    }

    public Long addVideoActionCountDelta(String videoId, String field, long delta) {
        String key = Constants.REDIS_KEY_VIDEO_ACTION_COUNT_DELTA + videoId;
        Long value = redisUtils.hincr(key, field, delta);
        redisUtils.expire(key, Constants.REDIS_EXPIRE_TIME_TWO_DAY);
        return value;
    }

    public Map<String, Integer> getVideoActionCountDelta(String videoId) {
        Map<Object, Object> valueMap = redisUtils.hmget(Constants.REDIS_KEY_VIDEO_ACTION_COUNT_DELTA + videoId);
        if (valueMap == null || valueMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> resultMap = new HashMap<>(valueMap.size());
        for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            resultMap.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
        }
        return resultMap;
    }

    public Set<String> getDirtyHistoryUsers() {
        return redisUtils.getSetMembers(Constants.REDIS_KEY_DIRTY_HISTORY_USER);
    }

    public void clearDirtyHistoryUser(String userId) {
        redisUtils.removeSetMember(Constants.REDIS_KEY_DIRTY_HISTORY_USER, userId);
    }

    public List<String> getVideoHistoryList(String userId) {
        return redisUtils.getZSetList(Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId, -1);
    }

    public Set<ZSetOperations.TypedTuple<String>> getVideoHistoryWithScores(String userId) {
        return redisUtils.getZSetWithScores(Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId, -1);
    }

    public Set<ZSetOperations.TypedTuple<String>> getVideoHistoryWithScoresByPage(String userId, long start, long end) {
        return redisUtils.getZSetWithScoresByRange(Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId, start, end);
    }

    public Long getVideoHistoryCount(String userId) {
        return redisUtils.getZSetSize(Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId);
    }

    public Map<String, Integer> getVideoHistoryFileIndexMap(String userId) {
        Map<Object, Object> valueMap = redisUtils.hmget(Constants.REDIS_KEY_VIDEO_PLAY_HISTORY_FILE_INDEX + userId);
        if (valueMap == null || valueMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> resultMap = new HashMap<>(valueMap.size());
        for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            resultMap.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
        }
        return resultMap;
    }

    public Long delHistory(String userId, String videoId) {
        String videoHistoryIndex = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY_FILE_INDEX + userId;
        String videoHistoryPlay = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId;
        return redisUtils.hdel(videoHistoryIndex, videoId) + redisUtils.zremove(videoHistoryPlay, videoId);
    }

    public void cleanHistory(String userId) {
        String videoHistoryIndex = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY_FILE_INDEX + userId;
        String videoHistoryPlay = Constants.REDIS_KEY_VIDEO_PLAY_HISTORY + userId;
        redisUtils.delete(videoHistoryPlay, videoHistoryIndex);
    }

    public boolean setSysSetting(SysSettingDTO sysSettingDTO) {
        SysSetting sysSetting = sysSettingDTO.toSysSetting();
        sysSetting.setId(SYS_SETTING_ID);

        SysSetting currentSetting = sysSettingService.getSysSettingById(SYS_SETTING_ID);
        Integer effectRows;
        if (currentSetting == null) {
            effectRows = sysSettingService.add(sysSetting);
        } else {
            effectRows = sysSettingService.updateSysSettingById(sysSetting, SYS_SETTING_ID);
        }
        if (effectRows == null || effectRows == 0) {
            return false;
        }
        return redisUtils.set(Constants.REDIS_SYS_SETTING_KEY, sysSettingDTO);
    }
}
