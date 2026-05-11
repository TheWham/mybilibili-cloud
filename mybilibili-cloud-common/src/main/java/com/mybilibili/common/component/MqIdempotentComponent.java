package com.mybilibili.common.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.common.redis.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * MQ 消费幂等工具。
 *
 * <p>RabbitMQ 在消费者异常、连接中断时可能重投消息。互动计数和硬币数都是累加更新，
 * 同一条消息重复消费会把数据加错，所以这里用 Redis 记录“某个队列已经处理过的事件”。</p>
 *
 * @author amani
 * @since 2026/05/12
 */
@Component
public class MqIdempotentComponent {

    private static final String DONE_KEY_PREFIX = Constants.REDIS_PREFIX + "mq:done:";
    private static final long DONE_EXPIRE_TIME = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT;

    @Resource
    private RedisUtils<Object> redisUtils;

    public boolean tryStart(String queueName, String eventId) {
        if (!StringUtils.hasText(queueName) || !StringUtils.hasText(eventId)) {
            return true;
        }
        return !redisUtils.keyExists(buildDoneKey(queueName, eventId));
    }

    public void markDone(String queueName, String eventId) {
        if (!StringUtils.hasText(queueName) || !StringUtils.hasText(eventId)) {
            return;
        }
        redisUtils.setex(buildDoneKey(queueName, eventId), Constants.ONE, DONE_EXPIRE_TIME);
    }

    public void release(String queueName, String eventId) {
        // done 标记只在业务成功后写入，失败时没有额外状态需要清理。
    }

    private String buildDoneKey(String queueName, String eventId) {
        return DONE_KEY_PREFIX + queueName + ":" + eventId;
    }

}
