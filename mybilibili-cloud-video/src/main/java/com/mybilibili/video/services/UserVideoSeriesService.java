package com.mybilibili.video.services;

import com.mybilibili.base.entity.dto.UserVideoSeriesDTO;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.entity.dto.SeriesWithVideoQueryDTO;
import com.mybilibili.video.entity.po.UserVideoSeries;
import com.mybilibili.video.entity.po.VideoInfo;
import com.mybilibili.video.entity.query.UserVideoSeriesQuery;

import java.util.List;


/**
 * @author amani
 * @since 2026/03/18
 * 用户视频列表Service
 */
public interface UserVideoSeriesService {

	/**
	 * 根据条件查询
	 */
	List<UserVideoSeries> findListByParam(UserVideoSeriesQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(UserVideoSeriesQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserVideoSeries> findListByPage(UserVideoSeriesQuery param);

	/**
	 * 新增
	 */
	Integer add(UserVideoSeries bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserVideoSeries>  listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserVideoSeries> listBean);


	/**
	 * 根据 SeriesId查询
	 */
	UserVideoSeries getUserVideoSeriesBySeriesId(Integer seriesId);

	/**
	 * 根据 SeriesId更新
	 */
	Integer updateUserVideoSeriesBySeriesId(UserVideoSeries bean, Integer seriesId);

	/**
	 * 根据 SeriesId删除
	 */
	Integer deleteUserVideoSeriesBySeriesIdAndUserId(Integer seriesId, String userId);

	List<VideoInfo> selectAllVideoBySeriesIdAndUserId(Integer seriesId, String userId);

	void saveVideoSeries(Integer seriesId, String seriesName, String seriesDescription, String videoIds, String userId);

	void saveSeriesVideo(Integer seriesId,Integer sort, String videoIds, String userId);

	List<UserVideoSeriesDTO> loadVideoSeries(String userId);

	void changeVideoSeriesSort(String videoSeriesIds, String userId);

	Integer delVideoSeries(Integer seriesId, String userId);

	List<SeriesWithVideoQueryDTO> selectVideoSeriesWithVideo(String userId);
}
