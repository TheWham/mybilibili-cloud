package com.mybilibili.base.entity.event;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频播放事件。
 *
 * <p>media 负责投递播放事件，video 消费后处理播放历史、有效播放和播放量增量。
 * 事件对象会经过 MQ 序列化，字段保持简单，避免和某个服务的内部模型强绑定。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Data
public class VideoPlayEvent implements Serializable {

    /**
     * 视频 ID。
     */
    private String videoId;

    /**
     * 当前播放的分 P 文件 ID。
     */
    private String fileId;

    /**
     * 当前播放的分 P 下标。
     */
    private Integer fileIndex;

    /**
     * 观看用户 ID。未登录用户不写播放历史。
     */
    private String userId;

    /**
     * 视频作者用户 ID，用于刷新作者侧播放统计。
     */
    private String videoUserId;

    /**
     * 事件产生时间。
     */
    private Date playTime;
}
