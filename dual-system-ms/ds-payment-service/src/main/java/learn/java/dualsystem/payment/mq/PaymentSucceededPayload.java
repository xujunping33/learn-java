package learn.java.dualsystem.payment.mq;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Contract: W34 PaymentSucceeded event. {@code dedupKey} defaults to {@code eventId} for consumer idempotency.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentSucceededPayload(
        String eventId,
        String dedupKey,
        long paymentId,
        long orderId,
        long userId,
        String amount,
        /** ISO-8601 instant string, UTC recommended */
        String paidAt,
        int schemaVersion
) {}
