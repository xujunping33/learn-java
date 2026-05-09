package learn.java.bootsocial.observability;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 通知队列消费计数（W30 Day209）：内存指标，供 {@code /actuator/info} 与排障。
 */
@Component
@Profile({"dev", "docker"})
public class NotifyConsumeMetrics {

    private final AtomicLong successTotal = new AtomicLong();
    private final AtomicLong failureTotal = new AtomicLong();

    public void recordSuccess() {
        successTotal.incrementAndGet();
    }

    public void recordFailure() {
        failureTotal.incrementAndGet();
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("notifyConsumeSuccessTotal", successTotal.get());
        m.put("notifyConsumeFailureTotal", failureTotal.get());
        return m;
    }
}
