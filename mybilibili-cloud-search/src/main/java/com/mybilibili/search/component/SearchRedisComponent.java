package com.mybilibili.search.component;

import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.search.constants.SearchRedisKeys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * search 服务热词缓存。
 */
@Component
public class SearchRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public void saveKeyword(String keyword) {
        redisUtils.zaddCount(SearchRedisKeys.VIDEO_SEARCH_COUNT, keyword);
    }

    public List<String> getSearchKeywordTop(Integer top) {
        return redisUtils.getZSetList(SearchRedisKeys.VIDEO_SEARCH_COUNT, top - 1);
    }
}
