package com.mybilibili.interact.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.interact.services.UserActionPersistService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 用户行为落库消费者。
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class UserActionPersistConsumer {

    @Resource
    private UserActionPersistService userActionPersistService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.USER_ACTION_PERSIST_QUEUE)
    public void consumeUserActionPersistEvent(UserActionSyncEvent event) {
        if (event == null
                || event.getUserId() == null
                || event.getVideoId() == null
                || event.getActionType() == null) {
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
