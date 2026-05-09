package learn.java.dualsystem.payment.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PaymentMqProperties.class)
public class RabbitPaymentConfiguration {

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

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
