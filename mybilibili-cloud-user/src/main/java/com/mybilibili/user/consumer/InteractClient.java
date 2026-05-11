package com.mybilibili.user.consumer;


import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.UserInteractCountDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoCommentInUCenterVO;
import com.mybilibili.base.entity.vo.VideoDanmuVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "userInteractClient", name = Constants.CLOUD_INTERACT_NAME)
public interface InteractClient {

    @GetMapping(Constants.INNER_API_PREFIX + "/loadComment")
    PaginationResultVO<VideoCommentInUCenterVO> loadComment(
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "videoId", required = false) String videoId,
            @RequestParam("userId") String userId
    );


    @GetMapping(Constants.INNER_API_PREFIX + "/loadCommentInUCenter")
    PaginationResultVO<VideoCommentInUCenterVO> loadCommentInUCenter(
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "videoId", required = false) String videoId,
            @RequestParam("userId") String userId
    );

    @GetMapping(Constants.INNER_API_PREFIX + "/loadDanmu")
    PaginationResultVO<VideoDanmuVO> loadDanmu(
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "videoId", required = false) String videoId,
            @RequestParam("userId") String userId
    );

    @GetMapping(Constants.INNER_API_PREFIX + "/delComment")
    void delComment(@RequestParam("commentId") Integer commentId,
                    @RequestParam("userId") String userId);

    @GetMapping(Constants.INNER_API_PREFIX + "/delDanmu")
    void delDanmu(@RequestParam("danmuId") Integer danmuId,
                  @RequestParam("userId") String userId);

    /**
     * 汇总用户作为 UP 主收到的评论、弹幕、投币和收藏数据。
     *
     * @param userId UP 主用户 id
     * @return 互动统计汇总
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/countUserInteractByUserId")
    UserInteractCountDTO countUserInteractByUserId(@RequestParam("userId") String userId);

}
