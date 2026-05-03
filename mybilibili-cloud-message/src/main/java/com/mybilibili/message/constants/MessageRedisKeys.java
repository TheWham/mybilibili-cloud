package com.mybilibili.message.constants;

import com.mybilibili.base.constants.Constants;

/**
 * message 服务 Redis key。
 */
public final class MessageRedisKeys {

    private MessageRedisKeys() {
    }

    public static final String USER_MESSAGE_QUEUE = Constants.REDIS_PREFIX + "queue:user:message:list:";
}
