package com.mybilibili.base.entity.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * @author 86150
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoVO implements Serializable {
    // 用户ID
    private String userId;
    // 用户昵称
    private String nickName;

    // 用户头像URL
    private String avatar;
    //封面主题
    private Integer theme;
    //播放量
    private Integer playCount;
    //获赞量
    private Integer likeCount;
    //粉丝量
    private Integer fansCount;
    //关注数
    private Integer focusCount;
    //硬币总量
    private Integer currentCoinCount;
    private String personIntroduction;
    private Integer haveFocus;

    public Integer getHaveFocus() {
        return haveFocus;
    }

    public void setHaveFocus(Integer haveFocus) {
        this.haveFocus = haveFocus;
    }

    public String getPersonIntroduction() {
        return personIntroduction;
    }

    public void setPersonIntroduction(String personIntroduction) {
        this.personIntroduction = personIntroduction;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getTheme() {
        return theme;
    }

    public void setTheme(Integer theme) {
        this.theme = theme;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getFansCount() {
        return fansCount;
    }

    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    public Integer getFocusCount() {
        return focusCount;
    }

    public void setFocusCount(Integer focusCount) {
        this.focusCount = focusCount;
    }

    public Integer getCurrentCoinCount() {
        return currentCoinCount;
    }

    public void setCurrentCoinCount(Integer currentCoinCount) {
        this.currentCoinCount = currentCoinCount;
    }

    @Override
    public String toString() {
        return "UserInfoVO{" +
                "userId='" + userId + '\'' +
                ", nickName='" + nickName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", theme=" + theme +
                ", playCount=" + playCount +
                ", likeCount=" + likeCount +
                ", fansCount=" + fansCount +
                ", focusCount=" + focusCount +
                ", currentCoinCount=" + currentCoinCount +
                '}';
    }
}
