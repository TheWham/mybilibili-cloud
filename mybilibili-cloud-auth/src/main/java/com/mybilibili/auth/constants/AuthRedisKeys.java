package com.mybilibili.auth.constants;

import com.mybilibili.base.constants.Constants;

/**
 * auth 服务 Redis key。
 */
public final class AuthRedisKeys {

    private AuthRedisKeys() {
    }

    public static final String CHECK_CODE_KEY = Constants.REDIS_PREFIX + "checkCodeKey:";
}
