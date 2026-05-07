package com.mybilibili.interact.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * video 服务视频信息客户端。
 *
 * <p>interact 只保存用户行为，不直接依赖 video 模块的 Service。
 * 需要补齐视频标题、封面等展示字段时，通过内部接口按 videoId 批量查询。</p>
 */
@FeignClient(contextId = "interactVideoInfoClient", name = Constants.CLOUD_VIDEO_NAME)
public interface VideoInfoClient {

    /**
     * 根据视频 id 列表查询收藏页需要展示的视频信息。
     *
     * @param videoIds 视频 id 列表
     * @return 收藏页视频基础信息
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/collection/loadVideoInfo")
    List<UserCollectionVO> loadCollectionVideoInfo(@RequestParam("videoIds") List<String> videoIds);

    /**
     * 根据视频 id 查询视频信息。
     *
     * @param videoId 视频 id
     * @return 视频状态 以及评论区是否开启
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/checkVideoCommentStatusByVideoId")
    Boolean checkVideoCommentStatusByVideoId(@RequestParam("videoId") String videoId);
}
