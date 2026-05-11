package com.mybilibili.interact.services;

import com.mybilibili.interact.entity.dto.UserActionDTO;

/**
 * 用户互动命令服务。
 *
 * @author amani
 * @since 2026/05/11
 */
public interface UserActionCommandService {

    /**
     * 执行视频点赞、收藏、投币。
     *
     * @param userActionDTO 互动参数
     * @param userId 当前登录用户 id
     */
    void doAction(UserActionDTO userActionDTO, String userId);
}
