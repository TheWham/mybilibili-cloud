package com.mybilibili.interact.config;

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

    /**
     * 用户行为交换机。
     *
     * <p>点赞、收藏、投币这类互动动作会同时影响多块数据，比如用户行为表、
     * 视频计数、用户硬币数。生产者只把事件投到这个交换机，后面由不同 routing key
     * 分发给各自服务的队列处理。</p>
     */
    @Bean
    public DirectExchange userActionExchange() {
        return new DirectExchange(MqConstants.USER_ACTION_EXCHANGE, true, false);
    }

    /**
     * 用户行为落库队列。
     *
     * <p>这个队列只归 interact 服务消费，用来把用户对视频的互动关系写入业务表。
     * video、user 服务如果也要处理同一个事件，会绑定自己的队列，互不抢消息。</p>
     */
    @Bean
    public Queue userActionPersistQueue() {
        return new Queue(MqConstants.USER_ACTION_PERSIST_QUEUE, true);
    }

    /**
     * 绑定用户行为落库队列。
     *
     * <p>DirectExchange 会按 routing key 精确投递。只有使用
     * {@link MqConstants#USER_ACTION_PERSIST_ROUTING_KEY} 发送的消息，
     * 才会进入 interact 的用户行为落库队列。</p>
     */
    @Bean
    public Binding userActionPersistBinding(DirectExchange userActionExchange, Queue userActionPersistQueue) {
        return BindingBuilder.bind(userActionPersistQueue)
                .to(userActionExchange)
                .with(MqConstants.USER_ACTION_PERSIST_ROUTING_KEY);
    }

    /**
     * 弹幕事件交换机。
     *
     * <p>发送弹幕时，接口层先完成参数校验、视频状态校验和用户额度占用，
     * 然后把弹幕事件投到这里。真正落库放到消费者里做，避免发送接口直接压数据库。</p>
     */
    @Bean
    public DirectExchange danmuExchange() {
        return new DirectExchange(MqConstants.DANMU_EXCHANGE, true, false);
    }

    /**
     * 弹幕落库队列。
     *
     * <p>这个队列是持久化队列，RabbitMQ 重启后队列本身还在。注意消息是否持久化
     * 还取决于发送端和消息转换器的配置，队列持久化只解决队列定义不丢的问题。</p>
     */
    @Bean
    public Queue danmuPersistQueue() {
        return new Queue(MqConstants.DANMU_PERSIST_QUEUE, true);
    }

    /**
     * 绑定弹幕落库队列。
     *
     * <p>只接收 routing key 为 {@link MqConstants#DANMU_PERSIST_ROUTING_KEY}
     * 的弹幕消息。后续如果弹幕还要同步到搜索、审核等模块，可以继续给同一个交换机
     * 绑定新的队列，不影响当前落库链路。</p>
     */
    @Bean
    public Binding danmuPersistBinding(DirectExchange danmuExchange, Queue danmuPersistQueue) {
        return BindingBuilder.bind(danmuPersistQueue)
                .to(danmuExchange)
                .with(MqConstants.DANMU_PERSIST_ROUTING_KEY);
    }

    /**
     * 弹幕批量消费容器。
     *
     * <p>弹幕写入频率高，逐条 insert 会放大数据库压力。这里让监听器一次拿一批消息，
     * 再由消费者统一 insertBatch。这样生产端仍然是一条弹幕一条 MQ 消息，可靠性更清楚；
     * 批量优化放在消费端，失败后也方便按 MQ 的重试机制处理。</p>
     */
    @Bean
    public SimpleRabbitListenerContainerFactory danmuBatchRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        // 监听方法接收 List<VideoDanmuPostEvent>，需要打开批量监听。
        factory.setBatchListener(true);
        // 允许容器把多条 RabbitMQ 消息攒成一批交给监听方法。
        factory.setConsumerBatchEnabled(true);
        // 最多攒 100 条交给一次消费者，和 VideoDanmuPersistConsumer 的批量落库配合。
        factory.setBatchSize(100);
        // 流量不高时最多等 1 秒，避免一直等满 100 条导致弹幕迟迟不落库。
        factory.setReceiveTimeout(1000L);
        // 单个消费者最多预取 200 条，给批量消费留余量，也避免一次拉太多压住内存。
        factory.setPrefetchCount(200);
        return factory;
    }

    /**
     * MQ 消息 JSON 转换器。
     *
     * <p>生产者发送 Java 事件对象，消费者也按事件对象接收，中间用 JSON 做序列化，
     * 比默认的 Java 序列化更方便排查消息内容。</p>
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
