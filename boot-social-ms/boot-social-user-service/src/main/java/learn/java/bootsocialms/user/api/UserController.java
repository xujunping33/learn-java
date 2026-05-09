package learn.java.bootsocialms.user.api;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Tracer tracer;

    public UserController(Tracer tracer) {
        this.tracer = tracer;
    }

    @GetMapping("/{id}")
    public UserResponse getById(
            @PathVariable("id") Long id
    ) {
        Span span = tracer.currentSpan();
        String traceId = span == null ? "-" : span.context().traceId();
        log.info("getUserById id={} trace={}", id, traceId);
        // Day220: 先用 stub 数据，后续接数据库/认证再替换
        return new UserResponse(id, "user-" + id);
    }
}

