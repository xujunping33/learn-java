package learn.java.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Day118：全应用共享（{@link ServletContext}）计数；所有用户、所有会话累加同一计数器。
 */
@WebServlet(name = "GlobalCounterServlet", urlPatterns = "/global-counter")
public class GlobalCounterServlet extends HttpServlet {

    private static final String CTX_ATTR = "learn.java.servlet.DAY118_GLOBAL_HITS";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AtomicLong hits = getOrCreateGlobalCounter(getServletContext());
        long total = hits.incrementAndGet();

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter()
                .printf(
                        "ServletContext global counter（全用户共享）%n"
                                + "totalHits=%d%n"
                                + "对比：多开无痕/多浏览器，数字仍连续累加。%n",
                        total);
    }

    private static AtomicLong getOrCreateGlobalCounter(ServletContext ctx) {
        Object existing = ctx.getAttribute(CTX_ATTR);
        if (existing instanceof AtomicLong) {
            return (AtomicLong) existing;
        }
        synchronized (ctx) {
            existing = ctx.getAttribute(CTX_ATTR);
            if (existing instanceof AtomicLong) {
                return (AtomicLong) existing;
            }
            AtomicLong created = new AtomicLong(0);
            ctx.setAttribute(CTX_ATTR, created);
            return created;
        }
    }
}
