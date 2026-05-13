package com.mybilibili.interact.mq.producer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.VideoDanmuPostEvent;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

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
}
