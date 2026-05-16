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

    /**
     * 用户互动事件交换机。
     */
    public static final String USER_ACTION_EXCHANGE = "mybilibili.user.action.exchange";

    /**
     * 用户视频行为落库队列，由 interact 服务消费。
     */
    public static final String USER_ACTION_PERSIST_QUEUE = "mybilibili.user.action.persist.queue";

    /**
     * 用户视频行为落库路由键。
     */
    public static final String USER_ACTION_PERSIST_ROUTING_KEY = "user.action.persist";

    /**
     * 视频互动计数队列，由 video 服务消费。
     */
    public static final String VIDEO_ACTION_COUNT_QUEUE = "mybilibili.video.action.count.queue";

    /**
     * 视频互动计数路由键。
     */
    public static final String VIDEO_ACTION_COUNT_ROUTING_KEY = "video.action.count";

    /**
     * 视频转码事件交换机。
     */
    public static final String VIDEO_TRANSFER_EXCHANGE = "mybilibili.video.transfer.exchange";

    /**
     * 视频转码事件队列，由 video 服务消费。
     */
    public static final String VIDEO_TRANSFER_QUEUE = "mybilibili.video.transfer.queue";

    /**
     * 视频转码事件路由键。
     */
    public static final String VIDEO_TRANSFER_ROUTING_KEY = "video.transfer.execute";

    /**
     * 用户硬币计数队列，由 user 服务消费。
     */
    public static final String USER_COIN_SYNC_QUEUE = "mybilibili.user.coin.sync.queue";

    /**
     * 用户硬币计数路由键。
     */
    public static final String USER_COIN_SYNC_ROUTING_KEY = "user.coin.sync";

    /**
     * 用户站内信事件交换机。
     */
    public static final String USER_MESSAGE_EXCHANGE = "mybilibili.user.message.exchange";

    /**
     * 用户站内信落库队列，由 message 服务消费。
     */
    public static final String USER_MESSAGE_PERSIST_QUEUE = "mybilibili.user.message.persist.queue";

    /**
     * 用户站内信落库路由键。
     */
    public static final String USER_MESSAGE_PERSIST_ROUTING_KEY = "user.message.persist";

    /**
     * 弹幕事件交换机。
     */
    public static final String DANMU_EXCHANGE = "mybilibili.danmu.exchange";

    /**
     * 弹幕批量落库队列，由 interact 服务消费。
     */
    public static final String DANMU_PERSIST_QUEUE = "mybilibili.danmu.persist.queue";

    /**
     * 弹幕批量落库路由键。
     */
    public static final String DANMU_PERSIST_ROUTING_KEY = "danmu.persist";
}
