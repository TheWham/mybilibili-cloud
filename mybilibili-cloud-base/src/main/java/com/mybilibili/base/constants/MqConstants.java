package com.mybilibili.base.constants;

/**
 * MQ 交换机、队列和路由键常量。
 *
 * <p>这些名字属于服务间契约，生产者和消费者都要使用同一份定义，
 * 所以放在 base，而不是放到某一个业务服务里。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
public final class MqConstants {

    private MqConstants() {
    }

    /**
     * 视频播放事件交换机。
     */
    public static final String VIDEO_PLAY_EXCHANGE = "mybilibili.video.play.exchange";

    /**
     * 视频播放事件队列，由 video 服务消费。
     */
    public static final String VIDEO_PLAY_QUEUE = "mybilibili.video.play.queue";

    /**
     * 视频播放事件路由键。
     */
    public static final String VIDEO_PLAY_ROUTING_KEY = "video.play.report";
}
