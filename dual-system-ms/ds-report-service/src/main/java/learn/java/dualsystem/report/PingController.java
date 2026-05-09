package learn.java.dualsystem.report;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("service", "ds-report-service", "ok", true);
    }

    @GetMapping("/api/reports/ping")
    public Map<String, Object> apiPing() {
        return ping();
    }
}

