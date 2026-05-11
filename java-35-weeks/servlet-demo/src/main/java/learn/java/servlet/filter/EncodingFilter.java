package learn.java.servlet.filter;

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

/**
 * Day125：请求 UTF-8 解码；对 {@code text/html}、{@code application/json} 的 {@code Content-Type} 补全
 * {@code ;charset=UTF-8}（若尚未带 charset）。必须在链中调用 {@code chain.doFilter}。
 */
public class EncodingFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(EncodingFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) {
        LOG.info("EncodingFilter#init");
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

    /** 为 html/json 补上 charset，减少「忘了写 charset」导致的乱码。 */
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
        LOG.info("EncodingFilter#destroy");
    }
}
