package com.mybilibili.search.component;

import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.search.constants.SearchRedisKeys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Integer> getVideoActionCountDelta(String videoId) {
        Map<Object, Object> valueMap = redisUtils.hmget(SearchRedisKeys.VIDEO_ACTION_COUNT_DELTA + videoId);
        if (valueMap == null || valueMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> resultMap = new HashMap<>(valueMap.size());
        for (Map.Entry<Object, Object> entry : valueMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            resultMap.put(entry.getKey().toString(), Integer.parseInt(entry.getValue().toString()));
        }
        return resultMap;
    }
}
