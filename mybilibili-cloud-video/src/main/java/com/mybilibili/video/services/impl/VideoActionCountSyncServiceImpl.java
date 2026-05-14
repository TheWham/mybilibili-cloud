package com.mybilibili.video.services.impl;

import com.mybilibili.base.entity.dto.VideoSearchCountUpdateDTO;
import com.mybilibili.base.entity.event.UserActionSyncEvent;
import com.mybilibili.base.enums.SearchOrderTypeEnum;
import com.mybilibili.base.enums.UserActionTypeEnum;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.consumer.SearchVideoClient;
import com.mybilibili.video.entity.dto.VideoCountUpdateDTO;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.query.VideoInfoQuery;
import com.mybilibili.video.mappers.VideoInfoMapper;
import com.mybilibili.video.services.VideoActionCountSyncService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频互动计数同步实现。
 *
 * <p>数据库更新成功后再抵消 Redis delta。这样接口查询时只会叠加“还没有落库”的那部分，
 * 不会出现 MySQL 已更新但 Redis delta 又重复加一次的情况。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Service
public class VideoActionCountSyncServiceImpl implements VideoActionCountSyncService {

    private static final Logger log = LoggerFactory.getLogger(VideoActionCountSyncServiceImpl.class);

    @Resource
    private VideoInfoMapper<VideoInfo, VideoInfoQuery> videoInfoMapper;
    @Resource
    private VideoRedisComponent videoRedisComponent;
    @Resource
    private SearchVideoClient searchVideoClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncVideoActionCount(List<UserActionSyncEvent> eventList) {
        if (eventList == null || eventList.isEmpty()) {
            return;
        }

        Map<UserActionTypeEnum, List<VideoCountUpdateDTO>> countUpdateMap = new LinkedHashMap<>();
        Map<UserActionTypeEnum, List<UserActionSyncEvent>> searchEventMap = new LinkedHashMap<>();

        for (UserActionSyncEvent event : eventList) {
            UserActionTypeEnum actionTypeEnum = event == null ? null : UserActionTypeEnum.getEnum(event.getActionType());
            if (!supportVideoCount(actionTypeEnum) || event.getActionCount() == null || event.getActionCount() == 0) {
                continue;
            }
            mergeVideoCount(countUpdateMap, actionTypeEnum, event);
            mergeSearchEvent(searchEventMap, actionTypeEnum, event);
        }

        for (Map.Entry<UserActionTypeEnum, List<VideoCountUpdateDTO>> entry : countUpdateMap.entrySet()) {
            UserActionTypeEnum actionTypeEnum = entry.getKey();
            List<VideoCountUpdateDTO> updateList = entry.getValue();
            if (updateList.isEmpty()) {
                continue;
            }
            videoInfoMapper.updateCountBatch(actionTypeEnum.getField(), updateList);
            offsetVideoActionDelta(actionTypeEnum, updateList);
        }

        for (Map.Entry<UserActionTypeEnum, List<UserActionSyncEvent>> entry : searchEventMap.entrySet()) {
            syncSearchCollectCount(entry.getValue(), entry.getKey());
        }
    }

    private boolean supportVideoCount(UserActionTypeEnum actionTypeEnum) {
        return UserActionTypeEnum.VIDEO_LIKE.equals(actionTypeEnum)
                || UserActionTypeEnum.VIDEO_COLLECT.equals(actionTypeEnum)
                || UserActionTypeEnum.VIDEO_COIN.equals(actionTypeEnum)
                || UserActionTypeEnum.VIDEO_DNAMU.equals(actionTypeEnum);
    }

