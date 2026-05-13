package com.mybilibili.video.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.video.services.VideoActionCountSyncService;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 视频互动计数消费者。
 *
 * @author amani
 * @since 2026/05/11
 */
@Component
public class VideoActionCountConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoActionCountConsumer.class);

    @Resource
    private VideoActionCountSyncService videoActionCountSyncService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.VIDEO_ACTION_COUNT_QUEUE)
    public void consumeVideoActionCountEvent(UserActionSyncEvent event) {
        if (event == null
                || !StringUtils.hasText(event.getEventId())
                || event.getVideoId() == null
                || event.getActionType() == null
                || event.getActionCount() == null) {
            log.warn("视频互动计数消息参数不完整，event:{}", event);
            return;
        }
        if (!mqIdempotentComponent.tryStart(MqConstants.VIDEO_ACTION_COUNT_QUEUE, event.getEventId())) {
            return;
        }
        try {
            videoActionCountSyncService.syncVideoActionCount(event);
            mqIdempotentComponent.markDone(MqConstants.VIDEO_ACTION_COUNT_QUEUE, event.getEventId());
        } catch (RuntimeException e) {
            mqIdempotentComponent.release(MqConstants.VIDEO_ACTION_COUNT_QUEUE, event.getEventId());
            throw e;
        }
    }
}
