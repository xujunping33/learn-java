package learn.java.springmvcdemo.web;

import java.util.Map;
import learn.java.springmvcdemo.web.stats.TrafficStatsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Day161：流量统计查询接口。 */
@RestController
public class StatsController {

    private final TrafficStatsService trafficStatsService;

    public StatsController(TrafficStatsService trafficStatsService) {
        this.trafficStatsService = trafficStatsService;
    }

    @GetMapping(path = "/api/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> stats() {
        TrafficStatsService.Snapshot s = trafficStatsService.snapshot();
        return Map.of("total", s.total(), "byPath", s.byPath());
    }
}

