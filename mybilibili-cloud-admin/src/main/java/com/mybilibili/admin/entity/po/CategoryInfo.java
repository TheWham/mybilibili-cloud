package com.mybilibili.admin.entity.po;

import java.io.Serializable;
import java.util.List;

/**
 * 分类信息表模型。
 *
 * <p>后台服务负责分类维护，所以这里保留一份后台自己的 PO。
 * 对其他服务开放时，后续建议改成 CategoryInfoDTO，避免直接暴露表模型。</p>
 *
 * @author amani
 * @date 2026/01/19
 */
public class CategoryInfo implements Serializable {

    private Integer categoryId;
    private String categoryName;
    private String categoryCode;
    private Integer pCategoryId;
    private String background;
    private String icon;
    private Integer sort;
    private List<CategoryInfo> children;
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
