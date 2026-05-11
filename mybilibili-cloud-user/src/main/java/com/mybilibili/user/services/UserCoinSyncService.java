package com.mybilibili.user.services;

import com.mybilibili.base.entity.event.UserCoinSyncEvent;

/**
 * 用户硬币同步服务。
 *
 * @author amani
 * @since 2026/05/11
 */
public interface UserCoinSyncService {

    /**
     * 同步投币或审核奖励产生的硬币变化。
     *
     * @param event 用户硬币事件
     */
    void syncUserCoin(UserCoinSyncEvent event);
}
