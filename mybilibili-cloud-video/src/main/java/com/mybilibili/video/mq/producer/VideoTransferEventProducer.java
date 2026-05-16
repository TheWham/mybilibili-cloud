package com.mybilibili.video.mq.producer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.VideoTransferEvent;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 视频转码事件生产者。
 *
 * <p>投稿事务提交后再投递消息，避免数据库回滚但任务已经进入队列的情况。</p>
 *
 * @author amani
 * @since 2026/05/15
 */
@Component
public class VideoTransferEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送单个分 P 的转码任务。
     *
     * @param event 转码事件
     */
    public void sendTransferEvent(VideoTransferEvent event) {
        if (event == null) {
            return;
        }
        rabbitTemplate.convertAndSend(MqConstants.VIDEO_TRANSFER_EXCHANGE,
                MqConstants.VIDEO_TRANSFER_ROUTING_KEY,
                event);
    }
}
