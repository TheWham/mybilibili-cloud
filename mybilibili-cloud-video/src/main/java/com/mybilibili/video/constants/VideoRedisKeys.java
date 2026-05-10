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
    public static final String VIDEO_ACTION_COUNT_DELTA = Constants.REDIS_PREFIX + "video:action:delta:";
    public static final String VIDEO_HISTORY_DELETE_QUEUE = Constants.REDIS_PREFIX + "queue:video:history:delete:list:";
    public static final String DEL_FILE_QUEUE = Constants.REDIS_PREFIX + "queue:del:file:list:";
    public static final String TRANSFER_FILE_QUEUE = Constants.REDIS_PREFIX + "queue:transfer:file:list:";
    public static final String VIDEO_AUDIT_REWARD_QUEUE = Constants.REDIS_PREFIX + "queue:action:video:audit:reward:list:";
    public static final String AI_SUBTITLE_INDEX_QUEUE = Constants.REDIS_AI_SUBTITLE_VECTOR_QUEUE_KEY;
    public static final String UPLOADING_FILE_INFO_KEY = Constants.REDIS_PREFIX + "uploadFileInfo:";
    public static final String SYS_SETTING_KEY = Constants.REDIS_PREFIX + "sysSetting:";
    public static final String USER_STATS_KEY = Constants.REDIS_PREFIX + "web:userInfo:stats:";
}

