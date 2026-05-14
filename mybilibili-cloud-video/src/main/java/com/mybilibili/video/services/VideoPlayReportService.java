package com.mybilibili.video.services;

/**
 * 播放上报应用服务。
 *
 * <p>controller 只负责取参数和登录态，fileId 校验、事件组装以及 MQ 投递统一收口到这里，
 * 这样后续不管是 Web 端还是别的入口接入播放上报，统计口径都能保持一致。</p>
 *
 * @author amani
 * @since 2026/05/14
 */
public interface VideoPlayReportService {

    /**
     * 根据分 P 文件 id 上报一次播放事件。
     *
     * @param fileId 分 P 文件 id
     * @param userId 当前登录用户 id；匿名访问时允许传 null
     */
    void reportVideoPlayByFileId(String fileId, String userId);
}
