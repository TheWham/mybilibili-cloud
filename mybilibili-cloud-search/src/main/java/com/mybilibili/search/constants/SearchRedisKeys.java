package com.mybilibili.search.constants;

import com.mybilibili.base.constants.Constants;

/**
 * search 服务 Redis key。
 */
public final class SearchRedisKeys {

    private SearchRedisKeys() {
    }

    public static final String VIDEO_SEARCH_COUNT = Constants.REDIS_PREFIX + "video:search:";
}
