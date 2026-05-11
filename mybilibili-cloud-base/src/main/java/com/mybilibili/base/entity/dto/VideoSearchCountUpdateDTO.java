package com.mybilibili.base.entity.dto;

import java.io.Serializable;

/**
 * 视频搜索索引计数字段更新请求。
 *
 * <p>调用方只传业务排序类型，不直接传 ES 字段名。search 服务会按白名单枚举换成
 * 真实索引字段，避免远程调用方拼出任意脚本字段。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
public class VideoSearchCountUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频 id。
     */
    private String videoId;

    /**
     * 本次增量，允许负数，用于取消收藏等场景。
     */
    private Integer changeCount;

    /**
     * 搜索排序类型，对应 {@code SearchOrderTypeEnum.status}。
     */
    private Integer orderType;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getChangeCount() {
        return changeCount;
    }

    public void setChangeCount(Integer changeCount) {
        this.changeCount = changeCount;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
}
