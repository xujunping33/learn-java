package learn.java.dualsystem.report.api;

import java.math.BigDecimal;

public record DailyReportResponse(
        String date,
        long ordersCount,
        long paidCount,
        BigDecimal paidAmountSum
) {}

