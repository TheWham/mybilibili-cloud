package com.mybilibili.search.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * video 服务索引源数据客户端。
 */
@FeignClient(contextId = "searchVideoInfoClient", name = Constants.CLOUD_VIDEO_NAME)
public interface VideoInfoClient {

    /**
     * 分页拉取正式视频数据，用于手动重建 ES 索引。
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 视频索引源数据
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/search/loadVideoIndexSource")
    PaginationResultVO<VideoInfoDTO> loadVideoIndexSource(@RequestParam("pageNo") Integer pageNo,
                                                          @RequestParam("pageSize") Integer pageSize);
}
