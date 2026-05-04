package com.mybilibili.base.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 这里保留一份系统默认配置，目的不是回到 yml 时代那种“写死配置”，
     * 而是给“首条配置自动初始化”和“脏数据兜底”一个稳定的默认值来源。
     * 这样即便 sys_setting 还是空表，系统第一次读取时也能生成一条完整记录。
     */
    private Integer registerCoinCount;
    private Integer postVideoCoinCount;
    private Integer videoSize;
    private Integer videoPCount;
    private Integer videoCount;
    private Integer commentCount;
    private Integer danmuCount;
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String updateBy;

    public static SysSettingDTO createDefault() {
        SysSettingDTO sysSettingDTO = new SysSettingDTO();
        sysSettingDTO.setRegisterCoinCount(100);
        sysSettingDTO.setPostVideoCoinCount(5);
        sysSettingDTO.setVideoSize(100);
        sysSettingDTO.setVideoPCount(10);
        sysSettingDTO.setVideoCount(10);
        sysSettingDTO.setCommentCount(100);
        sysSettingDTO.setDanmuCount(100);
        return sysSettingDTO;
    }

    public Integer getRegisterCoinCount() {
        return registerCoinCount;
    }

    public void setRegisterCoinCount(Integer registerCoinCount) {
        this.registerCoinCount = registerCoinCount;
    }

    public Integer getPostVideoCoinCount() {
        return postVideoCoinCount;
    }

    public void setPostVideoCoinCount(Integer postVideoCoinCount) {
        this.postVideoCoinCount = postVideoCoinCount;
    }

    public Integer getVideoSize() {
        return videoSize;
    }

    public void setVideoSize(Integer videoSize) {
        this.videoSize = videoSize;
    }

    public Integer getVideoPCount() {
        return videoPCount;
    }

    public void setVideoPCount(Integer videoPCount) {
        this.videoPCount = videoPCount;
    }

    public Integer getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(Integer videoCount) {
        this.videoCount = videoCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getDanmuCount() {
        return danmuCount;
    }

    public void setDanmuCount(Integer danmuCount) {
        this.danmuCount = danmuCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}
