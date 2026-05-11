package com.mybilibili.interact.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 用户互动请求参数。
 *
 * <p>这里保留单体时期的接口形态，前端迁移成本最低。真正的业务校验仍然放在
 * Service 里，避免只靠参数注解处理业务规则。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class UserActionDTO {

    /**
     * 视频 id。
     */
    @NotEmpty
    private String videoId;

    /**
     * 行为类型，见 UserActionTypeEnum。
     */
    @NotNull
    private Integer actionType;

    /**
     * 操作数量。投币允许 1 到 2，点赞收藏未传时按 1 处理。
     */
    @Min(1)
    @Max(2)
    private Integer actionCount;

    /**
     * 评论 id。当前迁移重点是视频点赞、收藏、投币，评论互动后续单独事件化。
     */
    private Integer commentId;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public Integer getActionCount() {
        return actionCount;
    }

    public void setActionCount(Integer actionCount) {
        this.actionCount = actionCount;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }
}
