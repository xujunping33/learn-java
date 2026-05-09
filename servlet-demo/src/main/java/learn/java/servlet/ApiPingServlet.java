package learn.java.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Day120：{@code GET /api/ping}，{@code Content-Type: application/json}，Gson 输出；含中文字段便于 Network 验编码。
 */
@WebServlet(name = "ApiPingServlet", urlPatterns = "/api/ping")
public class ApiPingServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("msg", "就绪");

        GSON.toJson(body, resp.getWriter());
    }
}
