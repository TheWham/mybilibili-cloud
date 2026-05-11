package com.mybilibili.user.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserCoinSyncEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.user.services.UserCoinSyncService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 用户硬币同步消费者。
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class UserCoinSyncConsumer {

    @Resource
    private UserCoinSyncService userCoinSyncService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.USER_COIN_SYNC_QUEUE)
    public void consumeUserCoinSyncEvent(UserCoinSyncEvent event) {
        if (event == null
                || event.getVideoUserId() == null
                || event.getActionCount() == null) {
            return;
        }
        if (!mqIdempotentComponent.tryStart(MqConstants.USER_COIN_SYNC_QUEUE, event.getEventId())) {
            return;
        }
        try {
            userCoinSyncService.syncUserCoin(event);
            mqIdempotentComponent.markDone(MqConstants.USER_COIN_SYNC_QUEUE, event.getEventId());
        } catch (RuntimeException e) {
            mqIdempotentComponent.release(MqConstants.USER_COIN_SYNC_QUEUE, event.getEventId());
            throw e;
        }
    }
}
