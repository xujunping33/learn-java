package learn.java.bootsocialms.post.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.stereotype.Component;

@Component
public class TracingFeignRequestInterceptor implements RequestInterceptor {

    private final Tracer tracer;
    private final Propagator propagator;

    public TracingFeignRequestInterceptor(Tracer tracer, Propagator propagator) {
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Override
    public void apply(RequestTemplate template) {
        Span span = tracer.currentSpan();
        if (span == null) {
            return;
        }
        propagator.inject(span.context(), template, new Propagator.Setter<>() {
            @Override
            public void set(RequestTemplate carrier, String key, String value) {
                carrier.header(key, value);
            }
        });
    }
}

