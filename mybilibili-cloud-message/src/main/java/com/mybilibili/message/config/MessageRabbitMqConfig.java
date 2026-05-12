package com.mybilibili.message.config;

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
 * message 服务 MQ 配置。
 *
 * <p>站内信事件统一从 RabbitMQ 进入 message 服务，避免各业务服务直接写消息表。</p>
 *
 * @author amani
 * @since 2026/05/13
 */
@Configuration
public class MessageRabbitMqConfig {

    @Bean
    public DirectExchange userMessageExchange() {
        return new DirectExchange(MqConstants.USER_MESSAGE_EXCHANGE, true, false);
    }

    @Bean
    public Queue userMessagePersistQueue() {
        return new Queue(MqConstants.USER_MESSAGE_PERSIST_QUEUE, true);
    }

    @Bean
    public Binding userMessagePersistBinding(DirectExchange userMessageExchange, Queue userMessagePersistQueue) {
        return BindingBuilder.bind(userMessagePersistQueue)
                .to(userMessageExchange)
                .with(MqConstants.USER_MESSAGE_PERSIST_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
