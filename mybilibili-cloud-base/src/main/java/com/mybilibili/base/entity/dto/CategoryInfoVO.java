package com.mybilibili.base.entity.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author amani
 * @date 2026/01/19
 */
public class CategoryInfoVO implements Serializable {

    private Integer categoryId;
    private String categoryName;
    private String categoryCode;
    private Integer pCategoryId;
    private String background;
    private String icon;
    private Integer sort;
    private List<CategoryInfoVO> children;
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

    public List<CategoryInfoVO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryInfoVO> children) {
        this.children = children;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public Integer getPCategoryId() {
        return pCategoryId;
    }

    public void setPCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
