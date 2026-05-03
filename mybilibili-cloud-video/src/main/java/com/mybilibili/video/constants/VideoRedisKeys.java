package com.mybilibili.video.constants;

import com.mybilibili.base.constants.Constants;

/**
 * video 服务 Redis key。
 */
public final class VideoRedisKeys {

    private VideoRedisKeys() {
    }

    public static final String CATEGORY_KEY = Constants.REDIS_PREFIX + "admin:category:list:";
    public static final String VIDEO_PLAY_COUNT_ONLINE_PREFIX = Constants.REDIS_PREFIX + "video:play:online:";
    public static final String VIDEO_PLAY_COUNT_ONLINE = VIDEO_PLAY_COUNT_ONLINE_PREFIX + "count:%s";
    public static final String VIDEO_PLAY_COUNT_USER = VIDEO_PLAY_COUNT_ONLINE_PREFIX + "user:%s:%s";
    public static final String VIDEO_PLAY_HISTORY = Constants.REDIS_PREFIX + "video:history:play:";
    public static final String VIDEO_PLAY_HISTORY_FILE_INDEX = Constants.REDIS_PREFIX + "video:history:fileIndex:";
    public static final String DIRTY_HISTORY_USER = Constants.REDIS_PREFIX + "video:history:user:";
    public static final String VIDEO_PLAY_COUNT = Constants.REDIS_PREFIX + "video:play:uv:";
    public static final String VIDEO_PLAY_EFFECTIVE = Constants.REDIS_PREFIX + "video:play:effective:";
    public static final String VIDEO_PLAY_COUNT_DELTA = Constants.REDIS_PREFIX + "video:play:delta";
    public static final String VIDEO_HISTORY_DELETE_QUEUE = Constants.REDIS_PREFIX + "queue:video:history:delete:list:";
}
