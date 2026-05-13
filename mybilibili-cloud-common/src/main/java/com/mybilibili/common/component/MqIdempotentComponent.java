package com.mybilibili.common.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
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
    private static final String PROCESSING_KEY_PREFIX = Constants.REDIS_PREFIX + "mq:processing:";
    private static final long DONE_EXPIRE_TIME = (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * Constants.REDIS_EXPIRE_TIME_DAY_COUNT;
    private static final long PROCESSING_EXPIRE_TIME = (long) Constants.REDIS_EXPIRE_TIME_ONE_MINUTE
            * Constants.REDIS_EXPIRE_TIME_MINUTE_COUNT;

    @Resource
    private RedisUtils<Object> redisUtils;

    public boolean tryStart(String queueName, String eventId) {
        checkParam(queueName, eventId);
        if (redisUtils.keyExists(buildDoneKey(queueName, eventId))) {
            return false;
        }

        // 抢占处理中标记，避免同一事件在多个消费者实例上并发执行。
        return redisUtils.setIfAbsent(buildProcessingKey(queueName, eventId),
                Constants.ONE,
                PROCESSING_EXPIRE_TIME);
    }

    public void markDone(String queueName, String eventId) {
        checkParam(queueName, eventId);
        boolean success = redisUtils.setex(buildDoneKey(queueName, eventId), Constants.ONE, DONE_EXPIRE_TIME);
        if (!success) {
            // done 标记写失败时不能正常 ACK，否则后续重投会再次执行业务。
            throw new BusinessException(ResponseCodeEnum.CODE_503);
        }
        redisUtils.delete(buildProcessingKey(queueName, eventId));
    }

    public void release(String queueName, String eventId) {
        if (!StringUtils.hasText(queueName) || !StringUtils.hasText(eventId)) {
            return;
        }
        redisUtils.delete(buildProcessingKey(queueName, eventId));
    }

    private String buildDoneKey(String queueName, String eventId) {
        return DONE_KEY_PREFIX + queueName + ":" + eventId;
    }

    private String buildProcessingKey(String queueName, String eventId) {
        return PROCESSING_KEY_PREFIX + queueName + ":" + eventId;
    }

    private void checkParam(String queueName, String eventId) {
        if (!StringUtils.hasText(queueName) || !StringUtils.hasText(eventId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

}
