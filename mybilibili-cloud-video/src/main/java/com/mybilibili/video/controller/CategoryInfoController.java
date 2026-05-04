package com.mybilibili.video.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybilibili.base.entity.dto.CategoryDTO;
import com.mybilibili.video.entity.po.CategoryInfo;
import com.mybilibili.base.entity.query.CategoryInfoQuery;
import com.mybilibili.base.entity.query.SimplePage;
import com.mybilibili.base.entity.vo.PaginationResultVO;
import com.mybilibili.base.entity.vo.ResponseVO;
import com.mybilibili.base.enums.PageSize;
import com.mybilibili.common.controller.ABaseController;
import com.mybilibili.video.component.VideoRedisComponent;
import com.mybilibili.video.consumer.CategoryClient;
import com.mybilibili.video.services.CategoryInfoService;
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
	private CategoryClient categoryClient;

	@Resource
	private CategoryInfoService categoryService;

	@Resource
	private VideoRedisComponent videoRedisComponent;

	/**
	 * @description 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList (CategoryInfoQuery query) {
		return getSuccessResponseVO(categoryService.findListByPage(query));
	}

	/**
	 * @description 根据条件查询数量
	 */
	@RequestMapping("/findCountByParam")
	public Integer findCountByParam(CategoryInfoQuery param) {
		return this.categoryService.findCountByParam(param);
	}


	/**
	 * @description 分页查询
	 */
	@RequestMapping("/findListByPage")
	public PaginationResultVO<CategoryInfo> findListByPage(CategoryInfoQuery param) {
		Integer count = this.categoryService.findCountByParam(param);
		int pageSize = param.getPageSize()==null? PageSize.SIZE15.getSize():param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CategoryInfo> list = this.categoryService.findListByParam(param);
		PaginationResultVO<CategoryInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * @description 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(CategoryInfo bean) {
		categoryService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<CategoryInfo> listBean) {
		categoryService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * @description 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<CategoryInfo> listBean) {
		categoryService.addOrUpdateBatch(listBean);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/loadAllCategory")
	public ResponseVO loadAllCategory()
	{
		return getSuccessResponseVO(categoryClient.loadAllCategory());
	}

	@RequestMapping("/saveCategory")
	public ResponseVO saveCategory(@Validated CategoryDTO categoryDTO){

		CategoryInfo categoryInfo = BeanUtil.toBean(categoryDTO, CategoryInfo.class);
		categoryService.saveCategory(categoryInfo);
		flashCache();
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/delCategory")
	public ResponseVO delCategory(@NotNull Integer categoryId)
	{
		categoryService.deleteCategory(categoryId);
		flashCache();
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/changeSort")
	public ResponseVO changeSort(Integer pCategoryId, Integer[] categoryIds)
	{
		List<Integer> idsList = Arrays.asList(categoryIds);
		categoryService.changeSort(pCategoryId, idsList);
		flashCache();
		return getSuccessResponseVO(null);
	}

	private void flashCache()
	{
		CategoryInfoQuery categoryInfoQuery = new CategoryInfoQuery();
		categoryInfoQuery.setOrderBy("sort asc");
		categoryInfoQuery.setConvert2Tree(true);
		List<CategoryInfo> categoryList = this.categoryService.findListByParam(categoryInfoQuery);
		videoRedisComponent.saveCategoryList2Redis(categoryList);
	}


}
