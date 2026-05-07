package com.mybilibili.message.entity.vo;

import com.mybilibili.message.entity.dto.UserMessageExtendDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author amani
 * @since 2026.4.14
 * 收件箱信息显示
 */

@Data
public class MessageNoticeVO {
    /**
     * 发送人头像
     */
    private String sendUserAvatar;
    /**
     * 发送人id
     */
    private String sendUserId;
    /**
     * 发送人姓名
     */
    private String sendUserName;
    /**
     * 信息类型
     */
    private Integer messageType;
    /**
     * 信息id
     */
    private Integer messageId;
    /**
     * 通知时间
     */
    private Date createTime;
    /**
     * 视频id
     */
    private String videoId;
    /**
     * 视频名称
     */
    private String videoName;
    /**
     * 视频缩放图
     */
    private String videoCover;
    /**
     * 拓展信息
     */
    private UserMessageExtendDTO extendDto;
}
