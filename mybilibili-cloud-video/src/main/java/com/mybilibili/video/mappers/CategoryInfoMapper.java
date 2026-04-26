package com.mybilibili.video.mappers;

import com.mybilibili.common.mappers.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息

 */
public interface CategoryInfoMapper<T, R> extends BaseMapper {

	/**
	 * @description 根据 CategoryId查询
	 */
	T selectByCategoryId(@Param("categoryId") Integer categoryId);

	/**
	 * @description 根据 CategoryId更新
	 */
	Integer updateByCategoryId(@Param("bean") T t, @Param("categoryId") Integer categoryId);

	/**
	 * @description 根据 CategoryId删除
	 */
	Integer deleteByCategoryId(@Param("categoryId") Integer categoryId);


	/**
	 * @description 根据 CategoryCode查询
	 */
	T selectByCategoryCode(@Param("categoryCode") String categoryCode);

	/**
	 * @description 根据 CategoryCode更新
	 */
	Integer updateByCategoryCode(@Param("bean") T t, @Param("categoryCode") String categoryCode);

	/**
	 * @description 根据 CategoryCode删除
	 */
	Integer deleteByCategoryCode(@Param("categoryCode") String categoryCode);

	/**
	 * @description 获取最大排序数值
	 */

	Integer getMaxSort(@Param("pCategoryId") Integer pCategoryId);

	void delCategoryRecursion(@Param("categoryIdOrPCategoryId") Integer categoryIdOrPcategoryId);

	void updateSortBatch(@Param("categoryList") List<T> categoryList);

}