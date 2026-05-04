package com.mybilibili.video.services;

import com.mybilibili.base.entity.event.VideoPlayEvent;

/**
 * 视频播放事件处理服务。
 *
 * @author amani
 * @since 2026/05/04
 */
public interface VideoPlayEventService {

    /**
     * 处理播放事件。
     *
     * @param videoPlayEvent 播放事件
     */
    void handleVideoPlayEvent(VideoPlayEvent videoPlayEvent);
}
