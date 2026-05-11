package com.mybilibili.user.config;

import com.mybilibili.base.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * user 服务 MQ 配置。
 *
 * @author amani
 * @since 2026/05/11
 */
@Configuration
public class UserRabbitMqConfig {

    @Bean
    public DirectExchange userActionExchange() {
        return new DirectExchange(MqConstants.USER_ACTION_EXCHANGE, true, false);
    }

    @Bean
    public Queue userCoinSyncQueue() {
        return new Queue(MqConstants.USER_COIN_SYNC_QUEUE, true);
    }

    @Bean
    public Binding userCoinSyncBinding(DirectExchange userActionExchange, Queue userCoinSyncQueue) {
        return BindingBuilder.bind(userCoinSyncQueue)
                .to(userActionExchange)
                .with(MqConstants.USER_COIN_SYNC_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
