package com.mybilibili.base.entity.po;

import java.io.Serializable;
import java.util.List;

/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息

 */
public class CategoryInfo implements Serializable {
	/**
	 * @description 自增分类id
	 */
    private Integer categoryId;

	/**
	 * @description 分类名称
	 */
    private String categoryName;

	/**
	 * @description 分类编码
	 */
    private String categoryCode;

	/**
	 * @description 父级分类id

	 */
    private Integer pCategoryId;

	/**
	 * @description 分类背景图
	 */
    private String background;

	/**
	 * @description 分类名称图标
	 */
    private String icon;

	/**
	 * @description 分类排列优先级
	 */
    private Integer sort;

	/**
	 * @description 子分类
	 */

	private List<CategoryInfo> children;

	/**
	 * @description 删除子分类
	 */
	private Integer categoryIdOrPCategoryId;

	public Integer getCategoryIdOrPCategoryId() {
		return categoryIdOrPCategoryId;
	}

	public void setCategoryIdOrPCategoryId(Integer categoryIdOrPCategoryId) {
		this.categoryIdOrPCategoryId = categoryIdOrPCategoryId;
	}

	public Integer getpCategoryId() {
		return pCategoryId;
	}

	public void setpCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
	}

	public List<CategoryInfo> getChildren() {
		return children;
	}

	public void setChildren(List<CategoryInfo> children) {
		this.children = children;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}
	public Integer getCategoryId() {
		return this.categoryId;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getCategoryName() {
		return this.categoryName;
	}
	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}
	public String getCategoryCode() {
		return this.categoryCode;
	}
	public void setPCategoryId(Integer pCategoryId) {
		this.pCategoryId = pCategoryId;
	}
	public Integer getPCategoryId() {
		return this.pCategoryId;
	}
	public void setBackground(String background) {
		this.background = background;
	}
	public String getBackground() {
		return this.background;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getIcon() {
		return this.icon;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public Integer getSort() {
		return this.sort;
	}

	@Override
	public String toString() {
		return "CategoryInfo{" +
				"categoryId='" + categoryId + 
				", categoryName='" + categoryName + '\'' + 
				", categoryCode='" + categoryCode + '\'' + 
				", pCategoryId='" + pCategoryId + '\'' + 
				", background='" + background + '\'' + 
				", icon='" + icon + '\'' + 
				", sort='" + sort + '\'' + 
				'}';
	}

}