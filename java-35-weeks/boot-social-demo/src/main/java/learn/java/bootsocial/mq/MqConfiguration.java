package learn.java.bootsocial.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile({"dev", "docker"})
public class MqConfiguration {

    public static final String TEST_EXCHANGE = "bootsocial.test.direct";
    public static final String TEST_QUEUE = "bootsocial.test.queue";
    public static final String TEST_ROUTING_KEY = "test";

    public static final String NOTIFY_EXCHANGE = "bootsocial.notify.direct";
    public static final String NOTIFY_QUEUE = "bootsocial.notify.queue";
    public static final String RK_COMMENT_CREATED = "comment.created";
    public static final String RK_POST_LIKED = "post.liked";

    public static final String DLX_EXCHANGE = "bootsocial.notify.dlx";
    public static final String DLQ_QUEUE = "bootsocial.notify.dlq";
    public static final String DLQ_ROUTING_KEY = "dlq";

    @Bean
    public DirectExchange testExchange() {
        return new DirectExchange(TEST_EXCHANGE, true, false);
    }

    @Bean
    public Queue testQueue() {
        return new Queue(TEST_QUEUE, true);
    }

    @Bean
    public Binding testBinding(DirectExchange testExchange, Queue testQueue) {
        return BindingBuilder.bind(testQueue).to(testExchange).with(TEST_ROUTING_KEY);
    }

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(NOTIFY_EXCHANGE, true, false);
    }

    @Bean
    public Queue notifyQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        return new Queue(NOTIFY_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding commentCreatedBinding(DirectExchange notifyExchange, Queue notifyQueue) {
        return BindingBuilder.bind(notifyQueue).to(notifyExchange).with(RK_COMMENT_CREATED);
    }

    @Bean
    public Binding postLikedBinding(DirectExchange notifyExchange, Queue notifyQueue) {
        return BindingBuilder.bind(notifyQueue).to(notifyExchange).with(RK_POST_LIKED);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue dlqQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    @Bean
    public Binding dlqBinding(DirectExchange dlxExchange, Queue dlqQueue) {
        return BindingBuilder.bind(dlqQueue).to(dlxExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter mqJsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter mqJsonMessageConverter) {
        RabbitTemplate tpl = new RabbitTemplate(connectionFactory);
        tpl.setMessageConverter(mqJsonMessageConverter);
        return tpl;
    }

    /**
     * Ensure {@link org.springframework.amqp.rabbit.annotation.RabbitListener} uses JSON converter too.
     * Otherwise it may fall back to default converter and fail to bind records/POJOs.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageConverter mqJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(mqJsonMessageConverter);

        // Day202: fixed retry + DLQ republish (avoid infinite retry storm)
        RepublishMessageRecoverer recoverer =
                new RepublishMessageRecoverer(
                        rabbitTemplate(connectionFactory, mqJsonMessageConverter),
                        DLX_EXCHANGE,
                        DLQ_ROUTING_KEY);
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .recoverer(recoverer)
                        .build());
        return factory;
    }
}

