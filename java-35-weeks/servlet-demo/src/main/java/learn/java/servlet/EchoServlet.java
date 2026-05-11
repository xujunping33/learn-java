package learn.java.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Day116：GET query string / POST form；POST 在读取参数前 {@code setCharacterEncoding("UTF-8")}。
 */
@WebServlet(name = "EchoServlet", urlPatterns = "/echo")
public class EchoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        writeEcho(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 必须在首次读取 body 参数之前（含 getParameter / Parts 等）
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain;charset=UTF-8");
        writeEcho(req, resp, "POST");
    }

    private static void writeEcho(HttpServletRequest req, HttpServletResponse resp, String method)
            throws IOException {
        var out = resp.getWriter();
        out.printf("EchoServlet method=%s%n", method);
        String qs = req.getQueryString();
        out.printf("queryString=%s%n", qs != null ? qs : "(null)");

        Map<String, String[]> raw = req.getParameterMap();
        if (raw.isEmpty()) {
            out.println("(no parameters)");
            return;
        }

        Map<String, String[]> sorted = new TreeMap<>(raw);
        for (Map.Entry<String, String[]> e : sorted.entrySet()) {
            out.printf("%s = %s%n", e.getKey(), Arrays.toString(e.getValue()));
        }

        String[] tags = req.getParameterValues("tag");
        if (tags != null && tags.length > 1) {
            out.printf("%ngetParameter(\"tag\") first value only = %s%n", req.getParameter("tag"));
        }
    }
}
