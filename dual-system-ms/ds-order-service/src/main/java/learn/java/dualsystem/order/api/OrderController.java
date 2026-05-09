package learn.java.dualsystem.order.api;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import learn.java.dualsystem.order.api.dto.ApiErrorResponse;
import learn.java.dualsystem.order.api.dto.CreateOrderRequest;
import learn.java.dualsystem.order.api.dto.CreateOrderResponse;
import learn.java.dualsystem.order.api.dto.MarkPaidRequest;
import learn.java.dualsystem.order.api.dto.OrderDetailResponse;
import learn.java.dualsystem.order.model.Order;
import learn.java.dualsystem.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest req) {
        if (req == null || req.userId() == null || req.amount() == null) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "userId and amount are required"));
        }
        if (req.userId() <= 0) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "userId must be positive"));
        }
        if (req.amount().signum() <= 0) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "amount must be positive"));
        }

        Order o = orderService.create(req.userId(), req.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateOrderResponse(o.getId(), o.getStatus().name()));
    }

    @GetMapping("/{id}")
    @SentinelResource(value = "getOrderById", blockHandler = "getByIdBlocked")
    public ResponseEntity<?> getById(@PathVariable("id") long id) {
        return orderService
                .getById(id)
                .<ResponseEntity<?>>map(o -> ResponseEntity.ok(toDetail(o)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiErrorResponse("NOT_FOUND", "order not found")));
    }

    public ResponseEntity<?> getByIdBlocked(long id, BlockException ex) {
        return ResponseEntity.status(429).body(new ApiErrorResponse("RATE_LIMITED", "too many requests"));
    }

    @PostMapping("/{id}/paid")
    public ResponseEntity<?> markPaid(@PathVariable("id") long id, @RequestBody MarkPaidRequest req) {
        if (req == null || req.paymentId() == null || req.paymentId() <= 0) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "paymentId is required"));
        }
        boolean ok = orderService.markPaid(id, req.paymentId());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiErrorResponse("CONFLICT", "order not in CREATED or paymentId mismatch"));
        }
        return ResponseEntity.ok().build();
    }

    private static OrderDetailResponse toDetail(Order o) {
        return new OrderDetailResponse(
                o.getId(),
                o.getUserId(),
                o.getAmount(),
                o.getStatus() == null ? null : o.getStatus().name(),
                o.getPaidPaymentId(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}

