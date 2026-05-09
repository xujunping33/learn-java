package learn.java.dualsystem.payment.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import learn.java.dualsystem.payment.mapper.PaymentMapper;
import learn.java.dualsystem.payment.mapper.PaymentOutboxMapper;
import learn.java.dualsystem.payment.model.PaymentOutbox;
import learn.java.dualsystem.payment.mq.PaymentSucceededMessage;
import learn.java.dualsystem.payment.mq.PaymentSucceededPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PaymentOutboxDispatchService {

    private static final Logger log = LoggerFactory.getLogger(PaymentOutboxDispatchService.class);

    private final PaymentOutboxMapper outboxMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentSucceededPublisher publisher;
    private final ObjectMapper objectMapper;
    private final PaymentOutboxProperties props;
    private final TransactionTemplate transactionTemplate;

    public PaymentOutboxDispatchService(
            PaymentOutboxMapper outboxMapper,
            PaymentMapper paymentMapper,
            PaymentSucceededPublisher publisher,
            ObjectMapper objectMapper,
            PaymentOutboxProperties props,
            PlatformTransactionManager transactionManager) {
        this.outboxMapper = outboxMapper;
        this.paymentMapper = paymentMapper;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.props = props;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Poll pending outbox rows and publish to RabbitMQ. Duplicate publish is acceptable: order-service is idempotent.
     */
    public void dispatchPending() {
        var pending = outboxMapper.selectPending(props.getBatchSize());
        for (PaymentOutbox row : pending) {
            try {
                var message = objectMapper.readValue(row.getPayloadJson(), PaymentSucceededMessage.class);
                publisher.publish(message);
                transactionTemplate.executeWithoutResult(status -> {
                    int n = outboxMapper.updateSentIfPending(row.getId());
                    if (n > 0) {
                        paymentMapper.markEventPublished(
                                row.getPaymentId(), LocalDateTime.now(ZoneOffset.UTC));
                    }
                });
            } catch (Exception ex) {
                log.warn(
                        "outbox publish or finalize failed id={} paymentId={}: {}",
                        row.getId(),
                        row.getPaymentId(),
                        ex.toString());
                outboxMapper.incrementRetryAndMaybeFail(row.getId(), props.getMaxRetries());
            }
        }
    }
}
