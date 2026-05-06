package learn.java.bootsocial.config;

import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import learn.java.bootsocial.auth.SessionKeys;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 保护帖子相关写接口：发帖 / 评论 / 点赞（Session）。读接口（GET）放行。
 */
@Component
@Profile({"dev", "docker"})
public class AuthInterceptor implements HandlerInterceptor {

    private static final Pattern POST_ID_COMMENTS = Pattern.compile("^/api/posts/[0-9]+/comments$");
    private static final Pattern POST_ID_LIKE = Pattern.compile("^/api/posts/[0-9]+/like$");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            return true;
        }
        String path = normalizePath(request.getRequestURI());
        if (!requiresAuth(method, path)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object uid = session == null ? null : session.getAttribute(SessionKeys.UID);
        if (!(uid instanceof Long)) {
            throw new BizException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "not logged in");
        }
        return true;
    }

    private static String normalizePath(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "";
        }
        if (uri.length() > 1 && uri.endsWith("/")) {
            return uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    static boolean requiresAuth(String method, String path) {
        if (!path.startsWith("/api/posts")) {
            return false;
        }
        if ("POST".equals(method) && "/api/posts".equals(path)) {
            return true;
        }
        if ("POST".equals(method) && POST_ID_COMMENTS.matcher(path).matches()) {
            return true;
        }
        if ("POST".equals(method) && POST_ID_LIKE.matcher(path).matches()) {
            return true;
        }
        if ("DELETE".equals(method) && POST_ID_LIKE.matcher(path).matches()) {
            return true;
        }
        return false;
    }
}
