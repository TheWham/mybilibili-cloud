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

import java.util.List;

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
    public void syncVideoActionCount(UserActionSyncEvent event) {
        UserActionTypeEnum actionTypeEnum = UserActionTypeEnum.getEnum(event.getActionType());
        if (!supportVideoCount(actionTypeEnum) || event.getActionCount() == null || event.getActionCount() == 0) {
            return;
        }

        videoInfoMapper.updateCountBatch(actionTypeEnum.getField(),
                List.of(new VideoCountUpdateDTO(event.getVideoId(), event.getActionCount())));
        videoRedisComponent.addVideoActionCountDelta(event.getVideoId(), actionTypeEnum.getField(), -event.getActionCount());
        syncSearchCollectCount(event, actionTypeEnum);
    }

    private boolean supportVideoCount(UserActionTypeEnum actionTypeEnum) {
        return UserActionTypeEnum.VIDEO_LIKE.equals(actionTypeEnum)
                || UserActionTypeEnum.VIDEO_COLLECT.equals(actionTypeEnum)
                || UserActionTypeEnum.VIDEO_COIN.equals(actionTypeEnum);
    }

    private void syncSearchCollectCount(UserActionSyncEvent event, UserActionTypeEnum actionTypeEnum) {
        if (!UserActionTypeEnum.VIDEO_COLLECT.equals(actionTypeEnum)) {
            return;
        }
        try {
            VideoSearchCountUpdateDTO updateDTO = new VideoSearchCountUpdateDTO();
            updateDTO.setVideoId(event.getVideoId());
            updateDTO.setChangeCount(event.getActionCount());
            updateDTO.setOrderType(SearchOrderTypeEnum.VIDEO_COLLECT.getStatus());
            searchVideoClient.updateVideoCount(updateDTO);
        } catch (Exception e) {
            // 搜索索引是展示侧数据，失败不回滚主库；后续重建索引可以修正。
            log.warn("同步收藏数到搜索索引失败，videoId:{}", event.getVideoId(), e);
        }
    }
}
