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

import java.util.ArrayList;
import java.util.List;

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

    @RabbitListener(queues = MqConstants.VIDEO_ACTION_COUNT_QUEUE,
            containerFactory = "videoActionCountBatchRabbitListenerContainerFactory")
    public void consumeVideoActionCountEvent(List<UserActionSyncEvent> eventList) {
        if (eventList == null || eventList.isEmpty()) {
            return;
        }

        List<UserActionSyncEvent> processingEventList = new ArrayList<>();
        List<String> processingEventIdList = new ArrayList<>();
        for (UserActionSyncEvent event : eventList) {
            if (!validActionCountEvent(event)) {
                log.warn("视频互动计数消息参数不完整，event:{}", event);
                continue;
            }
            if (!mqIdempotentComponent.tryStart(MqConstants.VIDEO_ACTION_COUNT_QUEUE, event.getEventId())) {
                continue;
            }
            processingEventList.add(event);
            processingEventIdList.add(event.getEventId());
        }

        if (processingEventList.isEmpty()) {
            return;
        }

        try {
            videoActionCountSyncService.syncVideoActionCount(processingEventList);
            for (String eventId : processingEventIdList) {
                mqIdempotentComponent.markDone(MqConstants.VIDEO_ACTION_COUNT_QUEUE, eventId);
            }
        } catch (RuntimeException e) {
            for (String eventId : processingEventIdList) {
                mqIdempotentComponent.release(MqConstants.VIDEO_ACTION_COUNT_QUEUE, eventId);
            }
            throw e;
        }
    }

    private boolean validActionCountEvent(UserActionSyncEvent event) {
        return event != null
                && StringUtils.hasText(event.getEventId())
                && event.getVideoId() != null
                && event.getActionType() != null
                && event.getActionCount() != null;
    }
}
