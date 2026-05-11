package learn.java.dualsystem.gateway;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ReportApiKeyGatewayFilter implements GlobalFilter, Ordered {

    private static final String HEADER = "X-Report-Key";

    @Value("${report.api-key:}")
    private String configuredKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (configuredKey == null || configuredKey.isBlank()) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        if (!isReportPath(path)) {
            return chain.filter(exchange);
        }
        String provided = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (!constantTimeEquals(configuredKey, provided)) {
            byte[] body = "{\"code\":\"UNAUTHORIZED\",\"message\":\"missing or invalid X-Report-Key\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var buffer = exchange.getResponse().bufferFactory().wrap(body);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }

    private static boolean isReportPath(String path) {
        return path.startsWith("/api/reports/") || path.startsWith("/api/admin/reports/");
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = actual.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length; i++) {
            r |= a[i] ^ b[i];
        }
        return r == 0;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
