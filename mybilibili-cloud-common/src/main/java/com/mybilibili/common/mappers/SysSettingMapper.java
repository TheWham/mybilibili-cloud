package com.mybilibili.common.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @author amani
 * @since 2026/04/22
 * 
 */
public interface SysSettingMapper<T, R> extends BaseMapper {

	/**
	 * 根据 Id查询
	 */
	T selectById(@Param("id") Long id);

	/**
	 * 根据 Id更新
	 */
	Integer updateById(@Param("bean") T t, @Param("id") Long id);

	/**
	 * 根据 Id删除
	 */
	Integer deleteById(@Param("id") Long id);

}