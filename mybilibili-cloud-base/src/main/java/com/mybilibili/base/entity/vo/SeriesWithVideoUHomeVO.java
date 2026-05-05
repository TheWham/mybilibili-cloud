package com.mybilibili.base.entity.vo;



import java.util.List;

/**
 * @author amani
 */
public class SeriesWithVideoUHomeVO {
    private Integer seriesId;
    private String seriesName;
    private List<UserVideoSeriesVideoVO> videoInfoList;


    public Integer getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public List<UserVideoSeriesVideoVO> getVideoInfoList() {
        return videoInfoList;
    }

    public void setVideoInfoList(List<UserVideoSeriesVideoVO> videoInfoList) {
        this.videoInfoList = videoInfoList;
    }

}
