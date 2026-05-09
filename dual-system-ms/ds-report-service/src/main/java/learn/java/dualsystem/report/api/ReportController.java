package learn.java.dualsystem.report.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Daily aggregates for ops / BI (B 端). Through {@code ds-gateway}: require header {@code X-Report-Key} when
 * {@code report.api-key} is set on the gateway (Day237).
 *
 * <p><b>口径（同一 {@code date} 参数）</b>
 * <ul>
 *   <li>{@code ordersCount}：按<strong>下单时间</strong>统计 — {@code ds_order.orders.created_at}，区间 [UTC 当日 00:00, 次日 00:00)。
 *   <li>{@code paidCount} / {@code paidAmountSum}：按<strong>支付成功时间</strong>统计 —
 *       {@code ds_payment.payments} 且 {@code status = 'SUCCESS'}、{@code paid_at} 落在该日 UTC 日界内（与上项相同 [00:00, 次日 00:00)）。
 * </ul>
 * 因此「同一天下单数」与「同一天支付成功笔数」无必然相等关系，勿混读。
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final NamedParameterJdbcTemplate jdbc;

    public ReportController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/daily")
    public ResponseEntity<?> daily(@RequestParam("date") String date) {
        LocalDate d;
        try {
            d = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "date must be YYYY-MM-DD"));
        }

        var start = d.atStartOfDay().toInstant(ZoneOffset.UTC);
        var end = d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        Map<String, Object> params = Map.of("start", start, "end", end);

        long ordersCount = jdbc.queryForObject(
                """
                SELECT COUNT(*) AS c
                FROM ds_order.orders
                WHERE created_at >= :start AND created_at < :end
                """,
                params,
                Long.class
        );

        Long paidCountObj = jdbc.queryForObject(
                """
                SELECT COUNT(*) AS c
                FROM ds_payment.payments
                WHERE status = 'SUCCESS'
                  AND paid_at IS NOT NULL
                  AND paid_at >= :start AND paid_at < :end
                """,
                params,
                Long.class
        );
        long paidCount = paidCountObj == null ? 0 : paidCountObj;

        BigDecimal paidAmountSum = jdbc.queryForObject(
                """
                SELECT COALESCE(SUM(amount), 0) AS s
                FROM ds_payment.payments
                WHERE status = 'SUCCESS'
                  AND paid_at IS NOT NULL
                  AND paid_at >= :start AND paid_at < :end
                """,
                params,
                BigDecimal.class
        );
        if (paidAmountSum == null) {
            paidAmountSum = BigDecimal.ZERO;
        }

        return ResponseEntity.ok(new DailyReportResponse(d.toString(), ordersCount, paidCount, paidAmountSum));
    }
}

