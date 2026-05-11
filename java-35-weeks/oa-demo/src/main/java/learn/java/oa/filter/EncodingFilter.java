package learn.java.oa.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.logging.Logger;

/** UTF-8；为 {@code application/json} 等补全 {@code ;charset=UTF-8}。 */
public class EncodingFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(EncodingFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.fine("EncodingFilter#init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        if (!(response instanceof HttpServletResponse hres)) {
            chain.doFilter(request, response);
            return;
        }
        hres.setCharacterEncoding("UTF-8");
        HttpServletResponse wrapped =
                new HttpServletResponseWrapper(hres) {
                    @Override
                    public void setContentType(String type) {
                        super.setContentType(withUtf8Charset(type));
                    }
                };
        chain.doFilter(request, wrapped);
    }

    static String withUtf8Charset(String type) {
        if (type == null) {
            return null;
        }
        String t = type.trim();
        String lower = t.toLowerCase();
        if (lower.startsWith("text/html") || lower.startsWith("application/json")) {
            if (!lower.contains("charset")) {
                return t + ";charset=UTF-8";
            }
        }
        return t;
    }

    @Override
    public void destroy() {
        LOG.fine("EncodingFilter#destroy");
    }
}
