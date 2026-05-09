package learn.java.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Day118：按浏览器会话（Session，通常靠 Cookie {@code JSESSIONID}）计数；刷新递增。
 */
@WebServlet(name = "CounterServlet", urlPatterns = "/counter")
public class CounterServlet extends HttpServlet {

    private static final String SESSION_ATTR = "day118.sessionHitCount";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(true);
        Integer count = (Integer) session.getAttribute(SESSION_ATTR);
        if (count == null) {
            count = 0;
        }
        count++;
        session.setAttribute(SESSION_ATTR, count);

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter()
                .printf(
                        "Session counter（刷新 +1）%n"
                                + "sessionId=%s%n"
                                + "count=%d%n"
                                + "对比：普通窗口 vs 无痕窗口，应各自从 1 开始。%n",
                        session.getId(),
                        count);
    }
}
