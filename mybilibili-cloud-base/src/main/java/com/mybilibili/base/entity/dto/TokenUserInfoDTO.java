package com.mybilibili.base.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;


/**
 * TokenUserInfoDTO 类用于存储用户信息数据传输对象
 * 该类实现了Serializable接口，支持序列化操作
 * 使用@JsonIgnoreProperties注解忽略未知属性，防止反序列化时出现异常
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDTO implements Serializable {

    // 用户ID
    private String userId;
    // 用户昵称
    private String nickName;
    // 令牌ID
    private String tokenId;
    // 令牌过期时间
    private Long expireAt;
    // 用户头像URL
    private String avatar;
    //主页主题
    private Integer theme;
    private String personIntroduction;

    public String getPersonIntroduction() {
        return personIntroduction;
    }

    public void setPersonIntroduction(String personIntroduction) {
        this.personIntroduction = personIntroduction;
    }

    public Integer getTheme() {
        return theme;
    }

    public void setTheme(Integer theme) {
        this.theme = theme;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * 获取用户ID
     * @return 用户ID字符串
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     * @param userId 要设置的用户ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 获取用户昵称
     * @return 用户昵称字符串
     */
    public String getNickName() {
        return nickName;
    }

    /**
     * 设置用户昵称
     * @param nickName 要设置的用户昵称
     */
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * 获取令牌ID
     * @return 令牌ID字符串
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * 设置令牌ID
     * @param tokenId 要设置的令牌ID
     */
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * 获取令牌过期时间
     * @return 过期时间字符串
     */
    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


}
