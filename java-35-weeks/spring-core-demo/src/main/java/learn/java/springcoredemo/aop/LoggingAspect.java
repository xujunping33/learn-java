package learn.java.springcoredemo.aop;

import java.util.Arrays;
import java.util.Collection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class LoggingAspect {

    @Pointcut(
            "execution(* learn.java.springcoredemo..service..*.*(..))"
                    + " || execution(* learn.java.springcoredemo..tx..*.*(..))"
                    + " || execution(* learn.java.springcoredemo..audit..*.*(..))"
                    + " || execution(* learn.java.springcoredemo..batch..*.*(..))")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.nanoTime();
        String sig = pjp.getSignature().toShortString();
        Object[] args = pjp.getArgs();
        try {
            Object result = pjp.proceed();
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            System.out.println("[AOP] " + ms + "ms " + sig + " args=" + Arrays.toString(args) + " -> " + summarize(result));
            return result;
        } catch (Throwable ex) {
            long ms = (System.nanoTime() - t0) / 1_000_000L;
            System.out.println("[AOP] " + ms + "ms " + sig + " args=" + Arrays.toString(args) + " FAILED: " + ex);
            throw ex;
        }
    }

    private static String summarize(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof Collection<?> c) {
            return c.getClass().getSimpleName() + "(size=" + c.size() + ")";
        }
        return String.valueOf(result);
    }
}
