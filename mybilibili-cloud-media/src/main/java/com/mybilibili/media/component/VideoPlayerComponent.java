package com.mybilibili.media.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.base.enums.ResponseCodeEnum;
import com.mybilibili.base.exception.BusinessException;
import com.mybilibili.media.consumer.VideoInfoClient;
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
import java.util.Optional;

/**
 * 视频资源播放组件。
 *
 * <p>media 服务只负责资源读取。根据 fileId 查询分 P 元数据的能力
 * 后续应通过 video 服务内部接口获取，不能在 media 里直接依赖 video 的 mapper。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Component
public class VideoPlayerComponent {

    @Resource
    private VideoInfoClient videoInfoClient;

    /**
     * 读取 m3u8 资源。
     *
     * <p>当前 video 文件元数据查询接口还没有迁移完成，先返回 404，避免 media
     * 为了编译通过直接依赖 video 的数据库实现。</p>
     */
    public ResponseEntity<UrlResource> videoResource(String fileId, String projectFolder) throws MalformedURLException {
        VideoInfoFilePostDTO filePostDTO = getFilePostDTO(fileId);

        Optional.ofNullable(filePostDTO).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_404));

        String filePath = filePostDTO.getFilePath();
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
     * <p>这里只负责返回 ts 切片内容。播放统计已经迁到 video 服务独立接口，
     * 不能继续挂在资源请求链路里，否则 HLS 分片请求会把统计调用放大。</p>
     */
    public ResponseEntity<UrlResource> videoResource(String fileId, String name, String projectFolder) throws MalformedURLException {
        VideoInfoFilePostDTO filePostDTO = getFilePostDTO(fileId);

        Optional.ofNullable(filePostDTO).orElseThrow(() -> new BusinessException(ResponseCodeEnum.CODE_404));

        String filePath = filePostDTO.getFilePath();
        String completeFilePath = projectFolder + Constants.FILE_PATH_FOLDER + filePath;
        Path tsFilePath = Paths.get(completeFilePath).resolve(name).normalize();
        if (!Files.exists(tsFilePath) || !Files.isRegularFile(tsFilePath)) {
            return ResponseEntity.notFound().build();
        }

        UrlResource urlResource = new UrlResource(tsFilePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp2t"))
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(urlResource);
    }

    private VideoInfoFilePostDTO getFilePostDTO(String fileId) {
        return videoInfoClient.getFilePostDTO(fileId);
    }

}
