package learn.java.dualsystem.order.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaymentMqProperties.class)
public class RabbitOrderTopologyConfiguration {

    @Bean
    public TopicExchange paymentTopicExchange(PaymentMqProperties props) {
        return new TopicExchange(props.getExchange(), true, false);
    }

    @Bean
    public Queue paymentSucceededQueue(PaymentMqProperties props) {
        return new Queue(props.getQueue(), true);
    }

    @Bean
    public Binding paymentSucceededBinding(
            Queue paymentSucceededQueue, TopicExchange paymentTopicExchange, PaymentMqProperties props) {
        return BindingBuilder.bind(paymentSucceededQueue)
                .to(paymentTopicExchange)
                .with(props.getRoutingKey());
    }
}
