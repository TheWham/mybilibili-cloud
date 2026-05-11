package com.mybilibili.interact.services.impl;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.interact.component.InteractRedisComponent;
import com.mybilibili.interact.consumer.UserInfoClient;
import com.mybilibili.interact.consumer.VideoInfoClient;
import com.mybilibili.interact.entity.dto.UserActionDTO;
import com.mybilibili.interact.mq.producer.UserActionEventProducer;
import com.mybilibili.interact.services.UserActionCommandService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 用户互动命令实现。
 *
 * <p>这里不直接写 MySQL：请求线程只负责 Redis 原子状态和 MQ 事件投递。
 * 落库失败可以通过 MQ 重试处理，不把高频互动压力压到接口链路上。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Service
public class UserActionCommandServiceImpl implements UserActionCommandService {

    private static final Long VIDEO_COIN_SUCCESS = 0L;
    private static final Long VIDEO_COIN_ALREADY_DONE = 1L;
    private static final Long VIDEO_COIN_NOT_ENOUGH = 2L;
    private static final Long VIDEO_TOGGLE_ACTIVE = 1L;
    private static final Long VIDEO_TOGGLE_CANCEL = -1L;

    @Resource
    private VideoInfoClient videoInfoClient;
    @Resource
    private UserInfoClient userInfoClient;
    @Resource
    private InteractRedisComponent interactRedisComponent;
    @Resource
    private UserActionEventProducer userActionEventProducer;

    @Override
    public void doAction(UserActionDTO userActionDTO, String userId) {
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(userActionDTO.getActionType());
        VideoInfoDTO videoInfo = Optional.ofNullable(videoInfoClient.getVideoInfoByVideoId(userActionDTO.getVideoId()))
                .orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_600));

        if (actionTypeEnum == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        switch (actionTypeEnum) {
            case VIDEO_LIKE:
            case VIDEO_COLLECT:
                handleToggleAction(userActionDTO, userId, videoInfo, actionTypeEnum);
                break;
            case VIDEO_COIN:
                handleCoinAction(userActionDTO, userId, videoInfo, actionTypeEnum);
                break;
            default:
                throw new BusinessException("当前互动类型暂未接入异步同步");
        }
    }

    private void handleToggleAction(UserActionDTO userActionDTO,
                                    String userId,
                                    VideoInfoDTO videoInfo,
                                    UserActionTypeEnum actionTypeEnum) {
        if (userId.equals(videoInfo.getUserId())) {
            String actionName = UserActionTypeEnum.VIDEO_LIKE.equals(actionTypeEnum) ? "点赞" : "收藏";
            throw new BusinessException("不能给自己的视频" + actionName);
        }

        int actionCount = normalizeActionCount(userActionDTO.getActionCount());
        String statsField = UserActionTypeEnum.VIDEO_LIKE.equals(actionTypeEnum)
                ? UserStatsRedisEnum.VIDEO_LIKE.getField()
                : UserStatsRedisEnum.USER_COLLECT_COUNT.getField();
        Long luaResult = interactRedisComponent.executeVideoToggleAction(
                userId,
                videoInfo.getUserId(),
                userActionDTO.getVideoId(),
                actionTypeEnum.getType(),
                actionCount,
                statsField
        );
        if (luaResult == null) {
            throw new BusinessException("操作失败,请稍后重试");
        }

        boolean active = VIDEO_TOGGLE_ACTIVE.equals(luaResult);
        if (!active && !VIDEO_TOGGLE_CANCEL.equals(luaResult)) {
            throw new BusinessException("操作失败,请稍后重试");
        }

        int delta = active ? actionCount : -actionCount;
        interactRedisComponent.addVideoActionCountDelta(userActionDTO.getVideoId(), actionTypeEnum.getField(), delta);
        userActionEventProducer.sendUserActionEvent(buildActionEvent(userActionDTO, userId, videoInfo.getUserId(),
                actionTypeEnum, active, delta));
    }

    private void handleCoinAction(UserActionDTO userActionDTO,
                                  String userId,
                                  VideoInfoDTO videoInfo,
                                  UserActionTypeEnum actionTypeEnum) {
        if (userId.equals(videoInfo.getUserId())) {
            throw new BusinessException("不能给自己投币");
        }

        int actionCount = normalizeActionCount(userActionDTO.getActionCount());
        ensureCurrentCoinLoaded(userId);
        ensureCurrentCoinLoaded(videoInfo.getUserId());

        Long luaResult = interactRedisComponent.executeVideoCoinAction(
                userId,
                videoInfo.getUserId(),
                userActionDTO.getVideoId(),
                actionTypeEnum.getType(),
                actionCount
        );
        if (luaResult == null) {
            throw new BusinessException("投币失败,请稍后重试");
        }
        if (VIDEO_COIN_ALREADY_DONE.equals(luaResult)) {
            throw new BusinessException("已经投过币了");
        }
        if (VIDEO_COIN_NOT_ENOUGH.equals(luaResult)) {
            throw new BusinessException("投币失败,硬币不足");
        }
        if (!VIDEO_COIN_SUCCESS.equals(luaResult)) {
            throw new BusinessException("投币失败,请稍后重试");
        }

        interactRedisComponent.addVideoActionCountDelta(userActionDTO.getVideoId(), actionTypeEnum.getField(), actionCount);
        userActionEventProducer.sendUserActionEvent(buildActionEvent(userActionDTO, userId, videoInfo.getUserId(),
                actionTypeEnum, true, actionCount));
    }

    private void ensureCurrentCoinLoaded(String userId) {
        Integer currentCoin = interactRedisComponent.getUserStatsValue(userId, UserStatsRedisEnum.USER_COIN.getField());
        if (currentCoin != null) {
            return;
        }
        List<UserInfoDTO> userInfoList = userInfoClient.getUserInfoByIds(List.of(userId));
        if (userInfoList == null || userInfoList.isEmpty() || userInfoList.get(0).getCurrentCoinCount() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        interactRedisComponent.setUserStatsValue(userId,
                UserStatsRedisEnum.USER_COIN.getField(),
                userInfoList.get(0).getCurrentCoinCount());
    }

    private UserActionSyncEvent buildActionEvent(UserActionDTO userActionDTO,
                                                 String userId,
                                                 String videoUserId,
                                                 UserActionTypeEnum actionTypeEnum,
                                                 boolean active,
                                                 int actionCount) {
        UserActionSyncEvent event = new UserActionSyncEvent();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setUserId(userId);
        event.setVideoId(userActionDTO.getVideoId());
        event.setVideoUserId(videoUserId);
        event.setActionType(actionTypeEnum.getType());
        event.setActionCount(actionCount);
        event.setActive(active);
        event.setActionTime(new Date());
        return event;
    }

    private int normalizeActionCount(Integer actionCount) {
        if (actionCount == null || actionCount <= 0) {
            return Constants.ONE;
        }
        return Math.min(actionCount, Constants.TWO);
    }
}
