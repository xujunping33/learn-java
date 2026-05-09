package learn.java.dualsystem.payment.mq;

public record PaymentSucceededMessage(
        String eventId,
        String dedupKey,
        long paymentId,
        long orderId,
        long userId,
        String amount,
        String paidAt,
        int schemaVersion
) {}
