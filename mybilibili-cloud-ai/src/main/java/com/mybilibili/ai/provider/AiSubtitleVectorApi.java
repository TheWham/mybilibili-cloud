package com.mybilibili.ai.provider;

import com.mybilibili.ai.service.AiSubtitleVectorService;
import com.mybilibili.base.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 字幕向量内部接口。
 *
 * <p>video 服务只通过 Feign 调用这里的稳定契约，具体的 ES delete-by-query 细节仍然留在
 * ai 服务内部。这样后面如果把向量库从 ES 换成别的存储，video 模块不用跟着改。</p>
 *
 * @author amani
 * @since 2026/05/16
 */
@RestController
@RequestMapping(Constants.INNER_API_PREFIX + "/aiSubtitleVector")
public class AiSubtitleVectorApi {

    @Resource
    private AiSubtitleVectorService aiSubtitleVectorService;

    /**
     * 删除指定视频的字幕向量索引。
     *
     * <p>审核通过后会先清理旧向量，再投递新的字幕向量化任务；视频删除时也会调用这个接口清理读侧数据。</p>
     *
     * @param videoId 视频 ID
     */
    @PostMapping("/deleteByVideoId")
    public void deleteByVideoId(@RequestParam("videoId") String videoId) {
        aiSubtitleVectorService.deleteByVideoId(videoId);
    }
}
