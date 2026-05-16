package com.mybilibili.base.enums;

/**
 * AI 字幕向量化任务状态。
 *
 * <p>这个状态只描述 AI 增强链路，不参与视频审核状态判断。视频审核通过后，
 * worker 可以晚一点处理字幕索引，后台只需要据此展示当前处理进度。</p>
 *
 * @author amani
 * @since 2026/05/16
 */
public enum AiSubtitleIndexStatusEnum {

    /**
     * 没有投递字幕向量化任务，通常是视频还没审核通过，或者没有可处理的视频源。
     */
    UN_SUBMITTED("UN_SUBMITTED", "未投递"),

    /**
     * 任务已经写入 Redis 队列，等待 Python worker 消费。
     */
    PENDING("PENDING", "排队中"),

    /**
     * worker 已经取到任务，正在提取字幕并写入向量索引。
     */
    PROCESSING("PROCESSING", "处理中"),

    /**
     * 当前视频的字幕向量化任务都处理成功。
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 至少有一个任务重试后仍失败，需要人工查看日志或重投。
     */
    FAILED("FAILED", "失败");

    private final String status;
    private final String desc;

    AiSubtitleIndexStatusEnum(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static AiSubtitleIndexStatusEnum getByStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        for (AiSubtitleIndexStatusEnum item : AiSubtitleIndexStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public static String getDescByStatus(String status) {
        AiSubtitleIndexStatusEnum item = getByStatus(status);
        return item == null ? "" : item.getDesc();
    }
}
