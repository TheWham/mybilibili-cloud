package com.mybilibili.interact.mq.producer;

import com.mybilibili.base.constants.MqConstants;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.base.entity.event.UserMessageEvent;
import com.mybilibili.base.enums.MessageTypeEnum;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.utils.JsonUtils;
import com.mybilibili.interact.entity.po.VideoComment;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户站内信事件生产者。
 *
 * <p>interact 只判断“该不该通知”和补齐业务扩展字段，真正落库交给 message 服务。</p>
 *
 * @author amani
 * @since 2026/05/13
 */
@Component
public class UserMessageEventProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendUserActionMessage(UserActionSyncEvent event) {
        if (!validActiveAction(event)) {
            return;
        }

        MessageTypeEnum messageTypeEnum = resolveVideoActionMessageType(event.getActionType());
        if (messageTypeEnum == null) {
            return;
        }

        Map<String, Object> extendInfo = new HashMap<>(4);
        extendInfo.put("actionType", event.getActionType());
        extendInfo.put("actionCount", Math.abs(event.getActionCount()));
        extendInfo.put("videoId", event.getVideoId());
        sendMessage(event.getVideoUserId(), event.getUserId(), event.getVideoId(),
                messageTypeEnum, event.getActionTime(), JsonUtils.convertObj2Json(extendInfo));
    }

    public void sendCommentMessage(VideoComment videoComment) {
        if (videoComment == null) {
            return;
        }

        boolean reply = StringUtils.hasText(videoComment.getReplyUserId());
        String receiveUserId = reply ? videoComment.getReplyUserId() : videoComment.getVideoUserId();

        Map<String, Object> extendInfo = new HashMap<>(5);
        extendInfo.put("commentId", videoComment.getCommentId());
        extendInfo.put("replyCommentId", videoComment.getReplyCommentId());
        extendInfo.put("pCommentId", videoComment.getPCommentId());
        extendInfo.put(reply ? "messageContent" : "messageContentReply", videoComment.getContent());
        extendInfo.put("replyUserId", videoComment.getReplyUserId());
        sendMessage(receiveUserId, videoComment.getUserId(), videoComment.getVideoId(),
                MessageTypeEnum.COMMENT, videoComment.getPostTime(), JsonUtils.convertObj2Json(extendInfo));
    }

    public void sendCommentLikeMessage(String sendUserId, VideoComment videoComment, Date createTime) {
        if (videoComment == null) {
            return;
        }

        Map<String, Object> extendInfo = new HashMap<>(4);
        extendInfo.put("actionType", UserActionTypeEnum.COMMENT_LIKE.getType());
        extendInfo.put("commentId", videoComment.getCommentId());
        extendInfo.put("videoId", videoComment.getVideoId());
        extendInfo.put("content", videoComment.getContent());
        sendMessage(videoComment.getUserId(), sendUserId, videoComment.getVideoId(),
                MessageTypeEnum.LIKE, createTime, JsonUtils.convertObj2Json(extendInfo));
    }

    private void sendMessage(String receiveUserId,
                             String sendUserId,
                             String videoId,
                             MessageTypeEnum messageTypeEnum,
                             Date createTime,
                             String extendJson) {
        if (!StringUtils.hasText(receiveUserId) || messageTypeEnum == null) {
            return;
        }
        if (StringUtils.hasText(sendUserId) && receiveUserId.equals(sendUserId)) {
            return;
        }

        UserMessageEvent event = new UserMessageEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setReceiveUserId(receiveUserId);
        event.setSendUserId(sendUserId);
        event.setVideoId(videoId);
        event.setMessageType(messageTypeEnum.getType());
        event.setCreateTime(createTime == null ? new Date() : createTime);
        event.setExtendJson(extendJson);
        rabbitTemplate.convertAndSend(MqConstants.USER_MESSAGE_EXCHANGE,
                MqConstants.USER_MESSAGE_PERSIST_ROUTING_KEY,
                event);
    }

    private boolean validActiveAction(UserActionSyncEvent event) {
        return event != null
                && Boolean.TRUE.equals(event.getActive())
                && StringUtils.hasText(event.getUserId())
                && StringUtils.hasText(event.getVideoUserId())
                && StringUtils.hasText(event.getVideoId())
                && event.getActionType() != null;
    }

    private MessageTypeEnum resolveVideoActionMessageType(Integer actionType) {
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(actionType);
        if (UserActionTypeEnum.VIDEO_LIKE.equals(actionTypeEnum)) {
            return MessageTypeEnum.LIKE;
        }
        if (UserActionTypeEnum.VIDEO_COLLECT.equals(actionTypeEnum)) {
            return MessageTypeEnum.COLLECT;
        }
        return null;
    }
}
