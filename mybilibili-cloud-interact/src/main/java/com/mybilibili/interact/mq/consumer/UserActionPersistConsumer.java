package com.mybilibili.interact.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.interact.services.UserActionPersistService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户行为落库消费者。
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class UserActionPersistConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserActionPersistConsumer.class);

    @Resource
    private UserActionPersistService userActionPersistService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.USER_ACTION_PERSIST_QUEUE)
    public void consumeUserActionPersistEvent(UserActionSyncEvent event) {
        if (event == null
                || !StringUtils.hasText(event.getEventId())
                || event.getUserId() == null
                || event.getVideoId() == null
                || event.getActionType() == null) {
            log.warn("用户行为落库消息参数不完整，event:{}", event);
            return;
        }
        if (!mqIdempotentComponent.tryStart(MqConstants.USER_ACTION_PERSIST_QUEUE, event.getEventId())) {
            return;
        }
        try {
            userActionPersistService.syncUserVideoAction(event);
            mqIdempotentComponent.markDone(MqConstants.USER_ACTION_PERSIST_QUEUE, event.getEventId());
        } catch (RuntimeException e) {
            mqIdempotentComponent.release(MqConstants.USER_ACTION_PERSIST_QUEUE, event.getEventId());
            throw e;
        }
    }
}
