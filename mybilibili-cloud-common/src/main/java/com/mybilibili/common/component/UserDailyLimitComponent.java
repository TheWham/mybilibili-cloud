package com.mybilibili.common.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.enums.UserDailyLimitTypeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.consumer.AdminSysSettingClient;
import com.mybilibili.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * 统一维护用户每日行为限制。
 *
 * <p>这里属于跨业务的轻量基础能力，只读取系统配置和写 Redis 计数。
 * 真正的业务动作仍由各业务服务自己决定。</p>
 */
@Component
public class UserDailyLimitComponent {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AdminSysSettingClient adminSysSettingClient;

    public void checkDailyLimit(String userId, UserDailyLimitTypeEnum limitType) {
        Integer limitCount = getLimitCount(limitType);
        if (limitCount == null || limitCount <= 0) {
            return;
        }

        String redisKey = buildDailyLimitKey(userId, limitType);
        Object currentValue = redisUtils.get(redisKey);
        long currentCount = currentValue == null ? 0L : Long.parseLong(currentValue.toString());
        if (currentCount >= limitCount) {
            throw new BusinessException(String.format("%s已达到今日上限", limitType.getDesc()));
        }
    }

    public void recordDailyAction(String userId, UserDailyLimitTypeEnum limitType) {
        Integer limitCount = getLimitCount(limitType);
        if (limitCount == null || limitCount <= 0) {
            return;
        }

        String redisKey = buildDailyLimitKey(userId, limitType);
        long expireMilliseconds = getRemainMillisecondsToday();
        redisUtils.executeLongScript(Constants.REDIS_LUA_USER_DAILY_LIMIT,
                Collections.singletonList(redisKey),
                limitCount,
                expireMilliseconds);
    }

    public void occupyDailyAction(String userId, UserDailyLimitTypeEnum limitType) {
        Integer limitCount = getLimitCount(limitType);
        if (limitCount == null || limitCount <= 0) {
            return;
        }

        String redisKey = buildDailyLimitKey(userId, limitType);
        long expireMilliseconds = getRemainMillisecondsToday();
        Long result = redisUtils.executeLongScript(Constants.REDIS_LUA_USER_DAILY_LIMIT,
                Collections.singletonList(redisKey),
                limitCount,
                expireMilliseconds);
        if (result == null || result <= 0) {
            throw new BusinessException(String.format("%s已达到今日上限", limitType.getDesc()));
        }
    }

    public void releaseDailyAction(String userId, UserDailyLimitTypeEnum limitType) {
        Integer limitCount = getLimitCount(limitType);
        if (limitCount == null || limitCount <= 0) {
            return;
        }

        redisUtils.decrement(buildDailyLimitKey(userId, limitType));
    }

    private Integer getLimitCount(UserDailyLimitTypeEnum limitType) {
        SysSettingDTO sysSettingDTO;
        try {
            sysSettingDTO = adminSysSettingClient.getSysSetting();
        } catch (Exception e) {
            // 限制配置读取失败时不阻断主链路，按默认配置继续校验。
            sysSettingDTO = SysSettingDTO.createDefault();
        }
        if (sysSettingDTO == null) {
            sysSettingDTO = SysSettingDTO.createDefault();
        }
        return limitType.resolveLimit(sysSettingDTO);
    }

    private String buildDailyLimitKey(String userId, UserDailyLimitTypeEnum limitType) {
        String today = LocalDate.now().format(DAY_FORMATTER);
        return Constants.REDIS_KEY_USER_DAILY_LIMIT + limitType.getKeySuffix() + ":" + userId + ":" + today;
    }

    private long getRemainMillisecondsToday() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        return Duration.between(now, tomorrowStart).toMillis() + Constants.REDIS_EXPIRE_TIME_ONE_SECOND;
    }
}
