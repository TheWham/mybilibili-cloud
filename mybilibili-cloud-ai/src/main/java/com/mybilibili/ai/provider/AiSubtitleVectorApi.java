package com.mybilibili.ai.provider;

import com.mybilibili.ai.service.AiSubtitleVectorService;
import com.mybilibili.base.constants.Constants;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 按分 P 文件 ID 删除字幕向量。
     *
     * <p>编辑审核通过后只删除被移除或被替换的分 P 向量，其他分 P 不受影响。</p>
     *
     * @param fileIds 分 P 文件 ID 列表
     */
    @PostMapping("/deleteByFileIds")
    public void deleteByFileIds(@RequestParam("fileIds") List<String> fileIds) {
        aiSubtitleVectorService.deleteByFileIds(fileIds);
    }

    /**
     * 更新字幕向量里的视频元数据。
     *
     * <p>视频文件没变时，审核通过只需要刷新标题、封面、标签等展示字段，不重新生成字幕向量。</p>
     *
     * @param videoId 视频 ID
     * @param videoName 视频标题
     * @param videoCover 视频封面
     * @param tags 视频标签
     */
    @PostMapping("/updateVideoMetaByVideoId")
    public void updateVideoMetaByVideoId(@RequestParam("videoId") String videoId,
                                         @RequestParam("videoName") String videoName,
                                         @RequestParam("videoCover") String videoCover,
                                         @RequestParam(value = "tags", required = false) String tags) {
        aiSubtitleVectorService.updateVideoMetaByVideoId(videoId, videoName, videoCover, tags);
    }
}
