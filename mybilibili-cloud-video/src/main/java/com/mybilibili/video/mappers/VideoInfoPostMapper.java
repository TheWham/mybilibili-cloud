package com.mybilibili.video.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @date 2026/02/13
 * @description 视频信息
 */
public interface VideoInfoPostMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 VideoId查询
	 */
	T selectByVideoId(@Param("videoId") String videoId);

	/**
	 * @description 根据 VideoId更新
	 */
	Integer updateByVideoId(@Param("bean") T t, @Param("videoId") String videoId);

	/**
	 * @description 根据 VideoId删除
	 */
	Integer deleteByVideoId(@Param("videoId") String videoId);


    Integer updateByCondition(@Param("bean")T updateInfoPost,@Param("query") R postQuery);
}