package learn.java.dualsystem.payment;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("service", "ds-payment-service", "ok", true);
    }

    @GetMapping("/api/payments/ping")
    public Map<String, Object> apiPing() {
        return ping();
    }
}

