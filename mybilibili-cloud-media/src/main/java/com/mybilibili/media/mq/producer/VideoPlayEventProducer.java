package com.mybilibili.media.mq.producer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.VideoPlayEvent;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 视频播放事件生产者。
 *
 * <p>media 服务不直接写 video/interact 的 Redis。它只在资源播放时投递事件，
 * 后续统计、历史和同步任务交给消费端处理。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Component
public class VideoPlayEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 投递视频播放事件。
     *
     * @param videoPlayEvent 播放事件数据
     */
    public void sendVideoPlayEvent(VideoPlayEvent videoPlayEvent) {
        if (videoPlayEvent == null || videoPlayEvent.getVideoId() == null || videoPlayEvent.getUserId() == null) {
            return;
        }
        if (videoPlayEvent.getPlayTime() == null) {
            videoPlayEvent.setPlayTime(new Date());
        }
        rabbitTemplate.convertAndSend(MqConstants.VIDEO_PLAY_EXCHANGE,
                MqConstants.VIDEO_PLAY_ROUTING_KEY,
                videoPlayEvent);
    }
}
