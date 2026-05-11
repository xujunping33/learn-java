package learn.java.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import learn.java.servlet.model.LoginPayload;

/**
 * Day126：POST {@code /api/login}，校验口令后在 {@link HttpSession} 写入 {@code user}，供 {@code SimpleAuthFilter} 使用。
 * （与 Day117 {@code /login} 查询串演示区分：本接口走 Session + JSON。）
 */
@WebServlet(name = "ApiLoginServlet", urlPatterns = "/api/login")
public class ApiLoginServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final String DEMO_PASSWORD = "demo";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"error\":\"use POST with JSON {\\\"user\\\",\\\"pass\\\"}\"}\n");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String body;
        try {
            body = req.getReader().lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            writeErr(resp, HttpServletResponse.SC_BAD_REQUEST, "无法读取请求体");
            return;
        }
        if (body == null || body.isBlank()) {
            writeErr(resp, HttpServletResponse.SC_BAD_REQUEST, "请求体为空");
            return;
        }

        LoginPayload p;
        try {
            p = GSON.fromJson(body, LoginPayload.class);
        } catch (JsonSyntaxException e) {
            writeErr(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON 格式错误");
            return;
        }
        if (p == null || p.user == null || p.pass == null) {
            writeErr(resp, HttpServletResponse.SC_BAD_REQUEST, "需要 user 与 pass");
            return;
        }

        String user = p.user.trim();
        if (user.isEmpty()) {
            writeErr(resp, HttpServletResponse.SC_BAD_REQUEST, "user 不能为空");
            return;
        }
        if (!DEMO_PASSWORD.equals(p.pass)) {
            writeErr(resp, HttpServletResponse.SC_UNAUTHORIZED, "口令错误");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("ok", true);
        ok.put("user", user);
        GSON.toJson(ok, resp.getWriter());
    }

    private static void writeErr(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().printf("{\"error\":%s}%n", GSON.toJson(msg));
    }
}
