package com.mybilibili.admin.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoCommentInUCenterVO;
import com.mybilibili.base.entity.vo.VideoDanmuVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "adminInteractClient", name = Constants.CLOUD_INTERACT_NAME)
public interface AdminInteractClient {

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/interact/loadComment")
    PaginationResultVO<VideoCommentInUCenterVO> loadComment(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                            @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                            @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/interact/loadDanmu")
    PaginationResultVO<VideoDanmuVO> loadDanmu(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                               @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                               @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/interact/delComment")
    void delComment(@RequestParam("commentId") Integer commentId);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/interact/delDanmu")
    void delDanmu(@RequestParam("danmuId") Integer danmuId);
}
