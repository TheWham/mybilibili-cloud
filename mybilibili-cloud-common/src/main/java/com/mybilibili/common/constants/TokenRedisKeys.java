package com.mybilibili.common.constants;

import com.mybilibili.base.constants.Constants;

/**
 * 登录态 Redis key。
 *
 * <p>这些 key 被控制器基类和登录切面读取，属于跨服务安全上下文的一部分，
 * 所以暂时放在 common。验证码、用户统计这类业务 key 不再放到这里。</p>
 */
public final class TokenRedisKeys {

    private TokenRedisKeys() {
    }

    public static final String WEB_TOKEN_KEY = Constants.REDIS_PREFIX + "web:redisToken:";
    public static final String ADMIN_TOKEN_KEY = Constants.REDIS_PREFIX + "admin:redisToken:";
    public static final String USER_TOKEN_KEY = Constants.REDIS_PREFIX + "web:redisUserToken:";
}
