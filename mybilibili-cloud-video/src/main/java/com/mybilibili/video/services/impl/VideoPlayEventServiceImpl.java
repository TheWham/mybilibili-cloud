package com.mybilibili.video.services.impl;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.event.VideoPlayEvent;
import com.mybilibili.base.enums.UserStatsRedisEnum;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.services.VideoPlayEventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 视频播放事件处理实现。
 *
 * <p>这里集中处理播放事件对应的 Redis 快速统计。MQ 消费者只负责接收消息，
 * 不直接承载业务细节，方便后续复用和测试。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Service
public class VideoPlayEventServiceImpl implements VideoPlayEventService {

    @Resource
    private VideoRedisComponent videoRedisComponent;

    @Override
    public void handleVideoPlayEvent(VideoPlayEvent videoPlayEvent) {
        //统计用户观看次数
        videoRedisComponent.saveVideoPlayCount2HLL(videoPlayEvent.getVideoId(), videoPlayEvent.getUserId());

        boolean effectivePlay = videoRedisComponent.saveVideoEffectivePlay(videoPlayEvent.getVideoId(),
                videoPlayEvent.getUserId());

        if (effectivePlay) {
            refreshVideoPlayStats(videoPlayEvent);
        }

        //保存历史记录
        videoRedisComponent.saveVideoHistory(videoPlayEvent.getVideoId(),
                videoPlayEvent.getUserId(),
                videoPlayEvent.getFileIndex());
    }

    private void refreshVideoPlayStats(VideoPlayEvent videoPlayEvent) {
        if (videoPlayEvent.getVideoUserId() != null) {
            videoRedisComponent.incrementUserStats(videoPlayEvent.getVideoUserId(),
                    UserStatsRedisEnum.VIDEO_PLAY.getField(),
                    Constants.ONE);
        }
        videoRedisComponent.addVideoPlayCountDelta(videoPlayEvent.getVideoId());
    }
}
