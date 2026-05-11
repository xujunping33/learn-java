package learn.java.springmvcdemo.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Day160：记录方法、URI、状态码与耗时（afterCompletion）。 */
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String ATTR_T0_NS = RequestLoggingInterceptor.class.getName() + ".t0Ns";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(ATTR_T0_NS, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex) {
        Object t0 = request.getAttribute(ATTR_T0_NS);
        long ms = -1L;
        if (t0 instanceof Long start) {
            ms = (System.nanoTime() - start) / 1_000_000L;
        }
        System.out.println(
                "[INTERCEPTOR] "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
                        + " -> "
                        + response.getStatus()
                        + " "
                        + ms
                        + "ms");
    }
}
