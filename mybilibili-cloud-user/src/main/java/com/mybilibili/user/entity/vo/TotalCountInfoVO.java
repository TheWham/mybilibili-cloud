package com.mybilibili.user.entity.vo;

/**
 * @author amani
 * @since 2026.3.31
 * 返回给前端用户中心首页视频数据
 */

public class TotalCountInfoVO {
    private Integer userCount;
    private Integer fansCount;
    private Integer playCount;
    private Integer commentCount;
    private Integer danmuCount;
    private Integer likeCount;
    private Integer coinCount;
    private Integer collectCount;

    public TotalCountInfoVO() {
    }

    public TotalCountInfoVO(Integer fansCount, Integer playCount, Integer commentCount, Integer danmuCount, Integer likeCount, Integer coinCount, Integer collectCount) {
        this.fansCount = fansCount;
        this.playCount = playCount;
        this.commentCount = commentCount;
        this.danmuCount = danmuCount;
        this.likeCount = likeCount;
        this.coinCount = coinCount;
        this.collectCount = collectCount;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getFansCount() {
        return fansCount;
    }

    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
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

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
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
