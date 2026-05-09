package learn.java.dualsystem.order;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("service", "ds-order-service", "ok", true);
    }

    @GetMapping("/api/orders/ping")
    public Map<String, Object> apiPing() {
        return ping();
    }
}

