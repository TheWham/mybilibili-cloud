package com.mybilibili.message.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "messageVideoInfoClient", name = Constants.CLOUD_VIDEO_NAME)
public interface VideoInfoClient {


    @GetMapping(Constants.INNER_API_PREFIX + "/message/selectVideoInfoByIds")
    List<VideoInfoDTO> selectVideoInfoByIds(@RequestParam("videoIds") List<String> videoIds);

}
