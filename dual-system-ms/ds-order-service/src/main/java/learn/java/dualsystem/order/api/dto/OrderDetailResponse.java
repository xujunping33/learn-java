package learn.java.dualsystem.order.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDetailResponse(
        Long id,
        Long userId,
        BigDecimal amount,
        String status,
        Long paidPaymentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

