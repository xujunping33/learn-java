package learn.java.dualsystem.payment.api.dto;

public record CreatePaymentRequest(Long orderId, Long userId, java.math.BigDecimal amount) {}

