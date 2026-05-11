package learn.java.springmvcdemo.web.stats;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

/** Day161：线程安全 PV 统计（总量 + 按 path）。 */
@Component
public class TrafficStatsService {

    private final LongAdder total = new LongAdder();
    private final ConcurrentHashMap<String, LongAdder> byPath = new ConcurrentHashMap<>();

    public void record(String path) {
        total.increment();
        byPath.computeIfAbsent(path, ignored -> new LongAdder()).increment();
    }

    public Snapshot snapshot() {
        long t = total.sum();

        Map<String, Long> m = new LinkedHashMap<>();
        byPath.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> m.put(e.getKey(), e.getValue().sum()));

        return new Snapshot(t, Collections.unmodifiableMap(m));
    }

    public record Snapshot(long total, Map<String, Long> byPath) {}
}

