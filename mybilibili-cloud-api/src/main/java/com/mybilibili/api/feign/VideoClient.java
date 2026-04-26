package com.mybilibili.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mybilibili-cloud-video")
public interface VideoClient {
    @RequestMapping("/loadFeign")
    String loadFeign();
}
