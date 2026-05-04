package com.mybilibili.video.services;

import com.mybilibili.video.entity.po.VideoPlayHistory;
import com.mybilibili.video.entity.query.VideoPlayHistoryQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.entity.vo.VideoHistoryVO;

import java.util.Date;
import java.util.List;


/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史Service
 */
public interface VideoPlayHistoryService {

	/**
	 * 根据条件查询
	 */
	List<VideoPlayHistory> findListByParam(VideoPlayHistoryQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(VideoPlayHistoryQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<VideoPlayHistory> findListByPage(VideoPlayHistoryQuery param);

	/**
	 * 加载用户播放历史页。
	 */
	PaginationResultVO<VideoHistoryVO> loadHistoryByPage(String userId, Integer pageNo);

	/**
	 * 新增
	 */
	Integer add(VideoPlayHistory bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<VideoPlayHistory>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<VideoPlayHistory> listBean);


	/**
	 * 根据 UserIdAndVideoId查询
	 */
	VideoPlayHistory getVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);

	/**
	 * 根据 UserIdAndVideoId更新
	 */
	Integer updateVideoPlayHistoryByUserIdAndVideoId(VideoPlayHistory bean, String userId, String videoId);

	/**
	 * 根据 UserIdAndVideoId删除
	 */
	Integer deleteVideoPlayHistoryByUserIdAndVideoId(String userId, String videoId);

	/**
	 * 根据 userId 删除当前用户全部播放历史
	 */
	Integer deleteVideoPlayHistoryByUserId(String userId);

	/**
	 * 删除早于指定时间的历史记录
	 */
	Integer deleteVideoPlayHistoryBefore(Date lastUpdateTime);

    Integer delHistory(String videoId, String userId);

	Integer cleanHistory(String userId);
}
