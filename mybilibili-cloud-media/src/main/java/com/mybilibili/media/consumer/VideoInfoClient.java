package com.mybilibili.media.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(Constants.CLOUD_VIDEO_NAME)
public interface VideoInfoClient {

    @RequestMapping(Constants.INNER_API_PREFIX + "/getFilePostDTO")
    VideoInfoFilePostDTO getFilePostDTO(@RequestParam("fileId") String fileId);
}
