package learn.java.bootsocialms.post.api;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import learn.java.bootsocialms.post.client.UserClient;
import learn.java.bootsocialms.post.dto.ApiErrorResponse;
import learn.java.bootsocialms.post.dto.PostDetailResponse;
import learn.java.bootsocialms.post.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    private final UserClient userClient;
    private final Tracer tracer;

    public PostController(UserClient userClient, Tracer tracer) {
        this.userClient = userClient;
        this.tracer = tracer;
    }

    @GetMapping("/{id}")
    @SentinelResource(value = "getPostById", blockHandler = "getByIdBlocked")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        Span span = tracer.currentSpan();
        String traceId = span == null ? "-" : span.context().traceId();
        log.info("getPostById id={} trace={}", id, traceId);
        // Day220: 先用 stub post + 远程取作者名，后续接数据库再替换
        long authorId = 1L;
        UserResponse author = userClient.getById(authorId);
        return ResponseEntity.ok(new PostDetailResponse(id, authorId, author.username(), "hello post-" + id));
    }

    public ResponseEntity<ApiErrorResponse> getByIdBlocked(Long id, BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiErrorResponse("RATE_LIMITED", "Too many requests"));
    }
}

