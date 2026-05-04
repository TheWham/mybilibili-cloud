package com.mybilibili.video.services;

import com.mybilibili.video.entity.po.UserVideoSeriesVideo;
import com.mybilibili.video.entity.query.UserVideoSeriesVideoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频Service
 */
public interface UserVideoSeriesVideoService {

	/**
	 * 根据条件查询
	 */
	List<UserVideoSeriesVideo> findListByParam(UserVideoSeriesVideoQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserVideoSeriesVideoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserVideoSeriesVideo> findListByPage(UserVideoSeriesVideoQuery param);

	/**
	 * 新增
	 */
	Integer add(UserVideoSeriesVideo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserVideoSeriesVideo>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserVideoSeriesVideo> listBean);


	/**
	 * 根据 SeriesIdAndVideoId查询
	 */
	UserVideoSeriesVideo getUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId);

	/**
	 * 根据 SeriesIdAndVideoId更新
	 */
	Integer updateUserVideoSeriesVideoBySeriesIdAndVideoId(UserVideoSeriesVideo bean, Integer seriesId, String videoId);

	/**
	 * 根据 SeriesIdAndVideoId删除
	 */
	Integer deleteUserVideoSeriesVideoBySeriesIdAndVideoId(Integer seriesId, String videoId);

}