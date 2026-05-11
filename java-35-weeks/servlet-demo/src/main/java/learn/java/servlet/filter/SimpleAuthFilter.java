package learn.java.servlet.filter;

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
import java.util.logging.Logger;

/**
 * Day126：保护 {@code /api/*}（除 {@code /api/login}）；Session 无 {@code user} 时返回 401 JSON，不进入后续链。
 */
public class SimpleAuthFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(SimpleAuthFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("SimpleAuthFilter#init");
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

        if (path.equals("/api/login") || path.equals("/api/ping")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object user = session != null ? session.getAttribute("user") : null;
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter()
                    .write(
                            "{\"error\":\"未登录：请先打开 static/login.html 执行登录（POST /api/login）\"}\n");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOG.info("SimpleAuthFilter#destroy");
    }
}
