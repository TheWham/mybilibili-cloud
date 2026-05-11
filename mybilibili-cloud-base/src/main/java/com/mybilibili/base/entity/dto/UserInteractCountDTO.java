package com.mybilibili.base.entity.dto;

import java.io.Serializable;

/**
 * UP 主互动维度统计。
 *
 * <p>统计口径是“这个用户作为视频作者收到的互动”，不是这个用户主动发出的行为。
 * user 服务只关心展示结果，不应该直接依赖 interact 的表结构。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class UserInteractCountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频收到的评论数。
     */
    private Integer commentCount;

    /**
     * 视频收到的弹幕数。
     */
    private Integer danmuCount;

    /**
     * 视频收到的投币数。
     */
    private Integer coinCount;

    /**
     * 视频被收藏的次数。
     */
    private Integer collectCount;

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

    public Integer getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(Integer coinCount) {
        this.coinCount = coinCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }
}
