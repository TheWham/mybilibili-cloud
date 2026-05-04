package com.mybilibili.video.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.VideoPlayEvent;
import com.mybilibili.video.services.VideoPlayEventService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 视频播放事件消费者。
 *
 * <p>消费者只负责消息接收和基础校验，具体业务处理交给 service。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Component
public class VideoPlayEventConsumer {

    @Resource
    private VideoPlayEventService videoPlayEventService;

    @RabbitListener(queues = MqConstants.VIDEO_PLAY_QUEUE)
    public void consumeVideoPlayEvent(VideoPlayEvent videoPlayEvent) {
        if (videoPlayEvent == null
                || videoPlayEvent.getVideoId() == null
                || videoPlayEvent.getUserId() == null) {
            return;
        }
        videoPlayEventService.handleVideoPlayEvent(videoPlayEvent);
    }
}
