package com.mybilibili.media.constants;

import com.mybilibili.base.constants.Constants;

/**
 * media 服务 Redis key。
 */
public final class MediaRedisKeys {

    private MediaRedisKeys() {
    }

    public static final String UPLOADING_FILE_INFO_KEY = Constants.REDIS_PREFIX + "uploadFileInfo:";
    public static final String DEL_FILE_QUEUE = Constants.REDIS_PREFIX + "queue:del:file:list:";
    public static final String TRANSFER_FILE_QUEUE = Constants.REDIS_PREFIX + "queue:transfer:file:list:";
}
