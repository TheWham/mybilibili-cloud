package com.mybilibili.user.consumer;

import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoCountDTO;
import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * video 服务主页数据客户端。
 *
 * <p>这里使用 contextId 标识客户端职责，name 仍然使用服务名。
 * 同一个应用后续如果再接入 video 服务的其他 FeignClient，也不会因为服务名相同
 * 生成重复的 FeignClientSpecification。</p>
 */
@FeignClient(contextId = "userVideoInfoClient", name = Constants.CLOUD_VIDEO_NAME)
public interface VideoInfoClient {

    /**
     * 查询用户主页视频列表。
     *
     * @param userId 用户 id
     * @param type 列表类型，空值按 video 服务默认规则处理
     * @param pageNo 页码，空值时使用默认页码
     * @param videoName 视频名称筛选条件
     * @param orderType 排序类型
     * @return 用户主页视频分页数据
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/loadVideoList")
    PaginationResultVO<VideoInfoUHomeVO> loadVideoList(@RequestParam("userId") String userId,
                                                       @RequestParam(value = "type", required = false) Integer type,
                                                       @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                       @RequestParam(value = "videoName", required = false) String videoName,
                                                       @RequestParam(value = "orderType", required = false) Integer orderType);

    /**
     * 查询用户主页展示用的视频合集，并携带合集内的部分视频。
     *
     * @param userId 用户 id
     * @return 合集及合集视频列表
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/series/loadVideoSeriesWithVideo")
    List<SeriesWithVideoUHomeVO> loadVideoSeriesWithVideo(@RequestParam("userId") String userId);

    /**
     * 查询用户创建的视频合集列表。
     *
     * @param userId 用户 id
     * @return 视频合集列表
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/series/loadVideoSeries")
    List<UserVideoSeriesVO> loadVideoSeries(@RequestParam("userId") String userId);

    /**
     * 查询用户创建的视频收藏列表。
     *
     * @param userId 用户 id
     * @return 视频收藏列表
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/loadUserCollection")
    PaginationResultVO<UserCollectionVO> loadUserCollection(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                              @RequestParam("userId") String userId);


    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/loadVideoList")
    PaginationResultVO<VideoInfoPostDTO> loadUCenterVideoList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy,
                                                              @RequestParam(value = "status", required = false) Integer status,
                                                              @RequestParam("userId") String userId);

    /**
     * 获取视频所处各自状态的数量
     * @return
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/getVideoCountInfo")
    VideoAuditCountVO getVideoCountInfo(@RequestParam("userId") String userId);

    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/getVideoByVideoId")
    VideoInfoPostEditVO getVideoByVideoId(@RequestParam("videoId") String videoId,
                                          @RequestParam("userId") String userId);

    @PostMapping(Constants.INNER_API_PREFIX + "/ucenter/postVideo")
    void postVideo(@RequestBody VideoInfoPostDTO videoInfoPostDTO);

    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/deleteVideo")
    void deleteVideo(@RequestParam("videoId") String videoId,
                     @RequestParam("userId") String userId);

    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/saveVideoInteraction")
    void saveVideoInteraction(@RequestParam("videoId") String videoId,
                              @RequestParam("userId") String userId,
                              @RequestParam("interaction") String interaction);

    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/loadAllVideo")
    List<VideoInfoDTO> loadAllVideo(@RequestParam("userId") String userId);

    @GetMapping(Constants.INNER_API_PREFIX + "/ucenter/getVideoListByIds")
    List<VideoInfoDTO> getVideoListByIds(@RequestParam("videoIds") List<String> videoIds, @RequestParam("userId") String userId);

    /**
     * 汇总用户作为 UP 主收到的视频播放量和点赞量。
     *
     * <p>统计口径由 video 服务统一维护，user 服务只通过 Feign 获取结果。</p>
     *
     * @param userId UP 主用户 id
     * @return 视频统计汇总
     */
    @GetMapping(Constants.INNER_API_PREFIX + "/countVideoInfoByUserId")
    VideoCountDTO countVideoInfoByUserId(@RequestParam("userId") String userId);

}
