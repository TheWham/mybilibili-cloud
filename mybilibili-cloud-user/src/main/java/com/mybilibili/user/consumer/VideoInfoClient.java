package com.mybilibili.user.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.SeriesWithVideoUHomeVO;
import com.mybilibili.base.entity.vo.VideoInfoUHomeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(Constants.CLOUD_VIDEO_NAME)
public interface VideoInfoClient {


    @RequestMapping(Constants.INNER_API_PREFIX + "/loadVideoList")
    PaginationResultVO<VideoInfoUHomeVO> loadVideoList(@RequestParam("userId") String userId,
                                                       @RequestParam(value = "type", required = false) Integer type,
                                                       @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                       @RequestParam(value = "videoName", required = false) String videoName,
                                                       @RequestParam(value = "orderType", required = false) Integer orderType);

    @RequestMapping(Constants.INNER_API_PREFIX + "/series/loadVideoSeriesWithVideo")
    List<SeriesWithVideoUHomeVO> loadVideoSeriesWithVideo(@RequestParam("userId") String userId);

}
