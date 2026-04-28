package com.mybilibili.user.entity.dto;

public class UserCoinCountUpdateDTO {
    private String userId;
    private Integer totalCoinCount;
    private Integer currentCoinCount;

    public UserCoinCountUpdateDTO() {
    }

    public UserCoinCountUpdateDTO(String userId, Integer totalCoinCount, Integer currentCoinCount) {
        this.userId = userId;
        this.totalCoinCount = totalCoinCount;
        this.currentCoinCount = currentCoinCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTotalCoinCount() {
        return totalCoinCount;
    }

    public void setTotalCoinCount(Integer totalCoinCount) {
        this.totalCoinCount = totalCoinCount;
    }

    public Integer getCurrentCoinCount() {
        return currentCoinCount;
    }

    public void setCurrentCoinCount(Integer currentCoinCount) {
        this.currentCoinCount = currentCoinCount;
    }
}
