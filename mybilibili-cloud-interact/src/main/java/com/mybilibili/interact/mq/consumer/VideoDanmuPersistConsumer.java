package com.mybilibili.interact.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.VideoDanmuPostEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 弹幕批量落库消费者。
 */
@Component
public class VideoDanmuPersistConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoDanmuPersistConsumer.class);

    @Resource
    private VideoDanmuService videoDanmuService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    @RabbitListener(queues = MqConstants.DANMU_PERSIST_QUEUE,
            containerFactory = "danmuBatchRabbitListenerContainerFactory")
    public void consumeDanmuPostEvents(List<VideoDanmuPostEvent> eventList) {
        if (eventList == null || eventList.isEmpty()) {
            return;
        }

        List<VideoDanmu> danmuList = new ArrayList<>();
        List<String> processingEventIdList = new ArrayList<>();
        try {
            for (VideoDanmuPostEvent event : eventList) {
                if (!validDanmuEvent(event)) {
                    log.warn("弹幕落库消息参数不完整，event:{}", event);
                    continue;
                }
                if (!mqIdempotentComponent.tryStart(MqConstants.DANMU_PERSIST_QUEUE, event.getEventId())) {
                    continue;
                }
                processingEventIdList.add(event.getEventId());
                danmuList.add(buildVideoDanmu(event));
            }

            if (!danmuList.isEmpty()) {
                videoDanmuService.addBatch(danmuList);
            }
            for (String eventId : processingEventIdList) {
                mqIdempotentComponent.markDone(MqConstants.DANMU_PERSIST_QUEUE, eventId);
            }
        } catch (RuntimeException e) {
            for (String eventId : processingEventIdList) {
                mqIdempotentComponent.release(MqConstants.DANMU_PERSIST_QUEUE, eventId);
            }
            throw e;
        }
    }

    private boolean validDanmuEvent(VideoDanmuPostEvent event) {
        return event != null
                && StringUtils.hasText(event.getEventId())
                && StringUtils.hasText(event.getUserId())
                && StringUtils.hasText(event.getVideoId())
                && StringUtils.hasText(event.getVideoUserId())
                && StringUtils.hasText(event.getFileId())
                && StringUtils.hasText(event.getText())
                && event.getTime() != null
                && event.getPostTime() != null;
    }

    private VideoDanmu buildVideoDanmu(VideoDanmuPostEvent event) {
        VideoDanmu videoDanmu = new VideoDanmu();
        videoDanmu.setVideoId(event.getVideoId());
        videoDanmu.setVideoUserId(event.getVideoUserId());
        videoDanmu.setFileId(event.getFileId());
        videoDanmu.setUserId(event.getUserId());
        videoDanmu.setPostTime(event.getPostTime());
        videoDanmu.setText(event.getText());
        videoDanmu.setMode(event.getMode());
        videoDanmu.setColor(event.getColor());
        videoDanmu.setTime(event.getTime());
        return videoDanmu;
    }
}
