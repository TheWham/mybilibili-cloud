package com.mybilibili.message.config;

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

    /**
     * 用户站内信批量消费容器。
     *
     * <p>点赞、收藏、评论这类通知会跟着互动量一起上来。生产端仍然保持一条业务事件一条 MQ 消息，
     * 消费端按小批量攒齐后统一落库，既能削掉一部分数据库写入压力，也不会把单条消息的幂等边界弄乱。</p>
     *
     * @param connectionFactory RabbitMQ 连接工厂
     * @param messageConverter  MQ 消息 JSON 转换器
     * @return 支持 List<UserMessageEvent> 入参的监听容器
     */
    @Bean
    public SimpleRabbitListenerContainerFactory userMessageBatchRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // 监听方法接收 List<UserMessageEvent>，这里必须打开批量监听。
        factory.setBatchListener(true);
        // 允许容器把多条 RabbitMQ 消息攒成一批交给业务消费者。
        factory.setConsumerBatchEnabled(true);
        // 站内信不是强实时链路，100 条以内批量写入能兼顾吞吐和延迟。
        factory.setBatchSize(100);
        // 流量低的时候最多等 1 秒，避免通知一直卡在容器里。
        factory.setReceiveTimeout(1000L);
        // 预取量略高于批大小，给批量消费留余量，但不把太多消息压在单个消费者内存里。
        factory.setPrefetchCount(200);
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
