package com.mybilibili.user.services;

import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.SeriesWithVideoUHomeVO;
import com.mybilibili.base.entity.vo.UserCollectionVO;
import com.mybilibili.base.entity.vo.UserVideoSeriesVO;
import com.mybilibili.base.entity.vo.VideoInfoUHomeVO;

import java.util.List;

/**
 * 用户主页跨服务数据编排。
 */
public interface UHomeService {

    /**
     * 查询用户主页的视频列表，实际数据仍由 video 服务维护。
     */
    PaginationResultVO<VideoInfoUHomeVO> loadVideoList(String userId, Integer type, Integer pageNo, String videoName, Integer orderType);

    /**
     * 查询用户主页展示的视频合集及合集内视频。
     */
    List<SeriesWithVideoUHomeVO> loadVideoSeriesWithVideo(String userId);

    /**
     * 查询用户创建的视频合集。
     */
    List<UserVideoSeriesVO> loadVideoSeries(String userId);

    /**
     * 查询用户公开视频收藏列表。
     */
    PaginationResultVO<UserCollectionVO> loadUserCollection(Integer pageNo, String userId);
}
