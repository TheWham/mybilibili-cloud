package com.mybilibili.interact.services.impl;

import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.base.entity.query.UserActionQuery;
import com.mybilibili.interact.entity.po.UserVideoAction;
import com.mybilibili.interact.mappers.UserVideoActionMapper;
import com.mybilibili.interact.services.UserActionPersistService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户行为落库实现。
 *
 * <p>interact 只维护用户做过哪些互动。视频计数和用户硬币数分别由 video、user
 * 服务消费同一业务事件后处理。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Service
public class UserActionPersistServiceImpl implements UserActionPersistService {

    @Resource
    private UserVideoActionMapper<UserVideoAction, UserActionQuery> userVideoActionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserVideoAction(UserActionSyncEvent event) {
        if (Boolean.FALSE.equals(event.getActive())) {
            userVideoActionMapper.deleteByVideoIdAndActionTypeAndUserId(
                    event.getVideoId(),
                    event.getActionType(),
                    event.getUserId());
            return;
        }
        UserVideoAction userVideoAction = new UserVideoAction();
        userVideoAction.setVideoId(event.getVideoId());
        userVideoAction.setVideoUserId(event.getVideoUserId());
        userVideoAction.setActionType(event.getActionType());
        userVideoAction.setActionCount(Math.abs(event.getActionCount()));
        userVideoAction.setUserId(event.getUserId());
        userVideoAction.setActionTime(event.getActionTime());
        userVideoActionMapper.insertOrUpdateBatch(List.of(userVideoAction));
    }
}
