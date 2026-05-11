package learn.java.dualsystem.payment.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentSucceededPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentMqProperties props;

    public PaymentSucceededPublisher(
            RabbitTemplate rabbitTemplate, ObjectMapper objectMapper, PaymentMqProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.props = props;
    }

    public void publish(PaymentSucceededMessage message) throws Exception {
        byte[] body = objectMapper.writeValueAsBytes(message);
        var mp = new MessageProperties();
        mp.setContentType("application/json");
        rabbitTemplate.send(props.getExchange(), props.getRoutingKey(), new Message(body, mp));
    }
}
