package com.mybilibili.interact.services;

import com.mybilibili.base.entity.event.UserActionSyncEvent;

/**
 * 用户行为落库服务。
 *
 * @author amani
 * @since 2026/05/11
 */
public interface UserActionPersistService {

    /**
     * 同步用户视频行为到 MySQL。
     *
     * @param event 用户行为事件
     */
    void syncUserVideoAction(UserActionSyncEvent event);
}
