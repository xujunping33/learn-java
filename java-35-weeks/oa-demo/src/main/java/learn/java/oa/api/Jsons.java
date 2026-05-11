package learn.java.oa.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class Jsons {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    private Jsons() {}

    public static void write(HttpServletResponse resp, int httpStatus, int code, String message, Object data)
            throws IOException {
        resp.setStatus(httpStatus);
        resp.setContentType("application/json");
        GSON.toJson(new ApiResult(code, message, data), resp.getWriter());
    }

    public static void writeOk(HttpServletResponse resp, Object data) throws IOException {
        write(resp, HttpServletResponse.SC_OK, 0, "OK", data);
    }

    /** 与 {@link #writeOk} 相同结构的 JSON 字符串（供 Redis 旁路缓存写入）。 */
    public static String toJsonOk(Object data) {
        return GSON.toJson(new ApiResult(0, "OK", data));
    }

    public static String readBody(HttpServletRequest req) throws IOException {
        byte[] raw = req.getInputStream().readAllBytes();
        return new String(raw, StandardCharsets.UTF_8);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }
}
