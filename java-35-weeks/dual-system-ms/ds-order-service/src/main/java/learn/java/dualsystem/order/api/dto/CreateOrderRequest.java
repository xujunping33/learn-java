package learn.java.dualsystem.order.api.dto;

import java.math.BigDecimal;

public record CreateOrderRequest(Long userId, BigDecimal amount) {}

