package learn.java.oa.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import learn.java.oa.api.Jsons;
import learn.java.oa.auth.SessionKeys;

/**
 * Day131：除白名单外，访问 {@code /api/*} 须已登录；{@code /api/admin/*} 须具备 {@code ADMIN} 角色。
 */
public class AuthFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(AuthFilter.class.getName());

    private static final int BIZ_UNAUTHORIZED = 40101;
    private static final int BIZ_FORBIDDEN = 40301;

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.fine("AuthFilter#init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest req) || !(response instanceof HttpServletResponse resp)) {
            chain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ctx = req.getContextPath();
        String uri = req.getRequestURI();
        if (!uri.startsWith(ctx)) {
            chain.doFilter(request, response);
            return;
        }
        String path = uri.substring(ctx.length());

        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        if (isPublicApi(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (readUserId(session) == null) {
            LOG.fine(() -> "auth reject (no session): " + path);
            Jsons.write(resp, HttpServletResponse.SC_UNAUTHORIZED, BIZ_UNAUTHORIZED, "未登录：请先 POST /api/login", null);
            return;
        }

        if (path.startsWith("/api/admin/")) {
            if (!hasAdminRole(session)) {
                LOG.fine(() -> "auth reject (not admin): " + path);
                Jsons.write(resp, HttpServletResponse.SC_FORBIDDEN, BIZ_FORBIDDEN, "需要管理员角色（ADMIN）", null);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private static Long readUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object uid = session.getAttribute(SessionKeys.USER_ID);
        if (uid instanceof Long l) {
            return l;
        }
        if (uid instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    private static boolean isPublicApi(String path) {
        return path.equals("/api/health") || path.equals("/api/login");
    }

    private static boolean hasAdminRole(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object raw = session.getAttribute(SessionKeys.ROLE_CODES);
        if (!(raw instanceof Set<?> set)) {
            return false;
        }
        for (Object o : set) {
            if ("ADMIN".equals(String.valueOf(o))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        LOG.fine("AuthFilter#destroy");
    }
}
