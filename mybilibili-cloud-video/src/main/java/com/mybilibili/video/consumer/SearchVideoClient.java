package com.mybilibili.video.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoSearchCountUpdateDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoSearchResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * search 服务视频索引客户端。
 */
@FeignClient(contextId = "videoSearchVideoClient", name = Constants.CLOUD_SEARCH_NAME)
public interface SearchVideoClient {

    /**
     * 保存视频搜索文档。
     *
     * @param videoInfoDTO 视频索引源数据
     */
    @PostMapping(Constants.INNER_API_PREFIX + "/videoDoc/save")
    void saveVideoDoc(@RequestBody VideoInfoDTO videoInfoDTO);

    /**
     * 更新视频搜索文档。
     *
     * @param videoInfoDTO 视频索引源数据
     */
    @PostMapping(Constants.INNER_API_PREFIX + "/videoDoc/update")
    void updateVideoDoc(@RequestBody VideoInfoDTO videoInfoDTO);

    /**
     * 删除视频搜索文档。
     *
     * @param videoId 视频 id
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/videoDoc/delete")
    void deleteVideoDoc(@RequestParam("videoId") String videoId);

    /**
     * 按白名单排序类型更新视频搜索计数。
     *
     * @param countUpdateDTO 计数更新请求
     */
    @PostMapping(Constants.INNER_API_PREFIX + "/videoDoc/updateCount")
    void updateVideoCount(@RequestBody VideoSearchCountUpdateDTO countUpdateDTO);

    /**
     * 查询视频搜索结果，供视频详情页推荐模块复用。
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/searchVideo")
    PaginationResultVO<VideoSearchResultVO> searchVideo(@RequestParam("highlight") Boolean highlight,
                                                        @RequestParam("keyword") String keyword,
                                                        @RequestParam(value = "orderType", required = false) Integer orderType,
                                                        @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                        @RequestParam("pageSize") Integer pageSize);
}
