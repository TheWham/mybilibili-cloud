package com.mybilibili.user.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserCoinSyncEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.user.services.UserCoinSyncService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户硬币同步消费者。
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class UserCoinSyncConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserCoinSyncConsumer.class);

    @Resource
    private UserCoinSyncService userCoinSyncService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.USER_COIN_SYNC_QUEUE)
    public void consumeUserCoinSyncEvent(UserCoinSyncEvent event) {
        if (event == null
                || !StringUtils.hasText(event.getEventId())
                || event.getVideoUserId() == null
                || event.getActionCount() == null) {
            log.warn("用户硬币同步消息参数不完整，event:{}", event);
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
