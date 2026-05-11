package learn.java.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Day117：模拟登录；缺参/空参 → 400；口令错误 → 401；演示不同 {@code Content-Type}。
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    /** 演示用固定口令（真实系统应对密码做哈希+盐，绝不写死在代码里）。 */
    private static final String DEMO_PASSWORD = "demo";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = trimToNull(req.getParameter("user"));
        String pass = trimToNull(req.getParameter("pass"));

        if (user == null || pass == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write("400 Bad Request：需要 query 参数 user 与 pass（均为非空）。\n");
            return;
        }

        if (!DEMO_PASSWORD.equals(pass)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"ok\":false,\"message\":\"wrong password\"}\n");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().printf(
                "<!DOCTYPE html><html lang=\"zh-CN\"><meta charset=\"UTF-8\"/>"
                        + "<title>login ok</title><body><p>200 OK：欢迎 %s（仅课堂模拟）。</p></body></html>%n",
                escapeHtml(user));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
