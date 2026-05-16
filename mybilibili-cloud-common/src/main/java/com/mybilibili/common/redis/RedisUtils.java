package com.mybilibili.common.redis;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);
    private static final Map<String, DefaultRedisScript<Long>> LUA_SCRIPT_CACHE = new ConcurrentHashMap<>();

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean keyExists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean setex(String key, V value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.MILLISECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value);
            return false;
        }
    }

    public boolean setIfAbsent(String key, V value, long time) {
        try {
            Boolean result;
            if (time > 0) {
                result = redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.MILLISECONDS);
            } else {
                result = redisTemplate.opsForValue().setIfAbsent(key, value);
            }
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            logger.error("设置redisKey:{},value:{}失败", key, value, e);
            return false;
        }
    }

    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.MILLISECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<V> getQueueList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }


    public boolean lpush(String key, V value, Long time) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            if (time != null && time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long remove(String key, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, 1, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean lpushAll(String key, List<V> values, long time) {
        try {
            redisTemplate.opsForList().leftPushAll(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public V rpop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public V brpop(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.opsForList().rightPop(key, timeout, timeUnit);
        } catch (QueryTimeoutException e) {
            // 阻塞队列等待超过客户端超时时间时，外层只需要按“本轮没拿到数据”处理，不刷错误日志。
            return null;
        } catch (Exception e) {
            logger.error("阻塞获取队列数据失败, key: {}", key, e);
            return null;
        }
    }

    public Long increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        return count;
    }

    public Long incrementex(String key, long milliseconds) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count == 1) {
            //设置过期时间1天
            expire(key, milliseconds);
        }
        return count;
    }

    public Long decrement(String key) {
        Long count = redisTemplate.opsForValue().increment(key, -1);
        if (count <= 0) {
            redisTemplate.delete(key);
        }
        logger.info("key:{},减少数量{}", key, count);
        return count;
    }


    public Set<String> getByKeyPrefix(String keyPrifix) {
        Set<String> keyList = redisTemplate.keys(keyPrifix + "*");
        return keyList;
    }


    public Map<String, V> getBatch(String keyPrifix) {
        Set<String> keySet = redisTemplate.keys(keyPrifix + "*");
        List<String> keyList = new ArrayList<>(keySet);
        List<V> keyValueList = redisTemplate.opsForValue().multiGet(keyList);
        Map<String, V> resultMap = keyList.stream().collect(Collectors.toMap(key -> key, value -> keyValueList.get(keyList.indexOf(value))));
        return resultMap;
    }

    public void zaddCount(String key, V v) {
        redisTemplate.opsForZSet().incrementScore(key, v, 1);
    }


    public void zaddCount4VideoHistory(String key, V v, double score)
    {
        redisTemplate.opsForZSet().add(key, v, score);
    }

    /**
     * 写入 ZSet，并按需刷新过期时间。
     *
     * <p>弹幕缓存会用播放时间作为 score，这样读取时天然按视频时间轴排序。</p>
     */
    public Boolean zadd(String key, V value, double score, long time) {
        try {
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            if (time > 0) {
                expire(key, time);
            }
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            logger.error("写入 ZSet 失败, key: {}", key, e);
            return false;
        }
    }

    /**
     * 按 score 正序读取 ZSet。
     *
     * <p>这里不做业务分页，调用方可以按具体场景决定取整段还是取时间窗口。</p>
     */
    public Set<V> zrange(String key, long start, long end) {
        try {
            Set<V> valueSet = redisTemplate.opsForZSet().range(key, start, end);
            return valueSet == null ? Collections.emptySet() : valueSet;
        } catch (Exception e) {
            logger.error("读取 ZSet 失败, key: {}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * 删除单个 ZSet 成员。
     *
     * <p>发送弹幕时如果 MQ 投递失败，需要把刚写进热缓存的成员撤掉。</p>
     */
    public Long zremove(String key, Object value) {
        try {
            return redisTemplate.opsForZSet().remove(key, value);
        } catch (Exception e) {
            logger.error("删除 ZSet 成员失败, key: {}", key, e);
            return 0L;
        }
    }

    public Long zremove(String key, Object... values) {
        try {
            return redisTemplate.opsForZSet().remove(key, values);
        } catch (Exception e) {
            logger.error("删除 ZSet 成员失败, key: {}", key, e);
            return 0L;
        }
    }

    public Long zremRangeByRank(String key, long start, long end) {
        try {
            return redisTemplate.opsForZSet().removeRange(key, start, end);
        } catch (Exception e) {
            logger.error("按排名删除 ZSet 失败, key: {}", key, e);
            return 0L;
        }
    }


    public List<V> getZSetList(String key, Integer count) {
        Set<V> topElements = redisTemplate.opsForZSet().reverseRange(key, 0, count);
        if (topElements == null || topElements.isEmpty()) {
            return Collections.emptyList();
        }
        List<V> list = new ArrayList<>(topElements);
        return list;
    }

    public Set<ZSetOperations.TypedTuple<V>> getZSetWithScores(String key, Integer count) {
        Set<ZSetOperations.TypedTuple<V>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, count);
        return tupleSet == null ? Collections.emptySet() : tupleSet;
    }

    public Set<ZSetOperations.TypedTuple<V>> getZSetWithScoresByRange(String key, long start, long end) {
        // 历史记录分页直接按 ZSet 排名取当前页，避免把整份数据拉回应用层后再手动切片。
        Set<ZSetOperations.TypedTuple<V>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        return tupleSet == null ? Collections.emptySet() : tupleSet;
    }

    public Long getZSetSize(String key) {
        Long size = redisTemplate.opsForZSet().zCard(key);
        return size == null ? 0L : size;
    }

    public Set<V> getSetMembers(String key) {
        Set<V> members = redisTemplate.opsForSet().members(key);
        return members == null ? Collections.emptySet() : members;
    }

    public Long removeSetMember(String key, V value) {
        try {
            return redisTemplate.opsForSet().remove(key, value);
        } catch (Exception e) {
            logger.error("Redis SREM 异常，key: {}, value: {}", key, value, e);
            return 0L;
        }
    }


    /**
     * 获取 Hash 中所有的键值对 (非常适合用来查用户的关注数、粉丝数、硬币数等全部统计信息)
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return key == null ? null : redisTemplate.opsForHash().entries(key);
    }

    public Object hget(String key, String item) {
        return key == null ? null : redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 批量放入 Hash 数据，并设置过期时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(毫秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置 Hash 失败, key: {}", key, e);
            return false;
        }
    }

    /**
     * Hash 递增/递减 (核心方法：用于原子加减粉丝数、硬币数等)
     * @param key 键 (如 easylive:user:stats:1001)
     * @param item 项 (如 fans_count)
     * @param by 要增加几 (传正数加，传负数减)
     * @return 执行后的最新值
     */
    public Long hincr(String key, String item, long by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    public Long hdel(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            logger.error("删除 Hash 字段失败, key: {}", key, e);
            return 0L;
        }
    }

    public Long executeLongScript(String scriptPath, List<String> keys, Object... args) {
        try {
            DefaultRedisScript<Long> redisScript = LUA_SCRIPT_CACHE.computeIfAbsent(scriptPath, this::buildLongRedisScript);
            Long result = redisTemplate.execute(redisScript, keys, args);
            return result == null ? 0L : result;
        } catch (Exception e) {
            logger.error("执行 Redis Lua 脚本失败, scriptPath: {}, keys: {}", scriptPath, keys, e);
            return null;
        }
    }

    /**
     * Lua 参数里只要出现 Hash field 名称，就不能走 JSON 序列化。
     * 否则 "currentCoinCount" 这类字段会被序列化成带引号的字符串，
     * 脚本里的 HGET/HINCRBY 会把它当成另一个 field，最终读到 0。
     *
     * @param scriptPath 脚本路径
     * @param keys Redis key 列表
     * @param args Lua ARGV 参数，统一按字符串序列化
     * @return 脚本返回值
     */
    public Long executeLongScriptWithStringArgs(String scriptPath, List<String> keys, Object... args) {
        try {
            DefaultRedisScript<Long> redisScript = LUA_SCRIPT_CACHE.computeIfAbsent(scriptPath, this::buildLongRedisScript);
            RedisSerializer<String> stringSerializer = RedisSerializer.string();
            Long result = stringRedisTemplate.execute(redisScript, stringSerializer, null, keys, convertArgsToString(args));
            return result == null ? 0L : result;
        } catch (Exception e) {
            logger.error("执行 Redis Lua 脚本失败(字符串参数), scriptPath: {}, keys: {}", scriptPath, keys, e);
            return null;
        }
    }

    private DefaultRedisScript<Long> buildLongRedisScript(String scriptPath) {
        try {
            ClassPathResource resource = new ClassPathResource(scriptPath);
            String scriptText = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(scriptText);
            redisScript.setResultType(Long.class);
            return redisScript;
        } catch (Exception e) {
            throw new IllegalStateException("加载 Lua 脚本失败: " + scriptPath, e);
        }
    }

    private String[] convertArgsToString(Object... args) {
        if (args == null || args.length == 0) {
            return new String[0];
        }
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = args[i] == null ? null : String.valueOf(args[i]);
        }
        return result;
    }

    // ============================ Pipeline (流水线) 相关操作 ============================

    /**
     * 执行 Redis Pipeline (流水线)
     * 批量执行多条命令，极大降低网络延迟
     * @param sessionCallback 回调接口，里面写具体的命令集
     * @return 执行结果列表
     */
    public List<Object> executePipelined(org.springframework.data.redis.core.SessionCallback<?> sessionCallback) {
        try {
            return redisTemplate.executePipelined(sessionCallback);
        } catch (Exception e) {
            logger.error("Redis Pipeline 执行失败", e);
            return Collections.emptyList();
        }
    }

    public Long saveVideoPlayCount2HLL(String key, V userId, int ttl) {
        Long addCount = redisTemplate.opsForHyperLogLog().add(key, userId);
        expire(key, ttl);
        return addCount == null ? 0L : addCount;
    }

    public void zaddVideoId(String key, V videoId) {
        try {
            // opsForSet() 专门处理集合类型
            redisTemplate.opsForSet().add(key, videoId);
        } catch (Exception e) {
            logger.error("Redis SADD 异常，key: {}, values: {}", key, videoId, e);
        }
    }

    public Long zaddUserId(String key, V values) {
        try {
            // opsForSet() 专门处理集合类型
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            logger.error("Redis SADD 异常，key: {}, values: {}", key, values, e);
            return 0L;
        }
    }
}
