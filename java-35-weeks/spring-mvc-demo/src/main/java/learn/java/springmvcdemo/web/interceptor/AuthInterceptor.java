package learn.java.springmvcdemo.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Day160：简易 API Key 鉴权。未带或错误 {@value ApiKeyConstants#HEADER} → 401 JSON（与 {@code ApiErrorBody} 同形）。
 * CORS 预检 {@code OPTIONS} 不落 Key，在此放行。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String UNAUTHORIZED_JSON =
            "{\"code\":\"UNAUTHORIZED\",\"message\":\"missing or invalid X-Api-Key\"}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String key = request.getHeader(ApiKeyConstants.HEADER);
        if (ApiKeyConstants.DEMO_VALUE.equals(key)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(UNAUTHORIZED_JSON);
        return false;
    }
}
