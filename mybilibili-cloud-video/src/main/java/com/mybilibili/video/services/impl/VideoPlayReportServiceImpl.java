package com.mybilibili.video.services.impl;

import com.mybilibili.base.entity.event.VideoPlayEvent;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.services.VideoInfoFilePostService;
import com.mybilibili.video.services.VideoPlayEventService;
import com.mybilibili.video.services.VideoPlayReportService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 播放上报实现。
 *
 * <p>前端只传 fileId，video 服务自己查出 videoId、分 P 下标和作者 id，
 * 这样可以避免客户端伪造统计字段，也方便后续统一调整播放统计规则。</p>
 */
@Service
public class VideoPlayReportServiceImpl implements VideoPlayReportService {

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoPlayEventService videoPlayEventService;

    @Override
    public void reportVideoPlayByFileId(String fileId, String userId) {
        // 匿名用户允许播放，但不进入正式播放量、历史记录和作者统计口径。
        if (userId == null || userId.isEmpty()) {
            return;
        }

        VideoInfoFilePost filePost = videoInfoFilePostService.getVideoInfoFilePostByFileId(fileId);
        if (filePost == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

        VideoPlayEvent videoPlayEvent = new VideoPlayEvent();
        videoPlayEvent.setFileId(filePost.getFileId());
        videoPlayEvent.setFileIndex(filePost.getFileIndex());
        videoPlayEvent.setVideoId(filePost.getVideoId());
        videoPlayEvent.setVideoUserId(filePost.getUserId());
        videoPlayEvent.setUserId(userId);
        videoPlayEvent.setPlayTime(new Date());

        // 上报入口和统计处理都在 video 服务内，直接调用本地 service，少一段 MQ 往返。
        videoPlayEventService.handleVideoPlayEvent(videoPlayEvent);
    }
}
