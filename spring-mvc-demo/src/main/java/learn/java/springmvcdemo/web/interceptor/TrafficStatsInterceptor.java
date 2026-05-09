package learn.java.springmvcdemo.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import learn.java.springmvcdemo.web.stats.TrafficStatsService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Day161：按请求 URI 做 PV 统计（放行所有请求；不统计 CORS 预检 OPTIONS）。 */
@Component
public class TrafficStatsInterceptor implements HandlerInterceptor {

    private final TrafficStatsService trafficStatsService;

    public TrafficStatsInterceptor(TrafficStatsService trafficStatsService) {
        this.trafficStatsService = trafficStatsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
            trafficStatsService.record(request.getRequestURI());
        }
        return true;
    }
}

