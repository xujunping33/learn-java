package learn.java.bootsocialms.gateway;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestTimingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestTimingFilter.class);
    private final Tracer tracer;
    private final Propagator propagator;
    private final Propagator.Getter<HttpHeaders> getter;

    public RequestTimingFilter(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
        this.getter = new Propagator.Getter<>() {
            @Override
            public String get(HttpHeaders carrier, String key) {
                return carrier.getFirst(key);
            }
        };
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        long startNs = System.nanoTime();
        String path = exchange.getRequest().getURI().getRawPath();
        Span span = propagator.extract(exchange.getRequest().getHeaders(), getter)
                .name("gateway")
                .start();

        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder.headers(headers ->
                        propagator.inject(span.context(), headers, (carrier, key, value) -> carrier.set(key, value))))
                .build();

        return Mono.using(
                () -> tracer.withSpan(span),
                scope -> chain.filter(mutated)
                        .doFinally(signalType -> {
                            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
                            Integer status = mutated.getResponse().getStatusCode() == null
                                    ? null
                                    : mutated.getResponse().getStatusCode().value();
                            log.info("gw {} -> status={} {}ms trace={}/{}",
                                    path, status, elapsedMs, span.context().traceId(), span.context().spanId());
                            span.end();
                        }),
                scope -> scope.close()
        );
    }

    @Override
    public int getOrder() {
        // Run right before routing so our injected trace headers "win".
        return NettyRoutingFilter.ORDER - 1;
    }
}

