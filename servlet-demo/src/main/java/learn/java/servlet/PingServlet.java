package learn.java.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Day114：无 {@code @WebServlet}，仅在 {@code WEB-INF/web.xml} 中映射 {@code /ping}。 */
public class PingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().write("pong (mapped via web.xml only)\n");
    }
}
