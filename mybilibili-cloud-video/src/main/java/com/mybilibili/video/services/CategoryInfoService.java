package com.mybilibili.video.services;


import com.mybilibili.base.entity.dto.CategoryInfoVO;
import com.mybilibili.base.entity.query.CategoryInfoQuery;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.video.entity.po.CategoryInfo;

import java.util.List;


/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息
Service
 */
public interface CategoryInfoService {

	/**
	 * @description 根据条件查询
	 */
	List<CategoryInfo> findListByParam(CategoryInfoQuery param);

	/**
	 * @description 根据条件查询数量
	 */
	Integer findCountByParam(CategoryInfoQuery param);

	/**
	 * @description 分页查询
	 */
	PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param);

	/**
	 * @description 新增
	 */
	Integer add(CategoryInfo bean);

	/**
	 * @description 批量新增
	 */
	Integer addBatch(List<CategoryInfo>  listBean);

	/**
	 * @description 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CategoryInfo> listBean);


	/**
	 * @description 根据 CategoryId查询
	 */
	CategoryInfo getCategoryInfoByCategoryId(Integer categoryId);

	/**
	 * @description 根据 CategoryId更新
	 */
	Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId);

	/**
	 * @description 根据 CategoryId删除
	 */
	Integer deleteCategoryInfoByCategoryId(Integer categoryId);

	/**
	 * @description 根据 CategoryId删除
	 */
	void deleteCategory(Integer categoryId);

	/**
	 * @description 根据 CategoryCode查询
	 */
	CategoryInfo getCategoryInfoByCategoryCode(String categoryCode);

	/**
	 * @description 根据 CategoryCode更新
	 */
	Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode);

	/**
	 * @description 根据 CategoryCode删除
	 */
	Integer deleteCategoryInfoByCategoryCode(String categoryCode);

	void saveCategory(CategoryInfo categoryInfo);

	void changeSort(Integer pCategoryId, List<Integer> categoryIds);

	/**
	 * 从 admin 服务读取前台可用的分类数据。
	 */
	List<CategoryInfoVO> loadAllCategoryFromAdmin();
}
