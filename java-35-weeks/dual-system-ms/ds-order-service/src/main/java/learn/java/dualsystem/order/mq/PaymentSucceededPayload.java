package learn.java.dualsystem.order.mq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentSucceededPayload(
        String eventId,
        String dedupKey,
        long paymentId,
        long orderId,
        long userId,
        String amount,
        String paidAt,
        int schemaVersion
) {}
