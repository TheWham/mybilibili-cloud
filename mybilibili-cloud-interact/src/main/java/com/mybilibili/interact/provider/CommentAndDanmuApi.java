package com.mybilibili.interact.provider;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoCommentVO;
import com.mybilibili.base.entity.vo.VideoDanmuVO;
import com.mybilibili.interact.entity.po.VideoComment;
import com.mybilibili.interact.entity.po.VideoDanmu;
import com.mybilibili.interact.entity.query.VideoCommentQuery;
import com.mybilibili.interact.entity.query.VideoDanmuQuery;
import com.mybilibili.interact.services.VideoCommentService;
import com.mybilibili.interact.services.VideoDanmuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.INNER_API_PREFIX)
public class CommentAndDanmuApi {

    @Resource
    private VideoCommentService videoCommentService;

    @Resource
    private VideoDanmuService videoDanmuService;

    /**
     * 用户中心评论列表。
     *
     * <p>只返回 base 模块中的展示 VO，user 服务不需要知道 interact 的评论 PO。</p>
     */
    @RequestMapping("/loadComment")
    public PaginationResultVO<VideoCommentVO> loadComment(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                          @RequestParam(value = "videoId", required = false) String videoId,
                                                          @RequestParam("userId") String userId) {
        VideoCommentQuery query = new VideoCommentQuery();
        query.setVideoId(videoId);
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(userId);
        query.setQueryChildren(false);
        query.setQueryUserInfo(true);
        query.setOrderBy("v.comment_id desc");

        PaginationResultVO<VideoComment> page = videoCommentService.findListByPage(query);
        return copyPage(page, VideoCommentVO.class);
    }

    /**
     * 用户中心弹幕列表。
     */
    @RequestMapping("/loadDanmu")
    public PaginationResultVO<VideoDanmuVO> loadDanmu(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                      @RequestParam(value = "videoId", required = false) String videoId,
                                                      @RequestParam("userId") String userId) {
        VideoDanmuQuery query = new VideoDanmuQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(userId);
        query.setVideoId(videoId);
        query.setQueryUserInfo(true);
        query.setOrderBy("v.time asc");

        PaginationResultVO<VideoDanmu> page = videoDanmuService.findListByPage(query);
        return copyPage(page, VideoDanmuVO.class);
    }

    /**
     * 删除当前用户自己的评论。
     */
    @RequestMapping("/delComment")
    public void delComment(@RequestParam("commentId") Integer commentId,
                           @RequestParam("userId") String userId) {
        videoCommentService.deleteByCommentId(commentId, false, userId);
    }

    /**
     * 删除当前用户自己的弹幕。
     */
    @RequestMapping("/delDanmu")
    public void delDanmu(@RequestParam("danmuId") Integer danmuId,
                         @RequestParam("userId") String userId) {
        videoDanmuService.deleteVideoDanmuByDanmuId(danmuId, false, userId);
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
}
