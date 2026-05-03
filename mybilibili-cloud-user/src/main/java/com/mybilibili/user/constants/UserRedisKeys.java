package com.mybilibili.user.constants;

import com.mybilibili.base.constants.Constants;

/**
 * user 服务 Redis key。
 */
public final class UserRedisKeys {

    private UserRedisKeys() {
    }

    public static final String USER_INFO_KEY = Constants.REDIS_PREFIX + "web:userInfo:showVO:";
    public static final String USER_STATS_KEY = Constants.REDIS_PREFIX + "web:userInfo:stats:";
    public static final String USER_STATS_SNAPSHOT_KEY = Constants.REDIS_PREFIX + "web:userInfo:stats:snapshot:";
    public static final String SYS_SETTING_KEY = Constants.REDIS_PREFIX + "sysSetting:";
}
