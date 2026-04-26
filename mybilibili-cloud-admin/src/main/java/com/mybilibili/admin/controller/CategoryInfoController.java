package com.mybilibili.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.admin.services.CategoryInfoService;
import com.mybilibili.base.entity.dto.CategoryDTO;
import com.mybilibili.base.entity.po.CategoryInfo;
import com.mybilibili.base.entity.query.CategoryInfoQuery;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.common.component.RedisComponent;
import com.mybilibili.common.controller.ABaseController;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息
Service
 */

@RestController
@RequestMapping("/category")
@Validated
public class CategoryInfoController extends ABaseController {
	private static final Logger log = LoggerFactory.getLogger(CategoryInfoController.class);
	@Resource
	private CategoryInfoService categoryInfoService;

	@Resource
	private RedisComponent redisComponent;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList (CategoryInfoQuery query) {
		return getSuccessResponseVO(categoryInfoService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("/findCountByParam")
	public Integer findCountByParam(CategoryInfoQuery param) {
		return this.categoryInfoService.findCountByParam(param);
	}

	/**
	 * @description 分页查询
	 */
	@RequestMapping("/findListByPage")
	public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
		Integer count = this.categoryInfoService.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CategoryInfo> list = this.categoryInfoService.findListByParam(param);
		PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(CategoryInfo bean) {
		categoryInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<CategoryInfo> listBean) {
		categoryInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<CategoryInfo> listBean) {
		categoryInfoService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 CategoryId查询
	 */
	@RequestMapping("/getCategoryInfoByCategoryId")
	public ResponseVO getCategoryInfoByCategoryId(Integer categoryId) {
		return getSuccessResponseVO(this.categoryInfoService.getCategoryInfoByCategoryId(categoryId));
	}

	/**
	 * @description 根据 CategoryId更新
	 */
	@RequestMapping("/updateCategoryInfoByCategoryId")
	public ResponseVO updateCategoryInfoByCategoryId(CategoryInfo bean, Integer categoryId) {
		this.categoryInfoService.updateCategoryInfoByCategoryId(bean, categoryId);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 CategoryId删除
	 */
	@RequestMapping("/deleteCategoryInfoByCategoryId")
	public ResponseVO deleteCategoryInfoByCategoryId(Integer categoryId) {
		this.categoryInfoService.deleteCategoryInfoByCategoryId(categoryId);
		return getSuccessResponseVO(null);
	}


	/**
	 * @description 根据 CategoryCode查询
	 */
	@RequestMapping("/getCategoryInfoByCategoryCode")
	public ResponseVO getCategoryInfoByCategoryCode(String categoryCode) {
		return getSuccessResponseVO(this.categoryInfoService.getCategoryInfoByCategoryCode(categoryCode));
	}

	/**
	 * @description 根据 CategoryCode更新
	 */
	@RequestMapping("/updateCategoryInfoByCategoryCode")
	public ResponseVO updateCategoryInfoByCategoryCode(CategoryInfo bean, String categoryCode) {
		this.categoryInfoService.updateCategoryInfoByCategoryCode(bean, categoryCode);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 根据 CategoryCode删除
	 */
	@RequestMapping("/deleteCategoryInfoByCategoryCode")
	public ResponseVO deleteCategoryInfoByCategoryCode(String categoryCode) {
		this.categoryInfoService.deleteCategoryInfoByCategoryCode(categoryCode);
		return getSuccessResponseVO(null);
	}


	@RequestMapping("/loadCategory")
	public ResponseVO loadCategory()
	{
		List<com.mybilibili.base.entity.po.CategoryInfo> redisList = redisComponent.getCategoryList();
		if (!redisList.isEmpty()) {
			log.info("走的缓存");
			return getSuccessResponseVO(redisList);
		}
		flashCache();
		return getSuccessResponseVO(redisComponent.getCategoryList());
	}

	@RequestMapping("/saveCategory")
	public ResponseVO saveCategory(@Validated CategoryDTO categoryDTO){

		CategoryInfo categoryInfo = BeanUtil.toBean(categoryDTO, CategoryInfo.class);
		categoryInfoService.saveCategory(categoryInfo);
		flashCache();
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/delCategory")
	public ResponseVO delCategory(@NotNull Integer categoryId)
	{
		categoryInfoService.deleteCategory(categoryId);
		flashCache();
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/changeSort")
	public ResponseVO changeSort(Integer pCategoryId, Integer[] categoryIds)
	{
		List<Integer> idsList = Arrays.asList(categoryIds);
		categoryInfoService.changeSort(pCategoryId, idsList);
		flashCache();
		return getSuccessResponseVO(null);
	}

	private void flashCache()
	{
		CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
		categoryInfoQuery.setOrderBy("sort asc");
		categoryInfoQuery.setConvert2Tree(true);
		List<com.mybilibili.base.entity.po.CategoryInfo> categoryList = this.categoryInfoService.findListByParam(categoryInfoQuery);
		redisComponent.saveCategoryList2Redis(categoryList);
	}

}
