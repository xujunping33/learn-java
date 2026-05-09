package learn.java.bootsocial.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import learn.java.bootsocial.mq.MqConfiguration;
import learn.java.bootsocial.web.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "Dev DLQ", description = "仅 dev：DLQ 重放")
@RestController
@RequestMapping("/api/dev/dlq")
@Profile({"dev", "docker"})
public class DevDlqController {

    private static final Logger log = LoggerFactory.getLogger(DevDlqController.class);

    private final RabbitTemplate rabbitTemplate;

    public DevDlqController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Operation(summary = "查看一条 DLQ 消息（会取出再放回；仅用于排障）")
    @GetMapping("/peek")
    public ApiResult<Map<String, Object>> peek() {
        Message msg = rabbitTemplate.receive(MqConfiguration.DLQ_QUEUE);
        if (msg == null) {
            return ApiResult.ok(Map.of("empty", true));
        }
        MessageProperties props = msg.getMessageProperties();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("empty", false);
        out.put("contentType", props.getContentType());
        out.put("contentEncoding", props.getContentEncoding());
        out.put("headers", sanitizeHeaders(props.getHeaders()));
        out.put("bodyPreview", new String(msg.getBody(), StandardCharsets.UTF_8));

        // put it back to DLQ queue for later replay
        rabbitTemplate.send("", MqConfiguration.DLQ_QUEUE, msg);
        return ApiResult.ok(out);
    }

    @Operation(summary = "从 DLQ 拉取并重放（最小演示）")
    @PostMapping("/replay")
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<Integer> replay(@RequestParam(defaultValue = "10") int limit) {
        int n = 0;
        int max = Math.max(0, Math.min(limit, 200));
        for (int i = 0; i < max; i++) {
            Message msg = rabbitTemplate.receive(MqConfiguration.DLQ_QUEUE);
            if (msg == null) {
                break;
            }
            MessageProperties props = msg.getMessageProperties();
            String originalExchange = asString(props.getHeaders().get("x-original-exchange"));
            String originalRoutingKey = asString(props.getHeaders().get("x-original-routingKey"));
            if (originalExchange == null || originalRoutingKey == null) {
                // Can't safely replay without target; drop for manual inspection.
                log.warn("dlq replay drop: missing original route. headers={}", props.getHeaders());
                continue;
            }
            log.info(
                    "dlq replay -> exchange={} routingKey={} headersKeys={}",
                    originalExchange,
                    originalRoutingKey,
                    props.getHeaders().keySet());
            rabbitTemplate.send(originalExchange, originalRoutingKey, msg);
            n++;
        }
        return ApiResult.ok(n);
    }

    private static String asString(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof String s) {
            return s;
        }
        return String.valueOf(v);
    }

    private static Map<String, Object> sanitizeHeaders(Map<String, Object> headers) {
        Map<String, Object> out = new LinkedHashMap<>();
        headers.forEach(
                (k, v) -> {
                    if (v == null) {
                        out.put(k, null);
                        return;
                    }
                    if (v instanceof byte[] bytes) {
                        out.put(k, "bytes(len=" + bytes.length + ")");
                        return;
                    }
                    // LongString / Date / Integer ... convert to safe JSON scalar
                    out.put(k, String.valueOf(v));
                });
        return out;
    }
}

