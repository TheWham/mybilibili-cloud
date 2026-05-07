package com.mybilibili.interact.entity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoCommentDTO {

    @NotEmpty
    @Size(max = 500)
    private String content;

    @Size(max = 150)
    private String imgPath;

    @NotEmpty
    private String videoId;

    private Integer replyCommentId;

    /**
     * 前端回复框里带过来的昵称，只用于临时展示，最终用户信息以后应以后端查询为准。
     */
    private String nickName;
}
