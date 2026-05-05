package com.mybilibili.media.component;

import com.alibaba.fastjson2.JSON;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.SysSettingDTO;
import com.mybilibili.base.entity.dto.UploadingFileDTO;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.common.consumer.AdminSysSettingClient;
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
    @Resource
    private AdminSysSettingClient adminSysSettingClient;

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

    public void addFileList2TransferQueue(List<VideoInfoFilePostDTO> addList) {
        redisUtils.lpushAll(MediaRedisKeys.TRANSFER_FILE_QUEUE, addList, 0);
    }

    public VideoInfoFilePostDTO getTransferVideoInfo4Queue() {
        return (VideoInfoFilePostDTO) redisUtils.rpop(MediaRedisKeys.TRANSFER_FILE_QUEUE);
    }

    public VideoInfoFilePostDTO getTransferVideoInfo4QueueBlock() {
        return (VideoInfoFilePostDTO) redisUtils.brpop(MediaRedisKeys.TRANSFER_FILE_QUEUE,
                Constants.REDIS_QUEUE_BLOCK_SECONDS,
                TimeUnit.SECONDS);
    }

    public SysSettingDTO getSysSetting() {
        Object sysSetting = redisUtils.get(Constants.REDIS_SYS_SETTING_KEY);
        if (sysSetting instanceof SysSettingDTO dto) {
            return dto;
        }
        if (sysSetting != null) {
            return JSON.parseObject(JSON.toJSONString(sysSetting), SysSettingDTO.class);
        }

        SysSettingDTO sysSettingDTO;
        try {
            sysSettingDTO = adminSysSettingClient.getSysSetting();
        } catch (Exception e) {
            // media 不能直接查 sys_setting，admin 暂不可用时先按默认上传限制兜底。
            return SysSettingDTO.createDefault();
        }
        if (sysSettingDTO == null) {
            return SysSettingDTO.createDefault();
        }
        redisUtils.set(Constants.REDIS_SYS_SETTING_KEY, sysSettingDTO);
        return sysSettingDTO;
    }
}
