package com.mybilibili.interact.constants;

import com.mybilibili.base.constants.Constants;

/**
 * interact 服务 Redis key。
 */
public final class InteractRedisKeys {

    private InteractRedisKeys() {
    }

    public static final String USER_STATS_KEY = Constants.REDIS_PREFIX + "web:userInfo:stats:";
    public static final String VIDEO_ACTION_STATUS_KEY = Constants.REDIS_PREFIX + "action:video:status:";
    public static final String COMMENT_ACTION_STATUS_KEY = Constants.REDIS_PREFIX + "action:comment:status:";
    public static final String VIDEO_AUDIT_REWARD_QUEUE = Constants.REDIS_PREFIX + "queue:action:video:audit:reward:list:";
    public static final String VIDEO_ACTION_COUNT_DELTA = Constants.REDIS_PREFIX + "video:action:delta:";
    public static final String VIDEO_DANMU_CACHE = Constants.REDIS_PREFIX + "danmu:video:file:";
    public static final String LUA_VIDEO_COIN = "lua/video_coin.lua";
    public static final String LUA_VIDEO_TOGGLE_ACTION = "lua/video_toggle_action.lua";
}
