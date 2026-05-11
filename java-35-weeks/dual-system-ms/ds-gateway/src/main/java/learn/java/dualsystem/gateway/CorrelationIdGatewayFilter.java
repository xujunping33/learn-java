package learn.java.dualsystem.gateway;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Day238: propagate or generate {@code X-Request-Id} to downstream services (MDC there). Echoes the same id on the
 * response for clients.
 */
@Component
public class CorrelationIdGatewayFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-Id";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGatewayFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String existing = exchange.getRequest().getHeaders().getFirst(HEADER);
        String id = (existing == null || existing.isBlank()) ? UUID.randomUUID().toString() : existing.trim();

        ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER, id).build();
        ServerWebExchange rewritten = exchange.mutate().request(request).build();
        rewritten.getResponse().getHeaders().set(HEADER, id);

        if (log.isDebugEnabled()) {
            log.debug("{} {} {}", id, request.getMethod(), request.getURI().getPath());
        }
        return chain.filter(rewritten);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
