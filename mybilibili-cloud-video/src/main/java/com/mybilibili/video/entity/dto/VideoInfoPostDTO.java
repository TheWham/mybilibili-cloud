package com.mybilibili.video.entity.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VideoInfoPostDTO {

    @NotEmpty @Size(max = 300)
    private String tags;
    @NotEmpty @Size(max = 300)
    private String videoName;
    @NotNull
    private Integer postType;
    @Size(max = 2000)
    private String introduction;
    @NotNull
    private Integer pCategoryId;
    @NotEmpty
    private String videoCover;
    @Size(max = 3)
    private String interaction;
    private Integer categoryId;
    @NotEmpty
    private String uploadFileList;
    private String userId;
    private String videoId;
    private Integer transferResult;
    private Integer updateType;
    private Integer duration;

    public Integer getTransferResult() {
        return transferResult;
    }

    public void setTransferResult(Integer transferResult) {
        this.transferResult = transferResult;
    }

    public Integer getUpdateType() {
        return updateType;
    }

    public void setUpdateType(Integer updateType) {
        this.updateType = updateType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public Integer getPostType() {
        return postType;
    }

    public void setPostType(Integer postType) {
        this.postType = postType;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public Integer getpCategoryId() {
        return pCategoryId;
    }

    public void setpCategoryId(Integer pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public String getVideoCover() {
        return videoCover;
    }

    public void setVideoCover(String videoCover) {
        this.videoCover = videoCover;
    }

    public String getInteraction() {
        return interaction;
    }

    public void setInteraction(String interaction) {
        this.interaction = interaction;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getUploadFileList() {
        return uploadFileList;
    }

    public void setUploadFileList(String uploadFileList) {
        this.uploadFileList = uploadFileList;
    }
}
