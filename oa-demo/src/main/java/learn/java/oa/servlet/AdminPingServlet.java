package learn.java.oa.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import learn.java.oa.api.BaseJsonServlet;
import learn.java.oa.api.Jsons;

/** Day131：占位管理接口，用于验证「仅 ADMIN 可访问 /api/admin/*」。 */
public class AdminPingServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("scope", "admin");
        data.put("ok", true);
        Jsons.writeOk(resp, data);
    }
}
