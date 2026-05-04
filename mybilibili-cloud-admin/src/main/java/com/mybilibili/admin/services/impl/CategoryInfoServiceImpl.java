package com.mybilibili.admin.services.impl;


import com.mybilibili.admin.component.AdminRedisComponent;
import com.mybilibili.admin.mappers.CategoryInfoMapper;
import com.mybilibili.admin.services.CategoryInfoService;
import com.mybilibili.base.constants.Constants;
import com.mybilibili.admin.entity.po.CategoryInfo;
import com.mybilibili.base.entity.query.CategoryInfoQuery;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.base.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息
Service
 */

@Service("CategoryInfoService")
public class CategoryInfoServiceImpl implements CategoryInfoService {
	@Resource
	private CategoryInfoMapper<CategoryInfo, CategoryInfoQuery> categoryInfoMapper;
	@Resource
	private AdminRedisComponent adminRedisComponent;

	/**
	 * @description 根据条件查询
	 */
	@Override
	public List<CategoryInfo> findListByParam(CategoryInfoQuery param) {
		List<CategoryInfo> categoryList = this.categoryInfoMapper.selectList(param);

		if(categoryList.isEmpty())
			return Collections.emptyList();

		if (param != null && param.isConvert2Tree())
		{
			categoryList = addChildren(categoryList, Constants.ZERO);
		}
		return categoryList;
	}

	private List<CategoryInfo> addChildren(List<CategoryInfo> categoryList, Integer pCategoryId)
	{
		List<CategoryInfo> children = new ArrayList<>();
		for (var category : categoryList)
		{
			if (category.getCategoryId() != null && category.getPCategoryId() != null && category.getPCategoryId().equals(pCategoryId))
			{
				category.setChildren(addChildren(categoryList, category.getCategoryId()));
				children.add(category);
			}
		}
		return children;
	}

	/**
	 * @description 根据条件查询数量
	 */
	@Override
	public Integer findCountByParam(CategoryInfoQuery param) {
		return this.categoryInfoMapper.selectCount(param);
	}

	/**
	 * @description 分页查询
	 */
	@Override
	public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
		Integer count = this.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CategoryInfo> list = this.findListByParam(param);
		PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@Override
	public Integer add(CategoryInfo bean) {
		return this.categoryInfoMapper.insert(bean);
	}

	/**
	 * @description 批量新增
	 */
	@Override
	public Integer addBatch(List<CategoryInfo>  listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryInfoMapper.insertBatch(listBean);
	}

	/**
	 * @description 批量新增/修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CategoryInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.categoryInfoMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * @description 根据 CategoryId查询
	 */
	@Override
	public CategoryInfo getCategoryInfoByCategoryId(Integer categoryId) {
		return this.categoryInfoMapper.selectByCategoryId(categoryId);
	}

	/**
	 * @description 根据 CategoryId更新
	 */
	@Override
	public Integer updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId) {
		return this.categoryInfoMapper.updateByCategoryId(bean, categoryId);
	}

	/**
	 * @description 根据 CategoryId删除
	 */
	@Override
	public Integer deleteCategoryInfoByCategoryId(Integer categoryId) {
		return this.categoryInfoMapper.deleteByCategoryId(categoryId);
	}

	/**
	 * @description 根据categoryId递归删除
	 */
	@Override
	public void deleteCategory(Integer categoryId) {
		this.categoryInfoMapper.delCategoryRecursion(categoryId);
	}


	/**
	 * @description 根据 CategoryCode查询
	 */
	@Override
	public CategoryInfo getCategoryInfoByCategoryCode(String categoryCode) {
		return this.categoryInfoMapper.selectByCategoryCode(categoryCode);
	}

	/**
	 * @description 根据 CategoryCode更新
	 */
	@Override
	public Integer updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
		return this.categoryInfoMapper.updateByCategoryCode(bean, categoryCode);
	}

	/**
	 * @description 根据 CategoryCode删除
	 */
	@Override
	public Integer deleteCategoryInfoByCategoryCode(String categoryCode) {
		return this.categoryInfoMapper.deleteByCategoryCode(categoryCode);
	}

	@Override
	public void saveCategory(CategoryInfo categoryInfo) {
		CategoryInfo category = this.categoryInfoMapper.selectByCategoryCode(categoryInfo.getCategoryCode());

		if (category != null && category.getCategoryId() == null
		|| category != null && category.getCategoryId() != null && !category.getCategoryId().equals(categoryInfo.getCategoryId()))
			throw new BusinessException("分类编码已存在");

		if (categoryInfo.getCategoryId() == null) {
			Integer maxSort = categoryInfoMapper.getMaxSort(categoryInfo.getPCategoryId());
			categoryInfo.setSort(maxSort + 1);
			categoryInfoMapper.insert(categoryInfo);
		}else {
			categoryInfoMapper.updateByCategoryId(categoryInfo, categoryInfo.getCategoryId());
		}

	}

	@Override
	public void changeSort(Integer pCategoryId, List<Integer> categoryIds) {
		Integer sort = 0;
		List<CategoryInfo> updateCategoryList = new ArrayList<>();
		for (var categoryId : categoryIds)
		{
			CategoryInfo categoryInfo = new CategoryInfo();
			categoryInfo.setCategoryId(categoryId);
			categoryInfo.setPCategoryId(pCategoryId);
			categoryInfo.setSort(++sort);
			updateCategoryList.add(categoryInfo);
		}
		this.categoryInfoMapper.updateSortBatch(updateCategoryList);
	}

	@Override
	public List<CategoryInfo> loadAllCategory() {
		List<CategoryInfo> redisList = adminRedisComponent.getCategoryList();
		if (!redisList.isEmpty()) {
			return redisList;
		}
		flashCache();
		return adminRedisComponent.getCategoryList();
	}

	private void flashCache()
	{
		CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
		categoryInfoQuery.setOrderBy("sort asc");
		categoryInfoQuery.setConvert2Tree(true);
		List<CategoryInfo> categoryList = this.findListByParam(categoryInfoQuery);
		adminRedisComponent.saveCategoryList2Redis(categoryList);
	}

}
