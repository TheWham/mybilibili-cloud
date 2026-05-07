package com.mybilibili.media.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.TokenUserInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.base.entity.event.VideoPlayEvent;
import com.mybilibili.media.consumer.VideoInfoClient;
import com.mybilibili.media.mq.producer.VideoPlayEventProducer;
import jakarta.annotation.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Resource
    private VideoInfoClient videoInfoClient;

    /**
     * 读取 m3u8 资源。
     *
     * <p>当前 video 文件元数据查询接口还没有迁移完成，先返回 404，避免 media
     * 为了编译通过直接依赖 video 的数据库实现。</p>
     */
    public ResponseEntity<UrlResource> videoResource(String fileId, String projectFolder) throws MalformedURLException {
        String filePath = getFilePath(fileId);
        String completeFilePath = projectFolder + Constants.FILE_PATH_FOLDER + filePath;
        Path m3u8FilePath = Paths.get(completeFilePath).resolve(Constants.M3U8_NAME).normalize();
        if (!Files.exists(m3u8FilePath) || !Files.isRegularFile(m3u8FilePath)) {
            return ResponseEntity.notFound().build();
        }
        UrlResource urlResource = new UrlResource(m3u8FilePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(urlResource);
    }

    /**
     * 读取 ts 资源。
     *
     * <p>恢复文件读取时，在这里根据 video 服务返回的 VideoInfoFilePostDTO 组装文件路径，
     * 并调用 {@link #sendVideoPlayEvent(VideoInfoFilePostDTO, TokenUserInfoDTO)} 投递播放事件。</p>
     */
    public ResponseEntity<UrlResource> videoResource(String fileId, String name, String projectFolder) throws MalformedURLException {
        String filePath = getFilePath(fileId);
        String completeFilePath = projectFolder + Constants.FILE_PATH_FOLDER + filePath;
        Path tsFilePath = Paths.get(completeFilePath).resolve(name).normalize();
        if (!Files.exists(tsFilePath) || !Files.isRegularFile(tsFilePath)) {
            return ResponseEntity.notFound().build();
        }
        //TODO 展示先保证正常播放, 后续需要进行mq处理redis的 uv真实记录统计, 用户播放记录, 用户数量缓存
//        String tokenId = getTokenIdFromCookie();
//        TokenUserInfoDTO tokenInfo = redisComponent.getTokenInfo(tokenId);
//        if (tokenInfo != null) {
//            VideoPlayDTO videoPlayDTO = new VideoPlayDTO();
//            videoPlayDTO.setVideoId(filePostByFileId.getVideoId());
//            videoPlayDTO.setFileIndex(filePostByFileId.getFileIndex());
//            videoPlayDTO.setUserId(tokenInfo.getUserId());
//            videoPlayDTO.setVideoUserId(filePostByFileId.getUserId());
//            //用uv做真实记录统计
//            redisComponent.saveVideoPlayCount2HLL(videoPlayDTO.getVideoId(), videoPlayDTO.getUserId());
//            //用户id保留30min, 30min之后再次观看可记录到播放统计
//            boolean isEffectivePlay = redisComponent.saveVideoEffectivePlay(videoPlayDTO.getVideoId(), videoPlayDTO.getUserId());
//            if (isEffectivePlay) {
//                //设置用户数量缓存
//                redisComponent.incrementUserStats(videoPlayDTO.getVideoUserId(), UserStatsRedisEnum.VIDEO_PLAY.getField(), Constants.ONE);
//                //记录增量20s统计完之后清空
//                redisComponent.addVideoPlayCountDelta(videoPlayDTO.getVideoId());
//            }
//            redisComponent.saveVideoHistory(videoPlayDTO.getVideoId(), videoPlayDTO.getUserId(), videoPlayDTO.getFileIndex());
//        }

        UrlResource urlResource = new UrlResource(tsFilePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(urlResource);
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
    private String getFilePath(String fileId)
    {
        return videoInfoClient.getFilePath(fileId);
    }

}
