package learn.java.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/** Day126：记录 method、URI、queryString（在业务 Servlet 之前）。 */
public class RequestLoggingFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("RequestLoggingFilter#init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            String q = req.getQueryString();
            LOG.info(() -> req.getMethod() + " " + req.getRequestURI() + (q != null ? " ?" + q : ""));
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOG.info("RequestLoggingFilter#destroy");
    }
}
