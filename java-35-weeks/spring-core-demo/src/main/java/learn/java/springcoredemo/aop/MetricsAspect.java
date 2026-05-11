package learn.java.springcoredemo.aop;

import java.util.concurrent.atomic.AtomicLong;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Week22 Day152：与 {@link LoggingAspect} 共用切点；{@code @Order} 更小者<strong>外层</strong>包裹（先进入后退出）。
 * <p>
 * {@code @Around} 累计耗时与调用次数；{@code @AfterThrowing} 累计异常次数。
 */
@Aspect
@Component
@Order(1)
public class MetricsAspect {

    private final AtomicLong invocations = new AtomicLong();
    private final AtomicLong failureEvents = new AtomicLong();
    private final AtomicLong totalLatencyNs = new AtomicLong();

    @Pointcut(
            "execution(* learn.java.springcoredemo..service..*.*(..))"
                    + " || execution(* learn.java.springcoredemo..tx..*.*(..))"
                    + " || execution(* learn.java.springcoredemo..audit..*.*(..))"
                    + " || execution(* learn.java.springcoredemo..batch..*.*(..))")
    public void applicationLayer() {}

    @Around("applicationLayer()")
    public Object recordLatency(ProceedingJoinPoint pjp) throws Throwable {
        invocations.incrementAndGet();
        long t0 = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            totalLatencyNs.addAndGet(System.nanoTime() - t0);
        }
    }

    @AfterThrowing(pointcut = "applicationLayer()", throwing = "ex")
    public void recordFailure(Throwable ex) {
        failureEvents.incrementAndGet();
    }

    public void reset() {
        invocations.set(0);
        failureEvents.set(0);
        totalLatencyNs.set(0);
    }

    public void printSnapshot(String label) {
        long n = invocations.get();
        long ms = n == 0 ? 0 : totalLatencyNs.get() / 1_000_000L;
        double avg = n == 0 ? 0 : ms / (double) n;
        System.out.println(
                "[METRICS] "
                        + label
                        + " invocations="
                        + n
                        + " afterThrowingFailures="
                        + failureEvents.get()
                        + " totalMs="
                        + ms
                        + " avgMs="
                        + String.format(java.util.Locale.ROOT, "%.3f", avg));
    }
}
