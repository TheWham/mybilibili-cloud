package com.mybilibili.base.entity.vo;


import com.mybilibili.base.entity.dto.VideoInfoFilePostDTO;
import com.mybilibili.base.entity.dto.VideoInfoPostDTO;

import java.util.List;

public class VideoInfoPostEditVO {
    private VideoInfoPostDTO videoInfo;
    private List<VideoInfoFilePostDTO> videoInfoFileList;

    public VideoInfoPostDTO getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(VideoInfoPostDTO videoInfo) {
        this.videoInfo = videoInfo;
    }

    public List<VideoInfoFilePostDTO> getVideoInfoFileList() {
        return videoInfoFileList;
    }

    public void setVideoInfoFileList(List<VideoInfoFilePostDTO> videoInfoFileList) {
        this.videoInfoFileList = videoInfoFileList;
    }
}
