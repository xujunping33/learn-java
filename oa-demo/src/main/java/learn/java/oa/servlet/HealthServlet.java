package learn.java.oa.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import learn.java.oa.api.BaseJsonServlet;
import learn.java.oa.api.Jsons;

public class HealthServlet extends BaseJsonServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", "oa-demo");
        data.put("status", "UP");
        data.put("time", Instant.now().toString());
        Jsons.writeOk(resp, data);
    }
}
