package com.mybilibili.base.constants;


/**
 * @version 2026.1.6
 * @author Amani
 */
public class Constants {


    private Constants() {
    }

    /**
     * 基础数字常量。
     */
    public static final Integer ZERO = 0;
    public static final Integer ONE = 1;
    public static final Integer TWO = 2;
    public static final Integer LENGTH_2 = 2;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_20 = 20;
    public static final Integer LENGTH_30 = 30;
    public static final Integer LENGTH_90 = 90;
    public static final Integer LENGTH_1000 = 1000;
    public static final Integer HOUR_24 = 24;
    public static final Integer USER_ID_LENGTH = 10;
    public static final Integer DEFAULT_COIN_COUNT = 100;
    public static final Integer UPDATE_NAME_COIN = 3;
    public static final Long MB_SIZE = 1024 * 1024L;

    /**
     * 时间相关常量，统一按毫秒处理。
     */
    public static final Integer REDIS_EXPIRE_TIME_ONE_SECOND = 1000;
    public static final Integer REDIS_EXPIRE_TIME_ONE_MINUTE = 60000;
    public static final Integer REDIS_EXPIRE_TIME_ONE_DAY = REDIS_EXPIRE_TIME_ONE_MINUTE * 60 * 24;
    public static final Integer REDIS_EXPIRE_TIME_TWO_DAY = REDIS_EXPIRE_TIME_ONE_MINUTE * 60 * 24 * 2;
    public static final Integer REDIS_EXPIRE_TIME_MINUTE_COUNT = 5;
    public static final Integer REDIS_EXPIRE_TIME_DAY_COUNT = 7;
    public static final Integer WEB_EXPIRE_TIME_DAY_COUNT = 7;
    public static final Integer REDIS_USER_STATS_CACHE_TTL_DAYS = 15;
    public static final Integer REDIS_VIDEO_PLAY_EFFECTIVE_EXPIRE_MINUTES = 30;
    public static final Integer REDIS_ACTION_STATUS_CACHE_TTL_MINUTES = 10;
    public static final Integer REDIS_VIDEO_DANMU_CACHE_TTL_MINUTES = 5;
    // 阻塞等待时间要小于 Redis 客户端超时时间，避免空队列时被客户端当成超时异常。
    public static final Integer REDIS_QUEUE_BLOCK_SECONDS = 3;

    /**
     * 通用配置。
     */
    public static final String REDIS_PREFIX = "easylive:";
    public static final String PASSWORD_REGEX = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * 登录态和验证码相关。
     */
    public static final String WEB_TOKEN_KEY = "token";
    public static final String ADMIN_TOKEN_KEY = "adminToken";
    public static final Integer WEB_TOKEN_EXPIRE_TIME = REDIS_EXPIRE_TIME_ONE_DAY / 1000;
    public static final Integer ADMIN_TOKEN_EXPIRE_TIME = REDIS_EXPIRE_TIME_ONE_DAY / 1000;
    public static final String REDIS_CHECK_CODE_KEY = REDIS_PREFIX + "checkCodeKey:";
    public static final String REDIS_WEB_TOKEN_KEY = REDIS_PREFIX + "web:redisToken:";
    public static final String REDIS_ADMIN_TOKEN_KEY = REDIS_PREFIX + "admin:redisToken:";
    public static final String REDIS_USER_TOKEN_KEY = REDIS_PREFIX + "web:redisUserToken:";

    /**
     * 系统配置和分类缓存。
     */
    public static final String REDIS_SYS_SETTING_KEY = REDIS_PREFIX + "sysSetting:";
    public static final String REDIS_ADMIN_CATEGORY_KEY = REDIS_PREFIX + "admin:category:list:";
    public static final String REDIS_KEY_USER_DAILY_LIMIT = REDIS_PREFIX + "user:daily:limit:";
    public static final String AUDIT_VIDEO_NAME = "auditVideo";
    /**
     * 文件和切片目录相关。
     */
    public static final String FILE_PATH_FOLDER = "file/";
    public static final String FILE_PATH_FOLDER_VIDEO = "video/";
    public static final String FILE_PATH_FOLDER_TEMP = "temp/";
    public static final String FILE_PATH_FOLDER_COVER = "cover/";
    public static final String FILE_TEMP_MP4 = "/temp.mp4";
    public static final String FILE_VIDEO_TEMP_SUFFIX = "_temp";
    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail";
    public static final String VIDEO_CODEC_HEVC = "hevc";
    public static final String TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";

    /**
     * 上传、转码和删文件相关队列。
     */
    public static final String REDIS_WEB_UPLOADING_FILE_INFO_KEY = REDIS_PREFIX + "uploadFileInfo:";
    public static final String REDIS_WEB_ADD_DEL_QUEUE_KEY = REDIS_PREFIX + "queue:del:file:list:";
    public static final String REDIS_WEB_ADD_TRANSFER_QUEUE_KEY = REDIS_PREFIX + "queue:transfer:file:list:";

