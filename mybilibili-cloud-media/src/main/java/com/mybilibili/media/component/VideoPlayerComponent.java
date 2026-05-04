package com.mybilibili.media.component;

import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.event.VideoPlayEvent;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.media.mq.producer.VideoPlayEventProducer;
import jakarta.annotation.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.Date;

/**
 * 视频资源播放组件。
 *
 * <p>media 服务只负责资源读取和播放事件投递。根据 fileId 查询分 P 元数据的能力
 * 后续应通过 video 服务内部接口获取，不能在 media 里直接依赖 video 的 mapper。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Component
public class VideoPlayerComponent {

    @Resource
    private VideoPlayEventProducer videoPlayEventProducer;

    /**
     * 读取 m3u8 资源。
     *
     * <p>当前 video 文件元数据查询接口还没有迁移完成，先返回 404，避免 media
     * 为了编译通过直接依赖 video 的数据库实现。</p>
     */
    public ResponseEntity<UrlResource> videoResource(String fileId, String projectFolder) throws MalformedURLException {
        return ResponseEntity.notFound().build();
    }

    /**
     * 读取 ts 资源。
     *
     * <p>恢复文件读取时，在这里根据 video 服务返回的 VideoInfoFilePostDTO 组装文件路径，
     * 并调用 {@link #sendVideoPlayEvent(VideoInfoFilePostDTO, TokenUserInfoDTO)} 投递播放事件。</p>
     */
    public ResponseEntity<UrlResource> videoResource(String fileId, String name, String projectFolder) throws MalformedURLException {
        return ResponseEntity.notFound().build();
    }

    /**
     * 投递播放事件。这里不写 Redis，播放统计由 video 服务消费 MQ 后处理。
     */
    public void sendVideoPlayEvent(VideoInfoFilePostDTO filePost, TokenUserInfoDTO tokenUserInfoDTO) {
        if (filePost == null || tokenUserInfoDTO == null) {
            return;
        }
        VideoPlayEvent videoPlayEvent = new VideoPlayEvent();
        videoPlayEvent.setFileId(filePost.getFileId());
        videoPlayEvent.setFileIndex(filePost.getFileIndex());
        videoPlayEvent.setVideoId(filePost.getVideoId());
        videoPlayEvent.setVideoUserId(filePost.getUserId());
        videoPlayEvent.setUserId(tokenUserInfoDTO.getUserId());
        videoPlayEvent.setPlayTime(new Date());
        videoPlayEventProducer.sendVideoPlayEvent(videoPlayEvent);
    }
}
