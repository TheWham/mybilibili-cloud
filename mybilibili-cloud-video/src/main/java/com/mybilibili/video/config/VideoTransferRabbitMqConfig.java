package com.mybilibili.video.config;

import com.mybilibili.base.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 视频转码 MQ 配置。
 *
 * <p>转码任务放在 video 服务内部消费，外部服务只负责投递事件，不关心具体执行过程。</p>
 *
 * @author amani
 * @since 2026/05/15
 */
@Configuration
public class VideoTransferRabbitMqConfig {

    /**
     * 视频转码交换机。
     *
     * @return 直连交换机
     */
    @Bean
    public DirectExchange videoTransferExchange() {
        return new DirectExchange(MqConstants.VIDEO_TRANSFER_EXCHANGE, true, false);
    }

    /**
     * 视频转码队列。
     *
     * @return 持久化队列
     */
    @Bean
    public Queue videoTransferQueue() {
        return new Queue(MqConstants.VIDEO_TRANSFER_QUEUE, true);
    }

    /**
     * 绑定转码队列和交换机。
     *
     * @param videoTransferExchange 交换机
     * @param videoTransferQueue 队列
     * @return 绑定关系
     */
    @Bean
    public Binding videoTransferBinding(DirectExchange videoTransferExchange, Queue videoTransferQueue) {
        return BindingBuilder.bind(videoTransferQueue)
                .to(videoTransferExchange)
                .with(MqConstants.VIDEO_TRANSFER_ROUTING_KEY);
    }
}
