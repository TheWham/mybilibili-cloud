package com.mybilibili.admin.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.base.entity.vo.AdminVideoInfoVO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "adminVideoClient", name = Constants.CLOUD_VIDEO_NAME)
public interface AdminVideoClient {

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/videoInfo/loadVideoList")
    PaginationResultVO<AdminVideoInfoVO> loadVideoList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                       @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                       @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy,
                                                       @RequestParam(value = "recommendType", required = false) Integer recommendType,
                                                       @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                       @RequestParam(value = "pCategoryId", required = false) Integer pCategoryId);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/videoInfo/loadVideoPList")
    List<VideoInfoFilePostDTO> loadVideoPList(@RequestParam("videoId") String videoId);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/videoInfo/auditVideo")
    void auditVideo(@RequestParam("videoId") String videoId,
                    @RequestParam("status") Integer status,
                    @RequestParam(value = "reason", required = false) String reason);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/videoInfo/recommendVideo")
    void recommendVideo(@RequestParam("videoId") String videoId);

    @RequestMapping(Constants.INNER_API_PREFIX + "/admin/videoInfo/deleteVideo")
    void deleteVideo(@RequestParam("videoId") String videoId);
}
