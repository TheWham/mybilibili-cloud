package com.mybilibili.video.consumer;

import com.mybilibili.base.constants.Constants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * AI 字幕向量服务内部客户端。
 *
 * <p>video 只关心视频删除时清理向量索引，不直接依赖 ai 模块的实现类。</p>
 */
@FeignClient(Constants.CLOUD_AI_NAME)
public interface AiSubtitleVectorClient {

    @PostMapping(Constants.INNER_API_PREFIX + "/aiSubtitleVector/deleteByVideoId")
    void deleteByVideoId(@RequestParam("videoId") String videoId);
}
