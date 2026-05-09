package learn.java.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Day119：健康检查 JSON（手写拼接；W18 可换 Jackson）。
 */
@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"ok\":true,\"app\":\"servlet-demo\"}\n");
    }
}
