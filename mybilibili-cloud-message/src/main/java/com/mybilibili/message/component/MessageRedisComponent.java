package com.mybilibili.message.component;

import com.mybilibili.base.entity.po.UserMessage;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.message.constants.MessageRedisKeys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * message 服务消息队列缓存。
 */
@Component
public class MessageRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public void addUserMessageQueue(UserMessage userMessage) {
        redisUtils.lpush(MessageRedisKeys.USER_MESSAGE_QUEUE, userMessage, 0L);
    }

    public UserMessage getNextUserMessageQueue() {
        return (UserMessage) redisUtils.rpop(MessageRedisKeys.USER_MESSAGE_QUEUE);
    }
}
