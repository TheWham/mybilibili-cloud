package com.mybilibili.interact.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoDanmuDTO {

    /**
     * 弹幕正文，长度和单体保持一致。
     */
    @NotEmpty
    @Size(max = 300)
    private String text;

    /**
     * 分 P 或视频文件 id。
     */
    @NotEmpty
    private String fileId;

    /**
     * 弹幕出现时间，单位沿用前端传值。
     */
    @NotNull
    private Integer time;

    @NotEmpty
    private String videoId;

    private Integer mode;

    private String color;
}
