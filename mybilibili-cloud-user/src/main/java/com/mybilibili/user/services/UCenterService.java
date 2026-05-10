package com.mybilibili.user.services;

import com.mybilibili.base.entity.dto.VideoInfoDTO;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.VideoAuditCountVO;
import com.mybilibili.base.entity.vo.VideoCommentInUCenterVO;
import com.mybilibili.base.entity.vo.VideoDanmuVO;
import com.mybilibili.base.entity.vo.VideoInfoPostEditVO;

import java.util.List;

/**
 * 用户中心跨服务数据编排。
 */
public interface UCenterService {

    /**
     * 查询当前用户投稿视频列表。
     */
    PaginationResultVO<VideoInfoPostDTO> loadVideoList(Integer pageNo, String videoNameFuzzy, Integer status, String userId);

    /**
     * 查询当前用户各审核状态的视频数量。
     */
    VideoAuditCountVO getVideoCountInfo(String userId);

    /**
     * 查询投稿编辑页的视频详情。
     */
    VideoInfoPostEditVO getVideoByVideoId(String videoId, String userId);

    /**
     * 发布或更新投稿视频，用户 ID 在业务层统一补齐。
     */
    void postVideo(VideoInfoPostDTO videoInfoPostDTO, String userId);

    /**
     * 删除当前用户投稿视频。
     */
    void deleteVideo(String videoId, String userId);

    /**
     * 保存投稿视频的互动展示配置。
     */
    void saveVideoInteraction(String videoId, String interaction, String userId);

    /**
     * 查询当前用户全部投稿视频。
     */
    List<VideoInfoDTO> loadAllVideo(String userId);

    /**
     * 查询当前用户收到或管理范围内的评论。
     */
    PaginationResultVO<VideoCommentInUCenterVO> loadComment(Integer pageNo, Integer pageSize, String videoId, String userId);

    /**
     * 查询当前用户视频下的弹幕。
     */
    PaginationResultVO<VideoDanmuVO> loadDanmu(Integer pageNo, Integer pageSize, String videoId, String userId);

    /**
     * 删除评论时把当前用户 ID 传给 interact 服务做权限判断。
     */
    void delComment(Integer commentId, String userId);

    /**
     * 删除弹幕时把当前用户 ID 传给 interact 服务做权限判断。
     */
    void delDanmu(Integer danmuId, String userId);
}
