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

import java.util.ArrayList;
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

    /**
     * 批量消费站内信事件并落库。
     *
     * <p>这里仍然按 eventId 逐条做幂等锁，批量只用于减少监听回调和数据库写入次数。
     * 如果整批落库失败，已经抢到锁的消息会释放 processing 标记，交给 RabbitMQ 后续重试。</p>
     *
     * @param eventList RabbitMQ 本次投递给消费者的一批站内信事件
     */
    @RabbitListener(queues = MqConstants.USER_MESSAGE_PERSIST_QUEUE,
            containerFactory = "userMessageBatchRabbitListenerContainerFactory")
    public void consumeUserMessageEvents(List<UserMessageEvent> eventList) {
        if (eventList == null || eventList.isEmpty()) {
            return;
        }

        List<UserMessage> messageList = new ArrayList<>(eventList.size());
        List<String> processingEventIdList = new ArrayList<>(eventList.size());
        try {
            for (UserMessageEvent event : eventList) {
                if (!validMessageEvent(event)) {
                    log.warn("用户站内信消息参数不完整，event:{}", event);
                    continue;
                }
                if (!mqIdempotentComponent.tryStart(MqConstants.USER_MESSAGE_PERSIST_QUEUE, event.getEventId())) {
                    continue;
                }
                processingEventIdList.add(event.getEventId());
                messageList.add(buildUserMessage(event));
            }

            if (!messageList.isEmpty()) {
                userMessageService.addBatch(messageList);
            }
            for (String eventId : processingEventIdList) {
                mqIdempotentComponent.markDone(MqConstants.USER_MESSAGE_PERSIST_QUEUE, eventId);
            }
        } catch (RuntimeException e) {
            for (String eventId : processingEventIdList) {
                mqIdempotentComponent.release(MqConstants.USER_MESSAGE_PERSIST_QUEUE, eventId);
            }
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
