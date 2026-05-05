package com.mybilibili.video.entity.dto;

import java.util.List;

/**
 * 用户主页合集及视频查询结果。
 *
 * <p>该 DTO 只服务 video 模块内部查询。对外返回前，需要在 provider 层转换成 base 中的 VO。</p>
 */
public class SeriesWithVideoQueryDTO {

    private Integer seriesId;
    private String seriesName;
    private List<UserVideoSeriesVideoQueryDTO> videoInfoList;

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

    public List<UserVideoSeriesVideoQueryDTO> getVideoInfoList() {
        return videoInfoList;
    }

    public void setVideoInfoList(List<UserVideoSeriesVideoQueryDTO> videoInfoList) {
        this.videoInfoList = videoInfoList;
    }
}
