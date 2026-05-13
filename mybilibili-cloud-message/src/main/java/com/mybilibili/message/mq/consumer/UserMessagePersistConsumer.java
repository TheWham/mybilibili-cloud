package com.mybilibili.message.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserMessageEvent;
import com.mybilibili.base.enums.MessageTypeEnum;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.message.entity.po.UserMessage;
import com.mybilibili.message.enums.MessageReadTypeEnum;
import com.mybilibili.message.services.UserMessageService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 用户站内信落库消费者。
 *
 * <p>消费端只做幂等和落库，通知是否应该产生由事件生产方按业务语义决定。</p>
 *
 * @author amani
 * @since 2026/05/13
 */
@Component
public class UserMessagePersistConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserMessagePersistConsumer.class);

    @Resource
    private UserMessageService userMessageService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.USER_MESSAGE_PERSIST_QUEUE)
    public void consumeUserMessageEvent(UserMessageEvent event) {
        if (!validMessageEvent(event)) {
            log.warn("用户站内信消息参数不完整，event:{}", event);
            return;
        }
        if (!mqIdempotentComponent.tryStart(MqConstants.USER_MESSAGE_PERSIST_QUEUE, event.getEventId())) {
            return;
        }

        try {
            userMessageService.addBatch(List.of(buildUserMessage(event)));
            mqIdempotentComponent.markDone(MqConstants.USER_MESSAGE_PERSIST_QUEUE, event.getEventId());
        } catch (RuntimeException e) {
            mqIdempotentComponent.release(MqConstants.USER_MESSAGE_PERSIST_QUEUE, event.getEventId());
            throw e;
        }
    }

    private boolean validMessageEvent(UserMessageEvent event) {
        return event != null
                && StringUtils.hasText(event.getEventId())
                && StringUtils.hasText(event.getReceiveUserId())
                && MessageTypeEnum.getEnum(event.getMessageType()) != null;
    }

    private UserMessage buildUserMessage(UserMessageEvent event) {
        UserMessage userMessage = new UserMessage();
        userMessage.setUserId(event.getReceiveUserId());
        userMessage.setSendUserId(event.getSendUserId());
        userMessage.setVideoId(event.getVideoId());
        userMessage.setMessageType(event.getMessageType());
        userMessage.setReadType(MessageReadTypeEnum.NO_READ.getType());
        userMessage.setCreateTime(event.getCreateTime() == null ? new Date() : event.getCreateTime());
        userMessage.setExtendJson(event.getExtendJson());
        return userMessage;
    }
}
