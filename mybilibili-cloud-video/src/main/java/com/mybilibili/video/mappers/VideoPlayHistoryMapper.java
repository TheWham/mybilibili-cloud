package com.mybilibili.video.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * @author amani
 * @since 2026/04/12
 * 视频播放历史
 */
public interface VideoPlayHistoryMapper<T, R> extends BaseMapper {

	/**
	 * 根据 UserIdAndVideoId查询
	 */
	T selectByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

	/**
	 * 根据 UserIdAndVideoId更新
	 */
	Integer updateByUserIdAndVideoId(@Param("bean") T t, @Param("userId") String userId, @Param("videoId") String videoId);

	/**
	 * 根据 UserIdAndVideoId删除
	 */
	Integer deleteByUserIdAndVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

	/**
	 * 根据 userId 删除这个用户的全部播放历史
	 */
	Integer deleteByUserId(@Param("userId") String userId);

	/**
	 * 删除早于指定时间的历史记录
	 */
	Integer deleteByLastUpdateTimeBefore(@Param("lastUpdateTime") Date lastUpdateTime);

}
