package com.mybilibili.video.services;

import com.mybilibili.base.entity.event.UserActionSyncEvent;

/**
 * 视频互动计数同步服务。
 *
 * @author amani
 * @since 2026/05/11
 */
public interface VideoActionCountSyncService {

    /**
     * 同步点赞、收藏、投币计数到 video_info。
     *
     * @param event 用户互动事件
     */
    void syncVideoActionCount(UserActionSyncEvent event);
}
