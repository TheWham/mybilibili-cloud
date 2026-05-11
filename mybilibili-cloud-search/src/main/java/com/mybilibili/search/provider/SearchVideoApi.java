package com.mybilibili.search.provider;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoSearchCountUpdateDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoSearchResultVO;
import com.mybilibili.search.service.VideoEsService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * search 服务内部视频索引接口。
 */
@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class SearchVideoApi {

    @Resource
    private VideoEsService videoEsService;

    @PostMapping("/videoDoc/save")
    public void saveVideoDoc(@RequestBody VideoInfoDTO videoInfoDTO) {
        videoEsService.saveDoc(videoInfoDTO);
    }

    @PostMapping("/videoDoc/update")
    public void updateVideoDoc(@RequestBody VideoInfoDTO videoInfoDTO) {
        videoEsService.updateDoc(videoInfoDTO);
    }

    @GetMapping("/videoDoc/delete")
    public void deleteVideoDoc(@RequestParam("videoId") String videoId) {
        videoEsService.deleteDoc(videoId);
    }

    @PostMapping("/videoDoc/updateCount")
    public void updateVideoCount(@RequestBody VideoSearchCountUpdateDTO countUpdateDTO) {
        videoEsService.updateCount(countUpdateDTO);
    }

    @GetMapping("/searchVideo")
    public PaginationResultVO<VideoSearchResultVO> searchVideo(@RequestParam("highlight") Boolean highlight,
                                                               @RequestParam("keyword") @NotEmpty String keyword,
                                                               @RequestParam(value = "orderType", required = false) Integer orderType,
                                                               @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                               @RequestParam("pageSize") Integer pageSize) {
        return videoEsService.search(highlight, keyword, orderType, pageNo, pageSize);
    }

    @GetMapping("/rebuildVideoIndex")
    public void rebuildVideoIndex() {
        videoEsService.rebuildVideoIndex();
    }
}