    private void syncSearchCollectCount(List<UserActionSyncEvent> eventList, UserActionTypeEnum actionTypeEnum) {
        SearchOrderTypeEnum orderTypeEnum = getSearchOrderType(actionTypeEnum);
        if (orderTypeEnum == null) {
            return;
        }
        try {
            for (UserActionSyncEvent event : eventList) {
                VideoSearchCountUpdateDTO updateDTO = new VideoSearchCountUpdateDTO();
                updateDTO.setVideoId(event.getVideoId());
                updateDTO.setChangeCount(event.getActionCount());
                updateDTO.setOrderType(orderTypeEnum.getStatus());
                searchVideoClient.updateVideoCount(updateDTO);
            }
        } catch (Exception e) {
            // 搜索索引是展示侧数据，失败不回滚主库；后续重建索引可以修正。
            log.warn("同步视频计数到搜索索引失败，videoId:{}, actionType:{}",
                    eventList.isEmpty() ? null : eventList.get(0).getVideoId(), actionTypeEnum, e);
        }
    }

    private void mergeVideoCount(Map<UserActionTypeEnum, List<VideoCountUpdateDTO>> countUpdateMap,
                                 UserActionTypeEnum actionTypeEnum,
                                 UserActionSyncEvent event) {
        List<VideoCountUpdateDTO> updateList = countUpdateMap.computeIfAbsent(actionTypeEnum, key -> new ArrayList<>());
        VideoCountUpdateDTO countUpdateDTO = findCountUpdate(updateList, event.getVideoId());
        if (countUpdateDTO == null) {
            updateList.add(new VideoCountUpdateDTO(event.getVideoId(), event.getActionCount()));
            return;
        }
        countUpdateDTO.setCount(countUpdateDTO.getCount() + event.getActionCount());
    }

    private void mergeSearchEvent(Map<UserActionTypeEnum, List<UserActionSyncEvent>> searchEventMap,
                                  UserActionTypeEnum actionTypeEnum,
                                  UserActionSyncEvent event) {
        SearchOrderTypeEnum orderTypeEnum = getSearchOrderType(actionTypeEnum);
        if (orderTypeEnum == null) {
            return;
        }
        List<UserActionSyncEvent> eventList = searchEventMap.computeIfAbsent(actionTypeEnum, key -> new ArrayList<>());
        UserActionSyncEvent mergedEvent = findSearchEvent(eventList, event.getVideoId());
        if (mergedEvent == null) {
            UserActionSyncEvent copyEvent = new UserActionSyncEvent();
            copyEvent.setVideoId(event.getVideoId());
            copyEvent.setActionCount(event.getActionCount());
            eventList.add(copyEvent);
            return;
        }
        mergedEvent.setActionCount(mergedEvent.getActionCount() + event.getActionCount());
    }

    private void offsetVideoActionDelta(UserActionTypeEnum actionTypeEnum, List<VideoCountUpdateDTO> updateList) {
        for (VideoCountUpdateDTO updateDTO : updateList) {
            videoRedisComponent.addVideoActionCountDelta(updateDTO.getVideoId(),
                    actionTypeEnum.getField(),
                    -updateDTO.getCount());
        }
    }

    private VideoCountUpdateDTO findCountUpdate(List<VideoCountUpdateDTO> updateList, String videoId) {
        for (VideoCountUpdateDTO updateDTO : updateList) {
            if (videoId.equals(updateDTO.getVideoId())) {
                return updateDTO;
            }
        }
        return null;
    }

    private UserActionSyncEvent findSearchEvent(List<UserActionSyncEvent> eventList, String videoId) {
        for (UserActionSyncEvent event : eventList) {
            if (videoId.equals(event.getVideoId())) {
                return event;
            }
        }
        return null;
    }

    private SearchOrderTypeEnum getSearchOrderType(UserActionTypeEnum actionTypeEnum) {
        if (UserActionTypeEnum.VIDEO_COLLECT.equals(actionTypeEnum)) {
            return SearchOrderTypeEnum.VIDEO_COLLECT;
        }
        if (UserActionTypeEnum.VIDEO_DNAMU.equals(actionTypeEnum)) {
            return SearchOrderTypeEnum.VIDEO_DANMU;
        }
        return null;
    }
}
