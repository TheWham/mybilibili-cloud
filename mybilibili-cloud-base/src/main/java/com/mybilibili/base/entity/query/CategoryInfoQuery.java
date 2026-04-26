package com.mybilibili.base.entity.query;

/**
 * @author amani
 * @date 2026/01/19
 * @description 分类信息

 */
public class CategoryInfoQuery extends BaseQuery {
	/**
	 * @description 自增分类id
	 */
	private String categoryId;
	private String categoryIdFuzzy;
	/**
	 * @description 分类名称
	 */
	private String categoryName;
	private String categoryNameFuzzy;
	/**
	 * @description 分类编码
	 */
	private String categoryCode;
	private String categoryCodeFuzzy;
	/**
	 * @description 父级分类id

	 */
	private String pCategoryId;
	private String pCategoryIdFuzzy;
	/**
	 * @description 分类背景图
	 */
	private String background;
	private String backgroundFuzzy;
	/**
	 * @description 分类名称图标
	 */
	private String icon;
	private String iconFuzzy;
	/**
	 * @description 分类排列优先级
	 */
	private Integer sort;

	/**
	 * @description 批量删除分类
	 */

	private Integer categoryIdOrPCategoryId;

	/**
	 * @description 是否将线性查询转换成树形
	 */
	private boolean convert2Tree;



	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryId() {
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
	public void setPCategoryId(String pCategoryId) {
		this.pCategoryId = pCategoryId;
	}
	public String getPCategoryId() {
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
	public void setCategoryIdFuzzy(String categoryIdFuzzy) {
		this.categoryIdFuzzy = categoryIdFuzzy;
	}
	public String getCategoryIdFuzzy() {
		return this.categoryIdFuzzy;
	}
	public void setCategoryNameFuzzy(String categoryNameFuzzy) {
		this.categoryNameFuzzy = categoryNameFuzzy;
	}
	public String getCategoryNameFuzzy() {
		return this.categoryNameFuzzy;
	}
	public void setCategoryCodeFuzzy(String categoryCodeFuzzy) {
		this.categoryCodeFuzzy = categoryCodeFuzzy;
	}
	public String getCategoryCodeFuzzy() {
		return this.categoryCodeFuzzy;
	}
	public void setPCategoryIdFuzzy(String pCategoryIdFuzzy) {
		this.pCategoryIdFuzzy = pCategoryIdFuzzy;
	}
	public String getPCategoryIdFuzzy() {
		return this.pCategoryIdFuzzy;
	}
	public void setBackgroundFuzzy(String backgroundFuzzy) {
		this.backgroundFuzzy = backgroundFuzzy;
	}
	public String getBackgroundFuzzy() {
		return this.backgroundFuzzy;
	}
	public void setIconFuzzy(String iconFuzzy) {
		this.iconFuzzy = iconFuzzy;
	}
	public String getIconFuzzy() {
		return this.iconFuzzy;
	}

	public String getpCategoryId() {
		return pCategoryId;
	}

	public void setpCategoryId(String pCategoryId) {
		this.pCategoryId = pCategoryId;
	}

	public String getpCategoryIdFuzzy() {
		return pCategoryIdFuzzy;
	}

	public void setpCategoryIdFuzzy(String pCategoryIdFuzzy) {
		this.pCategoryIdFuzzy = pCategoryIdFuzzy;
	}

	public Integer getCategoryIdOrPCategoryId() {
		return categoryIdOrPCategoryId;
	}

	public void setCategoryIdOrPCategoryId(Integer categoryIdOrPCategoryId) {
		this.categoryIdOrPCategoryId = categoryIdOrPCategoryId;
	}

	public boolean isConvert2Tree() {
		return convert2Tree;
	}

	public void setConvert2Tree(boolean convert2Tree) {
		this.convert2Tree = convert2Tree;
	}
}