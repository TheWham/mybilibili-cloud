package com.mybilibili.media.component;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UploadingFileDTO;
import com.mybilibili.base.entity.po.VideoInfoFilePost;
import com.mybilibili.common.redis.RedisUtils;
import com.mybilibili.media.constants.MediaRedisKeys;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * media 服务文件处理缓存。
 */
@Component
public class MediaRedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public void saveFileInfo(String userId, UploadingFileDTO uploadingFileDto) {
        String key = MediaRedisKeys.UPLOADING_FILE_INFO_KEY + userId + uploadingFileDto.getUploadId();
        redisUtils.setex(key, uploadingFileDto, Constants.REDIS_EXPIRE_TIME_ONE_DAY);
    }

    public UploadingFileDTO getUploadFileInfo(String key) {
        return (UploadingFileDTO) redisUtils.get(key);
    }

    public void delUploadVideoInfo(String userId, @NotEmpty String uploadId) {
        redisUtils.delete(MediaRedisKeys.UPLOADING_FILE_INFO_KEY + userId + uploadId);
    }

    public void addFileList2DelQueue(String videoId, List<String> filePathList) {
        redisUtils.lpushAll(MediaRedisKeys.DEL_FILE_QUEUE + videoId,
                filePathList,
                (long) Constants.REDIS_EXPIRE_TIME_ONE_DAY * 7);
    }

    public List<String> getDelFilePathsQueue(String videoId) {
        return redisUtils.getQueueList(MediaRedisKeys.DEL_FILE_QUEUE + videoId);
    }

    public void cleanDelFilePaths(String videoId) {
        redisUtils.delete(MediaRedisKeys.DEL_FILE_QUEUE + videoId);
    }

    public void addFileList2TransferQueue(List<VideoInfoFilePost> addList) {
        redisUtils.lpushAll(MediaRedisKeys.TRANSFER_FILE_QUEUE, addList, 0);
    }

    public VideoInfoFilePost getTransferVideoInfo4Queue() {
        return (VideoInfoFilePost) redisUtils.rpop(MediaRedisKeys.TRANSFER_FILE_QUEUE);
    }

    public VideoInfoFilePost getTransferVideoInfo4QueueBlock() {
        return (VideoInfoFilePost) redisUtils.brpop(MediaRedisKeys.TRANSFER_FILE_QUEUE,
                Constants.REDIS_QUEUE_BLOCK_SECONDS,
                TimeUnit.SECONDS);
    }
}
