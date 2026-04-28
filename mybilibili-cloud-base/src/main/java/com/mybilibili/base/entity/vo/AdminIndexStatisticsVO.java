package com.mybilibili.base.entity.vo;

public class AdminIndexStatisticsVO {
    private AdminTotalCountInfoVO totalCountInfo;
    private Integer[] preDayData;

    public AdminTotalCountInfoVO getTotalCountInfo() { return totalCountInfo; }
    public void setTotalCountInfo(AdminTotalCountInfoVO totalCountInfo) { this.totalCountInfo = totalCountInfo; }
    public Integer[] getPreDayData() { return preDayData; }
    public void setPreDayData(Integer[] preDayData) { this.preDayData = preDayData; }
}
