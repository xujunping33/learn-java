package learn.java.dualsystem.payment.api;

import learn.java.dualsystem.payment.api.dto.ApiErrorResponse;
import learn.java.dualsystem.payment.api.dto.CreatePaymentRequest;
import learn.java.dualsystem.payment.api.dto.CreatePaymentResponse;
import learn.java.dualsystem.payment.model.Payment;
import learn.java.dualsystem.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreatePaymentRequest req) {
        if (req == null || req.orderId() == null || req.userId() == null || req.amount() == null) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "orderId, userId, amount are required"));
        }
        if (req.orderId() <= 0 || req.userId() <= 0) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "orderId and userId must be positive"));
        }
        if (req.amount().signum() <= 0) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("BAD_REQUEST", "amount must be positive"));
        }

        Payment p = paymentService.create(req.orderId(), req.userId(), req.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreatePaymentResponse(p.getId(), p.getStatus().name()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable("id") long id) {
        PaymentService.ConfirmResult r = paymentService.confirm(id);
        return switch (r) {
            case CONFIRMED -> ResponseEntity.ok().build();
            case ALREADY_SUCCESS -> ResponseEntity.ok().build();
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("NOT_FOUND", "payment not found"));
            case CONFLICT -> ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("CONFLICT", "payment confirm conflict"));
        };
    }
}

