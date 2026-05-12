package com.mybilibili.interact.mq.producer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.base.entity.event.UserCoinSyncEvent;
import com.mybilibili.base.enums.UserActionTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 用户互动事件生产者。
 *
 * <p>一次用户互动会影响多张表，但生产者只负责把事件投出去，具体落库由各服务
 * 按自己的数据归属消费处理。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class UserActionEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private UserMessageEventProducer userMessageEventProducer;

    public void sendUserActionEvent(UserActionSyncEvent event) {
        if (!validActionEvent(event)) {
            return;
        }
        rabbitTemplate.convertAndSend(MqConstants.USER_ACTION_EXCHANGE,
                MqConstants.USER_ACTION_PERSIST_ROUTING_KEY,
                event);
        rabbitTemplate.convertAndSend(MqConstants.USER_ACTION_EXCHANGE,
                MqConstants.VIDEO_ACTION_COUNT_ROUTING_KEY,
                event);
        userMessageEventProducer.sendUserActionMessage(event);

        if (UserActionTypeEnum.VIDEO_COIN.getType().equals(event.getActionType())) {
            rabbitTemplate.convertAndSend(MqConstants.USER_ACTION_EXCHANGE,
                    MqConstants.USER_COIN_SYNC_ROUTING_KEY,
                    buildCoinSyncEvent(event));
        }
    }

    private boolean validActionEvent(UserActionSyncEvent event) {
        return event != null
                && event.getUserId() != null
                && event.getVideoId() != null
                && event.getVideoUserId() != null
                && event.getActionType() != null
                && event.getActionCount() != null
                && event.getActionCount() != 0;
    }

    private UserCoinSyncEvent buildCoinSyncEvent(UserActionSyncEvent event) {
        UserCoinSyncEvent coinSyncEvent = new UserCoinSyncEvent();
        coinSyncEvent.setEventId(event.getEventId());
        coinSyncEvent.setUserId(event.getUserId());
        coinSyncEvent.setVideoUserId(event.getVideoUserId());
        coinSyncEvent.setVideoId(event.getVideoId());
        coinSyncEvent.setActionCount(Math.abs(event.getActionCount()));
        coinSyncEvent.setAuditReward(false);
        coinSyncEvent.setActionTime(event.getActionTime());
        return coinSyncEvent;
    }
}
