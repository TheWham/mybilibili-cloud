package com.mybilibili.interact.mq.producer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.base.entity.event.VideoDanmuPostEvent;
import com.mybilibili.base.enums.UserActionTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 弹幕事件生产者。
 */
@Component
public class VideoDanmuEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendDanmuPostEvent(VideoDanmuPostEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.DANMU_EXCHANGE,
                MqConstants.DANMU_PERSIST_ROUTING_KEY,
                event);
    }

    public void sendDanmuVideoCountEvent(VideoDanmuPostEvent event) {
        rabbitTemplate.convertAndSend(MqConstants.USER_ACTION_EXCHANGE,
                MqConstants.VIDEO_ACTION_COUNT_ROUTING_KEY,
                buildDanmuVideoCountEvent(event));
    }

    public void sendDanmuVideoCountEvent(String eventId,
                                         String userId,
                                         String videoId,
                                         String videoUserId,
                                         int actionCount,
                                         Date actionTime) {
        UserActionSyncEvent countEvent = new UserActionSyncEvent();
        countEvent.setEventId(eventId);
        countEvent.setUserId(userId);
        countEvent.setVideoId(videoId);
        countEvent.setVideoUserId(videoUserId);
        countEvent.setActionType(UserActionTypeEnum.VIDEO_DNAMU.getType());
        countEvent.setActionCount(actionCount);
        countEvent.setActive(Boolean.TRUE);
        countEvent.setActionTime(actionTime);
        rabbitTemplate.convertAndSend(MqConstants.USER_ACTION_EXCHANGE,
                MqConstants.VIDEO_ACTION_COUNT_ROUTING_KEY,
                countEvent);
    }

    private UserActionSyncEvent buildDanmuVideoCountEvent(VideoDanmuPostEvent event) {
        UserActionSyncEvent countEvent = new UserActionSyncEvent();
        countEvent.setEventId(event.getEventId());
        countEvent.setUserId(event.getUserId());
        countEvent.setVideoId(event.getVideoId());
        countEvent.setVideoUserId(event.getVideoUserId());
        countEvent.setActionType(UserActionTypeEnum.VIDEO_DNAMU.getType());
        countEvent.setActionCount(Constants.ONE);
        countEvent.setActive(Boolean.TRUE);
        countEvent.setActionTime(event.getPostTime());
        return countEvent;
    }
}
