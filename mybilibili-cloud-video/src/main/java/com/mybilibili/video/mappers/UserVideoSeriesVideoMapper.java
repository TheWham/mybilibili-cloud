package com.mybilibili.video.mappers;


import com.mybilibili.common.mappers.BaseMapper;
import com.mybilibili.video.entity.dto.UserVideoSeriesVideoQueryDTO;
import com.mybilibili.video.entity.po.UserVideoSeriesVideo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author amani
 * @since 2026/03/18
 * 用户列表视频
 */
public interface UserVideoSeriesVideoMapper<T, R> extends BaseMapper {

	/**
	 * 根据 SeriesIdAndVideoId查询
	 */
	T selectBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

	/**
	 * 根据 SeriesIdAndVideoId更新
	 */
	Integer updateBySeriesIdAndVideoId(@Param("bean") T t, @Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

	/**
	 * 根据 SeriesIdAndVideoId删除
	 */
	Integer deleteBySeriesIdAndVideoId(@Param("seriesId") Integer seriesId, @Param("videoId") String videoId);

    Integer selectMaxSort(@Param("userId") String userId);

	List<String> selectVideoIdsBySeriesIdAndUserId(@Param("seriesId") Integer seriesId, @Param("userId") String userId);

	Integer deleteByIds(@Param("list") List<String> seriesVideoIds, @Param("userId") String userId,@Param("seriesId") Integer seriesId);

	List<UserVideoSeriesVideoQueryDTO> selectListLimit5(@Param("seriesId") Integer seriesId);
}
