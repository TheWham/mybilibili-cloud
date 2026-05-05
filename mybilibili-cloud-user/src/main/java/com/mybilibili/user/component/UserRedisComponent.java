package com.mybilibili.user.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.vo.UserInfoVO;
import com.mybilibili.common.consumer.AdminSysSettingClient;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.user.constants.UserRedisKeys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * user 服务缓存组件。
 *
 * <p>这里只放用户资料、用户统计和注册所需的系统配置读取。视频播放、互动状态、
 * 消息队列等 Redis 操作分别放到对应服务。</p>
 */
@Component
public class UserRedisComponent {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AdminSysSettingClient adminSysSettingClient;

    public UserInfoVO getUserInfoVOInRedis(String userId) {
        Object value = redisUtils.get(UserRedisKeys.USER_INFO_KEY + userId);
        return value == null ? null : (UserInfoVO) value;
    }

    public void saveUserInfoVOInRedis(UserInfoVO userInfoVO) {
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT * 6;
        redisUtils.setex(UserRedisKeys.USER_INFO_KEY + userInfoVO.getUserId(), userInfoVO, expireTime);
    }

    public void delUserInfoInRedis(String userId) {
        redisUtils.delete(UserRedisKeys.USER_INFO_KEY + userId);
    }

    public void saveUserStatsInfo(String userId, Map<String, Integer> userStatsInfo, String date) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(UserRedisKeys.USER_STATS_KEY + date + ":" + userId,
                statsMap,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public void flashUserStatsExpire(String userId, String date) {
        redisUtils.expire(UserRedisKeys.USER_STATS_KEY + date + ":" + userId,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public HashMap<String, Integer> getUserStatsInfo(String userId, String date) {
        return (HashMap<String, Integer>) redisUtils.hmget(UserRedisKeys.USER_STATS_KEY + date + ":" + userId);
    }

    public Long incrementUserStats(String userId, String field, long count) {
        String key = UserRedisKeys.USER_STATS_KEY + userId;
        Long value = redisUtils.hincr(key, field, count);
        redisUtils.expire(key, (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
        return value;
    }

    public Integer getUserStatsValue(String userId, String field) {
        Object value = redisUtils.hget(UserRedisKeys.USER_STATS_KEY + userId, field);
        return value == null ? null : Integer.parseInt(value.toString());
    }

    public void setUserStatsValue(String userId, String field, Integer value) {
        Map<String, Object> statsMap = new HashMap<>();
        statsMap.put(field, value);
        redisUtils.hmset(UserRedisKeys.USER_STATS_KEY + userId,
                statsMap,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public void saveRealtimeUserStatsInfo(String userId, Map<String, Integer> userStatsInfo) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(UserRedisKeys.USER_STATS_KEY + userId,
                statsMap,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public HashMap<String, Integer> getRealtimeUserStatsInfo(String userId) {
        return (HashMap<String, Integer>) redisUtils.hmget(UserRedisKeys.USER_STATS_KEY + userId);
    }

    public void refreshRealtimeUserStatsExpire(String userId) {
        redisUtils.expire(UserRedisKeys.USER_STATS_KEY + userId,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_USER_STATS_CACHE_TTL_DAYS);
    }

    public void saveUserStatsSnapshot(String userId, String statsDay, Map<String, Integer> userStatsInfo) {
        Map<String, Object> statsMap = new HashMap<>(userStatsInfo);
        redisUtils.hmset(UserRedisKeys.USER_STATS_SNAPSHOT_KEY + statsDay + ":" + userId,
                statsMap,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.LENGTH_2);
    }

    public HashMap<String, Integer> getUserStatsSnapshot(String userId, String statsDay) {
        return (HashMap<String, Integer>) redisUtils.hmget(UserRedisKeys.USER_STATS_SNAPSHOT_KEY + statsDay + ":" + userId);
    }

    public Set<String> getUserStatsSnapshotKeys(String statsDay) {
        return redisUtils.getByKeyPrefix(UserRedisKeys.USER_STATS_SNAPSHOT_KEY + statsDay + ":");
    }

    public SysSettingDTO getSysSetting() {
        Object sysSetting = redisUtils.get(UserRedisKeys.SYS_SETTING_KEY);
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
            // user 不再拥有 sys_setting 表，admin 暂不可用时用默认注册金币配置兜底。
            return SysSettingDTO.createDefault();
        }
        if (sysSettingDTO == null) {
            return SysSettingDTO.createDefault();
        }
        redisUtils.set(UserRedisKeys.SYS_SETTING_KEY, sysSettingDTO);
        return sysSettingDTO;
    }
}
