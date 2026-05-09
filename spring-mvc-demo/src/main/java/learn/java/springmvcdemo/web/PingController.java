package learn.java.springmvcdemo.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/api/ping")
    public Map<String, Boolean> ping() {
        return Map.of("ok", true);
    }
}
