package learn.java.ssmsocial.web;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    private static final Logger log = LoggerFactory.getLogger(PingController.class);

    @GetMapping("/api/ping")
    public Map<String, Boolean> ping() {
        log.info("ping");
        return Map.of("ok", true);
    }
}

