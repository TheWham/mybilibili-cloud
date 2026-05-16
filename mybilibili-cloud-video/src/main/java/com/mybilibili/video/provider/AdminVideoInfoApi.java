package com.mybilibili.video.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.base.entity.vo.AdminVideoInfoVO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.entity.po.VideoInfoFilePost;
import com.mybilibili.video.entity.po.VideoInfoPost;
import com.mybilibili.video.entity.query.VideoInfoFilePostQuery;
import com.mybilibili.video.entity.query.VideoInfoPostQuery;
import com.mybilibili.video.services.VideoInfoFilePostService;
import com.mybilibili.video.services.VideoInfoPostService;
import com.mybilibili.video.services.VideoInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class AdminVideoInfoApi {

    @Resource
    private VideoInfoPostService videoInfoPostService;

    @Resource
    private VideoInfoFilePostService videoInfoFilePostService;

    @Resource
    private VideoInfoService videoInfoService;
    @Resource
    private VideoRedisComponent videoRedisComponent;

    /**
     * 后台视频审核列表。
     *
     * <p>视频投稿、审核状态和播放计数字段都属于 video 服务。
     * admin 只拿稳定 VO，避免把 video 的 PO 暴露给后台门面服务。</p>
     */
    @RequestMapping("/admin/videoInfo/loadVideoList")
    public PaginationResultVO<AdminVideoInfoVO> loadVideoList(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                             @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                             @RequestParam(value = "videoNameFuzzy", required = false) String videoNameFuzzy,
                                                             @RequestParam(value = "recommendType", required = false) Integer recommendType,
                                                             @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                             @RequestParam(value = "pCategoryId", required = false) Integer pCategoryId) {
        VideoInfoPostQuery query = new VideoInfoPostQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setQueryCountInfo(true);
        query.setQueryUserInfo(true);
        query.setVideoNameFuzzy(videoNameFuzzy);
        query.setRecommendType(recommendType);
        query.setCategoryId(categoryId);
        query.setpCategoryId(pCategoryId);
        query.setOrderBy("v.last_update_time desc");

        PaginationResultVO<VideoInfoPost> page = videoInfoPostService.findListByPage(query);
        PaginationResultVO<AdminVideoInfoVO> result = copyPage(page, AdminVideoInfoVO.class);
        fillAiSubtitleIndexStatus(result.getList());
        return result;
    }

    @RequestMapping("/admin/videoInfo/loadVideoPList")
    public List<VideoInfoFilePostDTO> loadVideoPList(@RequestParam("videoId") String videoId) {
        VideoInfoFilePostQuery query = new VideoInfoFilePostQuery();
        query.setVideoId(videoId);
        query.setOrderBy("file_index asc");
        List<VideoInfoFilePost> filePostList = videoInfoFilePostService.findListByParam(query);
        return BeanUtil.copyToList(filePostList, VideoInfoFilePostDTO.class);
    }

    @RequestMapping("/admin/videoInfo/auditVideo")
    public void auditVideo(@RequestParam("videoId") String videoId,
                           @RequestParam("status") Integer status,
                           @RequestParam(value = "reason", required = false) String reason) {
        videoInfoService.auditVideo(videoId, status, reason);
    }

    @RequestMapping("/admin/videoInfo/recommendVideo")
    public void recommendVideo(@RequestParam("videoId") String videoId) {
        videoInfoService.recommendVideo(videoId);
    }

    @RequestMapping("/admin/videoInfo/deleteVideo")
    public void deleteVideo(@RequestParam("videoId") String videoId) {
        videoInfoFilePostService.deleVideo(videoId, null, true);
    }

    private <S, T> PaginationResultVO<T> copyPage(PaginationResultVO<S> sourcePage, Class<T> targetClass) {
        PaginationResultVO<T> targetPage = new PaginationResultVO<>();
        targetPage.setTotalCount(sourcePage.getTotalCount());
        targetPage.setPageSize(sourcePage.getPageSize());
        targetPage.setPageNo(sourcePage.getPageNo());
        targetPage.setPageTotal(sourcePage.getPageTotal());
        targetPage.setList(BeanUtil.copyToList(sourcePage.getList(), targetClass));
        return targetPage;
    }

    /**
     * 补充后台视频列表的 AI 字幕索引状态。
     *
     * <p>审核状态来自 MySQL，AI 状态来自异步 worker 写回的 Redis。两者分开展示，
     * 能避免 worker 没启动时被误判成审核失败。</p>
     */
    private void fillAiSubtitleIndexStatus(List<AdminVideoInfoVO> videoList) {
        if (videoList == null || videoList.isEmpty()) {
            return;
        }
        for (AdminVideoInfoVO videoInfo : videoList) {
            videoInfo.setAiSubtitleIndexStatus(videoRedisComponent.getAiSubtitleIndexStatus(videoInfo.getVideoId()));
        }
    }
}
