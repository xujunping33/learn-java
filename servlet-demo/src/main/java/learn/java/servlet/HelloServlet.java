package learn.java.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 * Day114：注解映射 {@code /hello}；日志观察 init / service / destroy。
 * Day115：引用 commons-lang3，验证依赖落在 war 的 {@code WEB-INF/lib}。
 */
@WebServlet(name = "HelloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HelloServlet.class.getName());

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LOG.info("HelloServlet#init (每个 Servlet 实例一次，通常首次请求前或容器启动时)");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.info("HelloServlet#service method=" + req.getMethod());
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        String libDemo = StringUtils.abbreviate(
                "commons-lang3 lives under WEB-INF/lib in the packaged war", 48);
        resp.getWriter().printf(
                "Hello from HelloServlet%nInstant=%s%nWEB-INF/lib demo: %s%n%nSee Tomcat logs for init/service lines.%n",
                Instant.now(),
                libDemo);
    }

    @Override
    public void destroy() {
        LOG.info("HelloServlet#destroy");
        super.destroy();
    }
}
