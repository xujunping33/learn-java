package learn.java.dualsystem.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import learn.java.dualsystem.order.service.PaymentSucceededInboxService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentSucceededListener {

    private final ObjectMapper objectMapper;
    private final PaymentSucceededInboxService inboxService;

    public PaymentSucceededListener(ObjectMapper objectMapper, PaymentSucceededInboxService inboxService) {
        this.objectMapper = objectMapper;
        this.inboxService = inboxService;
    }

    @RabbitListener(queues = "${dual.rabbit.payment-succeeded.queue}")
    public void onMessage(Message message) throws Exception {
        var payload = objectMapper.readValue(message.getBody(), PaymentSucceededPayload.class);
        inboxService.handle(payload);
    }
}
