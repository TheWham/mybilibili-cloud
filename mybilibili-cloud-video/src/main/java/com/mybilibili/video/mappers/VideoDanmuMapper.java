package com.mybilibili.video.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @date 2026/03/09
 * @description 视频弹幕
 */
public interface VideoDanmuMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 DanmuId查询
	 */
	T selectByDanmuId(@Param("danmuId") Integer danmuId);

	/**
	 * @description 根据 DanmuId更新
	 */
	Integer updateByDanmuId(@Param("bean") T t, @Param("danmuId") Integer danmuId);

	/**
	 * @description 根据 DanmuId删除
	 */
	Integer deleteByDanmuId(@Param("danmuId") Integer danmuId);

}