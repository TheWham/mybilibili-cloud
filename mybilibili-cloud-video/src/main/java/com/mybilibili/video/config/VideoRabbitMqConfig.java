package com.mybilibili.video.config;

import com.mybilibili.base.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * video 服务 MQ 配置。
 *
 * <p>播放事件队列由 video 服务声明和消费。media 只投递事件，不关心队列细节。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Configuration
public class VideoRabbitMqConfig {

    @Bean
    public DirectExchange videoPlayExchange() {
        return new DirectExchange(MqConstants.VIDEO_PLAY_EXCHANGE, true, false);
    }

    @Bean
    public Queue videoPlayQueue() {
        return new Queue(MqConstants.VIDEO_PLAY_QUEUE, true);
    }

    @Bean
    public Binding videoPlayBinding(DirectExchange videoPlayExchange, Queue videoPlayQueue) {
        return BindingBuilder.bind(videoPlayQueue)
                .to(videoPlayExchange)
                .with(MqConstants.VIDEO_PLAY_ROUTING_KEY);
    }

    @Bean
    public DirectExchange userActionExchange() {
        return new DirectExchange(MqConstants.USER_ACTION_EXCHANGE, true, false);
    }

    @Bean
    public Queue videoActionCountQueue() {
        return new Queue(MqConstants.VIDEO_ACTION_COUNT_QUEUE, true);
    }

    @Bean
    public Binding videoActionCountBinding(DirectExchange userActionExchange, Queue videoActionCountQueue) {
        return BindingBuilder.bind(videoActionCountQueue)
                .to(userActionExchange)
                .with(MqConstants.VIDEO_ACTION_COUNT_ROUTING_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory videoActionCountBatchRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setBatchListener(true);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(100);
        factory.setReceiveTimeout(1000L);
        factory.setPrefetchCount(200);
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
