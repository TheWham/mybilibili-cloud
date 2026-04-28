package com.mybilibili.user.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author amani
 * @since 2026/03/21
 * 用户数量统计表
 */
public interface UserStatsMapper<T, R> extends BaseMapper {

	/**
	 * 根据 UserId查询
	 */
	T selectByUserId(@Param("userId") String userId);

	/**
	 * 取用户最近一天的统计，给首页这类兜底场景用。
	 */
	T selectLatestByUserId(@Param("userId") String userId);

	/**
	 * 查询全站最近一天有统计数据的日期。
	 */
	Date selectLatestStatsDay();

	/**
	 * 按日期汇总全站统计数据。
	 */
	T selectSummaryByStatsDay(@Param("statsDay") Date statsDay);

	/**
	 * 查询一段时间内每天的全站汇总统计。
	 */
	List<T> selectSummaryListByDateRange(@Param("startDay") Date startDay, @Param("endDay") Date endDay);

	/**
	 * 根据 UserId更新
	 */
	Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

	/**
	 * 根据 UserId删除
	 */
	Integer deleteByUserId(@Param("userId") String userId);

	Integer insertOrUpdateCount(@Param("userId") String userId,@Param("field") String field,@Param("count") int count);
}
