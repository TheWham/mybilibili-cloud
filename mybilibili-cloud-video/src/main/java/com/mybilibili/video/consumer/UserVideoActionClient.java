package com.mybilibili.video.consumer;


import com.mybilibili.base.constants.Constants;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = Constants.CLOUD_INTERACT_NAME)
public interface UserVideoActionClient {
    //TODO 带接通收藏视频
}
