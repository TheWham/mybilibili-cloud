package com.mybilibili.base.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mybilibili.base.entity.po.SysSetting;

import java.io.Serial;
import java.io.Serializable;

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

    public static SysSettingDTO fromSysSetting(SysSetting sysSetting) {
        SysSettingDTO sysSettingDTO = createDefault();
        if (sysSetting == null) {
            return sysSettingDTO;
        }
        if (sysSetting.getRegisterCoinCount() != null) {
            sysSettingDTO.setRegisterCoinCount(sysSetting.getRegisterCoinCount());
        }
        if (sysSetting.getPostVideoCoinCount() != null) {
            sysSettingDTO.setPostVideoCoinCount(sysSetting.getPostVideoCoinCount());
        }
        if (sysSetting.getVideoSize() != null) {
            sysSettingDTO.setVideoSize(sysSetting.getVideoSize());
        }
        if (sysSetting.getVideoPCount() != null) {
            sysSettingDTO.setVideoPCount(sysSetting.getVideoPCount());
        }
        if (sysSetting.getVideoCount() != null) {
            sysSettingDTO.setVideoCount(sysSetting.getVideoCount());
        }
        if (sysSetting.getCommentCount() != null) {
            sysSettingDTO.setCommentCount(sysSetting.getCommentCount());
        }
        if (sysSetting.getDanmuCount() != null) {
            sysSettingDTO.setDanmuCount(sysSetting.getDanmuCount());
        }
        return sysSettingDTO;
    }

    public SysSetting toSysSetting() {
        SysSetting sysSetting = new SysSetting();
        sysSetting.setRegisterCoinCount(this.registerCoinCount);
        sysSetting.setPostVideoCoinCount(this.postVideoCoinCount);
        sysSetting.setVideoSize(this.videoSize);
        sysSetting.setVideoPCount(this.videoPCount);
        sysSetting.setVideoCount(this.videoCount);
        sysSetting.setCommentCount(this.commentCount);
        sysSetting.setDanmuCount(this.danmuCount);
        return sysSetting;
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
}
