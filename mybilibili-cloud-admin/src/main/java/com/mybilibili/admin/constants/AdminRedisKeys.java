package com.mybilibili.admin.constants;

import com.mybilibili.base.constants.Constants;

/**
 * admin 服务 Redis key。
 */
public final class AdminRedisKeys {

    private AdminRedisKeys() {
    }

    public static final String SYS_SETTING_KEY = Constants.REDIS_PREFIX + "sysSetting:";
    public static final String CATEGORY_KEY = Constants.REDIS_PREFIX + "admin:category:list:";
    public static final String CHECK_CODE_KEY = Constants.REDIS_PREFIX + "admin:checkCodeKey:";
}
