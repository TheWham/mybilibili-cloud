package com.mybilibili.base.entity.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author amani
 * @since 2026/03/18
 * 用户视频列表
 */
public class UserVideoSeriesVO implements Serializable {
    /**
     * 列表id
     */
    private Integer seriesId;

    /**
     * 列表名称
     */
    private String seriesName;

    /**
     * 列表叙述
     */
    private String seriesDescription;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 更新时间
     */
    @JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 用户id
     */
    private String userId;

    private String cover;

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }
    public Integer getSeriesId() {
        return this.seriesId;
    }
    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }
    public String getSeriesName() {
        return this.seriesName;
    }
    public void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }
    public String getSeriesDescription() {
        return this.seriesDescription;
    }
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    public Integer getSort() {
        return this.sort;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    public Date getUpdateTime() {
        return this.updateTime;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId() {
        return this.userId;
    }

}