package com.mybilibili.message.entity.vo;

/**
 * 收信箱信息数量分类显示
 * @author amani
 * @since 2026.4.14
 */
public class MessageTypeDataVO {
    private Integer messageCount;
    private Integer messageType;

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }
}
