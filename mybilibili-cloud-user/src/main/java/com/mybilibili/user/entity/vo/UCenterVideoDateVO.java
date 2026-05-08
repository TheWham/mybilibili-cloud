package com.mybilibili.user.entity.vo;

/**
 * @author amani
 * 返回给前端用户中心首页视频数据
 */
public class UCenterVideoDateVO {
    /**
     * 当前用户视频数据
     */
    private TotalCountInfoVO totalCountInfo;
    /**
     * 当前用户昨天视频数据
     */
    private Integer[] preDayData;

    public TotalCountInfoVO getTotalCountInfo() {
        return totalCountInfo;
    }

    public void setTotalCountInfo(TotalCountInfoVO totalCountInfo) {
        this.totalCountInfo = totalCountInfo;
    }

    public Integer[] getPreDayData() {
        return preDayData;
    }

    public void setPreDayData(Integer[] preDayData) {
        this.preDayData = preDayData;
    }
}
