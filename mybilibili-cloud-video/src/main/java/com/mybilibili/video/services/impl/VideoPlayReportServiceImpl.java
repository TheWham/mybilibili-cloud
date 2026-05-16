package com.mybilibili.video.services.impl;

import com.mybilibili.base.entity.event.VideoPlayEvent;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.entity.dto.VideoPlayFileMetaDTO;
import com.mybilibili.video.entity.po.VideoInfoFile;
import com.mybilibili.video.services.VideoInfoFileService;
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
    private VideoInfoFileService videoInfoFileService;

    @Resource
    private VideoRedisComponent videoRedisComponent;

    @Resource
    private VideoPlayEventService videoPlayEventService;

    @Override
    public void reportVideoPlayByFileId(String fileId, String userId) {
        // 匿名用户允许播放，但不进入正式播放量、历史记录和作者统计口径。
        if (userId == null || userId.isEmpty()) {
            return;
        }

        //这里从redis中取出 没有走db
        VideoPlayFileMetaDTO fileMeta = getVideoPlayFileMeta(fileId);

        VideoPlayEvent videoPlayEvent = new VideoPlayEvent();
        videoPlayEvent.setFileId(fileMeta.getFileId());
        videoPlayEvent.setFileIndex(fileMeta.getFileIndex());
        videoPlayEvent.setVideoId(fileMeta.getVideoId());
        videoPlayEvent.setVideoUserId(fileMeta.getVideoUserId());
        videoPlayEvent.setUserId(userId);
        videoPlayEvent.setPlayTime(new Date());

        // 上报入口和统计处理都在 video 服务内，直接调用本地 service，少一段 MQ 往返。
        videoPlayEventService.handleVideoPlayEvent(videoPlayEvent);
    }

    /**
     * 获取播放上报所需的分 P 元数据。
     *
     * <p>播放上报是高频接口，重复播放和刷新页面都会触发。这里先读 Redis，
     * 未命中时再查正式分 P 表并回填缓存，避免把每次有效播放判断前的元数据查询都压到 MySQL。</p>
     *
     * @param fileId 分 P 文件 id
     * @return 播放事件需要的分 P 元数据
     */
    private VideoPlayFileMetaDTO getVideoPlayFileMeta(String fileId) {
        VideoPlayFileMetaDTO fileMeta = videoRedisComponent.getVideoPlayFileMeta(fileId);
        if (fileMeta != null) {
            return fileMeta;
        }

        VideoInfoFile videoInfoFile = videoInfoFileService.getVideoInfoFileByFileId(fileId);
        if (videoInfoFile == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }

        fileMeta = buildVideoPlayFileMeta(videoInfoFile);
        videoRedisComponent.saveVideoPlayFileMeta(fileMeta);
        return fileMeta;
    }

    /**
     * 将正式分 P 表的数据转换成播放事件元数据。
     *
     * <p>这里故意只取统计所需字段，不把文件名、路径等无关信息放进缓存，
     * 后续排查播放量问题时能更清楚地区分“播放元数据”和“视频资源信息”。</p>
     *
     * @param videoInfoFile 已发布的视频分 P
     * @return 播放事件元数据
     */
    private VideoPlayFileMetaDTO buildVideoPlayFileMeta(VideoInfoFile videoInfoFile) {
        VideoPlayFileMetaDTO fileMeta = new VideoPlayFileMetaDTO();
        fileMeta.setFileId(videoInfoFile.getFileId());
        fileMeta.setVideoId(videoInfoFile.getVideoId());
        fileMeta.setVideoUserId(videoInfoFile.getUserId());
        fileMeta.setFileIndex(videoInfoFile.getFileIndex());
        return fileMeta;
    }
}
