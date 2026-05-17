package com.mybilibili.video.consumer;

import com.mybilibili.base.constants.Constants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * AI 字幕向量服务内部客户端。
 *
 * <p>video 只关心视频删除时清理向量索引，不直接依赖 ai 模块的实现类。</p>
 */
@FeignClient(Constants.CLOUD_AI_NAME)
public interface AiSubtitleVectorClient {

    @PostMapping(Constants.INNER_API_PREFIX + "/aiSubtitleVector/deleteByVideoId")
    void deleteByVideoId(@RequestParam("videoId") String videoId);

    @PostMapping(Constants.INNER_API_PREFIX + "/aiSubtitleVector/deleteByFileIds")
    void deleteByFileIds(@RequestParam("fileIds") List<String> fileIds);

    @PostMapping(Constants.INNER_API_PREFIX + "/aiSubtitleVector/updateVideoMetaByVideoId")
    void updateVideoMetaByVideoId(@RequestParam("videoId") String videoId,
                                  @RequestParam("videoName") String videoName,
                                  @RequestParam("videoCover") String videoCover,
                                  @RequestParam(value = "tags", required = false) String tags);
}
