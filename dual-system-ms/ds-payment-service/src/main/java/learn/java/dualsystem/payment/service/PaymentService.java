package learn.java.dualsystem.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import learn.java.dualsystem.payment.mapper.PaymentMapper;
import learn.java.dualsystem.payment.mapper.PaymentOutboxMapper;
import learn.java.dualsystem.payment.model.Payment;
import learn.java.dualsystem.payment.model.PaymentOutbox;
import learn.java.dualsystem.payment.model.PaymentStatus;
import learn.java.dualsystem.payment.mq.PaymentSucceededMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    public enum ConfirmResult {
        CONFIRMED,
        ALREADY_SUCCESS,
        NOT_FOUND,
        CONFLICT
    }

    private final PaymentMapper paymentMapper;
    private final PaymentOutboxMapper paymentOutboxMapper;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentMapper paymentMapper, PaymentOutboxMapper paymentOutboxMapper, ObjectMapper objectMapper) {
        this.paymentMapper = paymentMapper;
        this.paymentOutboxMapper = paymentOutboxMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment create(long orderId, long userId, java.math.BigDecimal amount) {
        var p = new Payment();
        p.setOrderId(orderId);
        p.setUserId(userId);
        p.setAmount(amount);
        paymentMapper.insert(p);
        return paymentMapper.selectById(p.getId());
    }

    /**
     * Marks payment SUCCESS and appends {@code payment_outbox} in the same transaction (Day235). MQ send is async via
     * {@link learn.java.dualsystem.payment.outbox.PaymentOutboxDispatchService}.
     */
    @Transactional
    public ConfirmResult confirm(long paymentId) {
        Payment p = paymentMapper.selectById(paymentId);
        if (p == null) {
            return ConfirmResult.NOT_FOUND;
        }
        if (p.getStatus() == PaymentStatus.SUCCESS) {
            return ConfirmResult.ALREADY_SUCCESS;
        }
        Instant paidInstant = Instant.now();
        LocalDateTime paidAtDb = LocalDateTime.ofInstant(paidInstant, ZoneOffset.UTC);
        int updated = paymentMapper.markSuccessIfInit(paymentId, paidAtDb);
        if (updated == 0) {
            Payment again = paymentMapper.selectById(paymentId);
            if (again != null && again.getStatus() == PaymentStatus.SUCCESS) {
                return ConfirmResult.ALREADY_SUCCESS;
            }
            return ConfirmResult.CONFLICT;
        }
        p = paymentMapper.selectById(paymentId);
        String eventId = UUID.randomUUID().toString();
        String paidAtIso = paidInstant.toString();
        var message = new PaymentSucceededMessage(
                eventId,
                eventId,
                paymentId,
                p.getOrderId(),
                p.getUserId(),
                p.getAmount().toPlainString(),
                paidAtIso,
                1);
        try {
            var outbox = new PaymentOutbox();
            outbox.setPaymentId(paymentId);
            outbox.setPayloadJson(objectMapper.writeValueAsString(message));
            outbox.setStatus(PaymentOutbox.STATUS_PENDING);
            outbox.setRetryCount(0);
            paymentOutboxMapper.insert(outbox);
        } catch (Exception ex) {
            throw new RuntimeException("failed to serialize or insert payment outbox", ex);
        }
        return ConfirmResult.CONFIRMED;
    }
}