    /**
     * 用户动作和通知异步队列。
     */
    public static final String REDIS_WEB_ACTION_VIDEO_COMMENT_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:comment:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_COIN_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:coin:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_AUDIT_REWARD_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:audit:reward:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_COLLECT_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:collect:list:";
    public static final String REDIS_WEB_ACTION_VIDEO_LIKE_QUEUE_KEY = REDIS_PREFIX + "queue:action:video:like:list:";
    public static final String REDIS_WEB_USER_MESSAGE_QUEUE_KEY = REDIS_PREFIX + "queue:user:message:list:";
    public static final String REDIS_WEB_VIDEO_HISTORY_DELETE_QUEUE_KEY = REDIS_PREFIX + "queue:video:history:delete:list:";

    /**
     * 用户动作状态缓存，用来做幂等和取消判断。
     */
    public static final String REDIS_WEB_ACTION_VIDEO_STATUS_KEY = REDIS_PREFIX + "action:video:status:";
    public static final String REDIS_WEB_ACTION_COMMENT_STATUS_KEY = REDIS_PREFIX + "action:comment:status:";

    /**
     * 视频播放链路相关。
     */
    public static final String REDIS_WEB_VIDEO_PLAY_QUEUE_KEY = REDIS_PREFIX + "queue:video:play:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX = REDIS_PREFIX + "video:play:online:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX + "count:%s";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX = "user:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREIFX + REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX + "%s:%s";
    public static final String REDIS_KEY_VIDEO_PLAY_HISTORY = REDIS_PREFIX + "video:history:play:";
    public static final String REDIS_KEY_VIDEO_PLAY_HISTORY_FILE_INDEX = REDIS_PREFIX + "video:history:fileIndex:";
    public static final String REDIS_KEY_DIRTY_HISTORY_USER = REDIS_PREFIX + "video:history:user:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT = REDIS_PREFIX + "video:play:uv:";
    public static final String REDIS_KEY_VIDEO_PLAY_EFFECTIVE = REDIS_PREFIX + "video:play:effective:";
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_DELTA = REDIS_PREFIX + "video:play:delta";
    public static final String REDIS_KEY_VIDEO_ACTION_COUNT_DELTA = REDIS_PREFIX + "video:action:delta:";
    public static final String REDIS_KEY_VIDEO_INFO_CACHE = REDIS_PREFIX + "video:info:";
    public static final String REDIS_KEY_VIDEO_DANMU_STATUS_CACHE = REDIS_PREFIX + "video:danmu:status:";

    /**
     * 用户展示信息和实时统计缓存。
     */
    public static final String REDIS_WEB_USER_INFO_KEY = REDIS_PREFIX + "web:userInfo:showVO:";
    public static final String REDIS_WEB_USER_STATS_KEY = REDIS_PREFIX + "web:userInfo:stats:";
    public static final String REDIS_WEB_USER_STATS_SNAPSHOT_KEY = REDIS_PREFIX + "web:userInfo:stats:snapshot:";

    /**
     * 搜索热词统计。
     */
    public static final String REDIS_KEY_VIDEO_SEARCH_COUNT = REDIS_PREFIX + "video:search:";

    /**
     * AI 字幕向量化任务队列，video 生产，Python worker 消费。
     */
    public static final String REDIS_AI_SUBTITLE_VECTOR_QUEUE_KEY = REDIS_PREFIX + "queue:ai:subtitle-vector";

    /**
     * AI 字幕向量化死信队列，worker 多次处理失败后写入，方便人工重投。
     */
    public static final String REDIS_AI_SUBTITLE_VECTOR_DEAD_QUEUE_KEY = REDIS_PREFIX + "queue:ai:subtitle-vector:dead";

    /**
     * AI 字幕向量化任务状态前缀，后面拼接 videoId。
     */
    public static final String REDIS_AI_SUBTITLE_VECTOR_STATUS_KEY_PREFIX = REDIS_PREFIX + "ai:subtitle-vector:status:";

    /**
     * Redis Lua 脚本资源路径。
     */
    public static final String REDIS_LUA_VIDEO_COIN = "lua/video_coin.lua";
    public static final String REDIS_LUA_VIDEO_TOGGLE_ACTION = "lua/video_toggle_action.lua";
    public static final String REDIS_LUA_USER_DAILY_LIMIT = "lua/user_daily_limit.lua";

    /**
     * 微服务
     */

    public static final String INNER_API_PREFIX = "/innerApi";
    public static final String CLOUD_ADMIN_NAME = "mybilibili-cloud-admin";
    public static final String CLOUD_VIDEO_NAME = "mybilibili-cloud-video";
    public static final String CLOUD_AUTH_NAME = "mybilibili-cloud-auth";
    public static final String CLOUD_INTERACT_NAME = "mybilibili-cloud-interact";
    public static final String CLOUD_MEDIA_NAME = "mybilibili-cloud-media";
    public static final String CLOUD_MESSAGE_NAME = "mybilibili-cloud-message";
    public static final String CLOUD_USER_NAME = "mybilibili-cloud-user";
    public static final String CLOUD_SEARCH_NAME = "mybilibili-cloud-search";
    public static final String CLOUD_AI_NAME = "mybilibili-cloud-ai";
}

