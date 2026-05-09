package learn.java.ssmsocial.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import learn.java.ssmsocial.auth.SessionKeys;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // allow CORS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // read-only APIs do not require login
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object uid = session == null ? null : session.getAttribute(SessionKeys.UID);
        if (uid instanceof Long) {
            return true;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"not logged in\"}");
        return false;
    }
}

