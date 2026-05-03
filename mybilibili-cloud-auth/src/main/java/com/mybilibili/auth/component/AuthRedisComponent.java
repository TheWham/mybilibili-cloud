package com.mybilibili.auth.component;

import com.mybilibili.auth.constants.AuthRedisKeys;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * auth 服务自己的 Redis 操作。
 *
 * <p>验证码只在登录注册链路内使用，不应该继续放在 common 的大组件里。</p>
 */
@Component
public class AuthRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public String saveCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        long expireTime = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE * Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT;
        redisUtils.setex(AuthRedisKeys.CHECK_CODE_KEY + checkCodeKey, code, expireTime);
        return checkCodeKey;
    }

    public String getCode(String checkCodeKey) {
        Object value = redisUtils.get(AuthRedisKeys.CHECK_CODE_KEY + checkCodeKey);
        if (Objects.isNull(value)) {
            throw new BusinessException("验证码失效");
        }
        return value.toString();
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(AuthRedisKeys.CHECK_CODE_KEY + checkCodeKey);
    }
}
