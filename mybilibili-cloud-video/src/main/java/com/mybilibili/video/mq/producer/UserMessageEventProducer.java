package com.mybilibili.video.mq.producer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserMessageEvent;
import com.mybilibili.base.enums.MessageTypeEnum;
import com.mybilibili.base.utils.JsonUtils;
import com.mybilibili.video.entity.po.VideoInfoPost;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 视频服务用户站内信事件生产者。
 *
 * <p>视频审核属于 video 服务的业务事实，message 服务只消费最终通知事件。</p>
 *
 * @author amani
 * @since 2026/05/13
 */
@Component
public class UserMessageEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendAuditVideoMessage(VideoInfoPost videoInfoPost, Integer auditStatus, String reason) {
        if (videoInfoPost == null || StringUtils.isBlank(videoInfoPost.getUserId()) || auditStatus == null) {
            return;
        }

        Map<String, Object> extendInfo = new HashMap<>(5);
        extendInfo.put("videoId", videoInfoPost.getVideoId());
        extendInfo.put("videoName", videoInfoPost.getVideoName());
        extendInfo.put("videoCover", videoInfoPost.getVideoCover());
        extendInfo.put("auditStatus", auditStatus);
        extendInfo.put("messageContent", StringUtils.defaultString(reason));

        UserMessageEvent event = new UserMessageEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setReceiveUserId(videoInfoPost.getUserId());
        event.setVideoId(videoInfoPost.getVideoId());
        event.setMessageType(MessageTypeEnum.SYSTEM.getType());
        event.setCreateTime(new Date());
        event.setExtendJson(JsonUtils.convertObj2Json(extendInfo));
        rabbitTemplate.convertAndSend(MqConstants.USER_MESSAGE_EXCHANGE,
                MqConstants.USER_MESSAGE_PERSIST_ROUTING_KEY,
                event);
    }
}
