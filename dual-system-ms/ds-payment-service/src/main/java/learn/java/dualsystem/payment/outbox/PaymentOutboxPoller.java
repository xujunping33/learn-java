package learn.java.dualsystem.payment.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentOutboxPoller {

    private final PaymentOutboxDispatchService dispatchService;

    public PaymentOutboxPoller(PaymentOutboxDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Scheduled(fixedDelayString = "${payment.outbox.dispatch-interval-ms:250}")
    public void tick() {
        dispatchService.dispatchPending();
    }
}
