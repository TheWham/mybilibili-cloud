package com.mybilibili.video.mq.consumer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.VideoTransferEvent;
import com.mybilibili.common.component.MqIdempotentComponent;
import com.mybilibili.video.services.VideoInfoFilePostService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 视频转码消息消费者。
 *
 * <p>这里不直接做重业务，先校验消息和幂等，再交给 service 处理具体转码逻辑。</p>
 *
 * @author amani
 * @since 2026/05/15
 */
@Component
public class VideoTransferConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoTransferConsumer.class);

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;
    @Resource
    private MqIdempotentComponent mqIdempotentComponent;

    /**
     * 消费视频转码消息。
     *
     * @param event 转码事件
     */
    @RabbitListener(queues = MqConstants.VIDEO_TRANSFER_QUEUE)
    public void consumeVideoTransferEvent(VideoTransferEvent event) {
        if (event == null
                || !StringUtils.hasText(event.getEventId())
                || !StringUtils.hasText(event.getFileId())
                || !StringUtils.hasText(event.getUploadId())
                || !StringUtils.hasText(event.getVideoId())
                || !StringUtils.hasText(event.getUserId())) {
            log.warn("视频转码消息参数不完整，event:{}", event);
            return;
        }
        if (!mqIdempotentComponent.tryStart(MqConstants.VIDEO_TRANSFER_QUEUE, event.getEventId())) {
            return;
        }
        try {
            videoInfoFilePostService.transferVideo(event);
            mqIdempotentComponent.markDone(MqConstants.VIDEO_TRANSFER_QUEUE, event.getEventId());
        } catch (RuntimeException e) {
            mqIdempotentComponent.release(MqConstants.VIDEO_TRANSFER_QUEUE, event.getEventId());
            throw e;
        }
    }
}
