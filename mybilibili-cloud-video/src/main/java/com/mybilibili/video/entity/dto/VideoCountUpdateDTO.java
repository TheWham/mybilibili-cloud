package com.mybilibili.video.entity.dto;

public class VideoCountUpdateDTO {

    private String videoId;
    private Integer count;

    public VideoCountUpdateDTO() {
    }

    public VideoCountUpdateDTO(String videoId, Integer count) {
        this.videoId = videoId;
        this.count = count;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
