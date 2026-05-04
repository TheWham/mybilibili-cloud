package com.mybilibili.video.entity.vo;

import com.mybilibili.video.entity.po.UserVideoSeriesVideo;

import java.util.List;

/**
 * @author amani
 */
public class SeriesWithVideoUHomeVO {
    private Integer seriesId;
    private String seriesName;
    private List<UserVideoSeriesVideo> videoInfoList;


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

    public List<UserVideoSeriesVideo> getVideoInfoList() {
        return videoInfoList;
    }

    public void setVideoInfoList(List<UserVideoSeriesVideo> videoInfoList) {
        this.videoInfoList = videoInfoList;
    }

    @Override
    public String toString() {
        return "SeriesWithVideoUHomeVO{" +
                "seriesId=" + seriesId +
                ", seriesName='" + seriesName + '\'' +
                ", videoInfoList=" + videoInfoList +
                '}';
    }
}
