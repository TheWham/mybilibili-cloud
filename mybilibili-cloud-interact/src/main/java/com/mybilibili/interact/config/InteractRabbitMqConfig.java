package com.mybilibili.interact.config;

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
 * interact 服务 MQ 配置。
 *
 * <p>互动入口只声明自己要消费的行为落库队列，video、user 的队列由各自服务声明。
 * 这样队列归属和数据归属保持一致。</p>
 *
 * @author amani
 * @since 2026/05/11
 */
@Configuration
public class InteractRabbitMqConfig {

    @Bean
    public DirectExchange userActionExchange() {
        return new DirectExchange(MqConstants.USER_ACTION_EXCHANGE, true, false);
    }

    @Bean
    public Queue userActionPersistQueue() {
        return new Queue(MqConstants.USER_ACTION_PERSIST_QUEUE, true);
    }

    @Bean
    public Binding userActionPersistBinding(DirectExchange userActionExchange, Queue userActionPersistQueue) {
        return BindingBuilder.bind(userActionPersistQueue)
                .to(userActionExchange)
                .with(MqConstants.USER_ACTION_PERSIST_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
