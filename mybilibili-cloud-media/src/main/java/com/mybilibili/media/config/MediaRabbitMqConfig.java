package com.mybilibili.media.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * media 服务 MQ 配置。
 *
 * <p>播放事件使用 JSON 传输，避免使用 JDK 序列化导致后续 DTO 演进困难。</p>
 *
 * @author amani
 * @since 2026/05/04
 */
@Configuration
public class MediaRabbitMqConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
