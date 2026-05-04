package com.mybilibili.video.entity.po;

import java.io.Serializable;
import java.util.List;

/**
 * 视频服务读取分类信息时使用的本地模型。
 *
 * <p>当前视频服务还保留了分类查询逻辑，所以暂时保留本模块 PO。
 * 等分类接口稳定后，跨服务返回值应统一改为 base 中的 DTO。</p>
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
