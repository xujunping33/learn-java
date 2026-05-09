package learn.java.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import learn.java.servlet.model.NewStudentPayload;
import learn.java.servlet.model.Student;
import learn.java.servlet.validation.StudentValidation;

/**
 * Day121 GET；Day122 POST；Day123 DELETE/PUT；Day124 前后端正则双保险校验。
 */
@WebServlet(name = "StudentsApiServlet", urlPatterns = "/api/students")
public class StudentsApiServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static final CopyOnWriteArrayList<Student> STORE = new CopyOnWriteArrayList<>();

    private static final Object STORE_LOCK = new Object();

    static {
        STORE.add(new Student(1, "张三", 88, 18, "13800138001"));
        STORE.add(new Student(2, "李四", 92, 19, "13912345678"));
        STORE.add(new Student(3, "王五", 76, 18, "15600001111"));
    }

    private static long nextId() {
        long max = 0;
        for (Student s : STORE) {
            max = Math.max(max, s.id);
        }
        return max + 1;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        GSON.toJson(List.copyOf(STORE), resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String body = readBody(req, resp);
        if (body == null) {
            return;
        }

        NewStudentPayload payload;
        try {
            payload = GSON.fromJson(body, NewStudentPayload.class);
        } catch (JsonSyntaxException e) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON 格式错误");
            return;
        }

        if (payload == null) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "无法解析为对象");
            return;
        }

        String err = validatePayload(payload);
        if (err != null) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, err);
            return;
        }

        String name = payload.name.trim();
        String phone = payload.phone.trim();

        Student created;
        synchronized (STORE_LOCK) {
            created = new Student(nextId(), name, payload.score, payload.age, phone);
            STORE.add(created);
        }

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json;charset=UTF-8");
        GSON.toJson(created, resp.getWriter());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String body = readBody(req, resp);
        if (body == null) {
            return;
        }

        Student incoming;
        try {
            incoming = GSON.fromJson(body, Student.class);
        } catch (JsonSyntaxException e) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "JSON 格式错误");
            return;
        }

        if (incoming == null || incoming.id <= 0) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "body 必须包含正整数 id");
            return;
        }

        String name = incoming.name == null ? "" : incoming.name.trim();
        String phone = incoming.phone == null ? "" : incoming.phone.trim();

        String err = StudentValidation.validateName(name);
        if (err == null) {
            err = StudentValidation.validateScore(incoming.score);
        }
        if (err == null) {
            err = StudentValidation.validateAge(incoming.age);
        }
        if (err == null) {
            err = StudentValidation.validatePhone(phone);
        }
        if (err != null) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, err);
            return;
        }

        Student updated = new Student(incoming.id, name, incoming.score, incoming.age, phone);

        synchronized (STORE_LOCK) {
            boolean found = false;
            for (int i = 0; i < STORE.size(); i++) {
                if (STORE.get(i).id == updated.id) {
                    STORE.set(i, updated);
                    found = true;
                    break;
                }
            }
            if (!found) {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "未找到 id=" + updated.id);
                return;
            }
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        GSON.toJson(updated, resp.getWriter());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");

        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "需要 query 参数 id");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "id 必须为数字");
            return;
        }

        synchronized (STORE_LOCK) {
            boolean removed = STORE.removeIf(s -> s.id == id);
            if (!removed) {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "未找到 id=" + id);
                return;
            }
        }

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private static String validatePayload(NewStudentPayload payload) {
        String name = payload.name == null ? "" : payload.name.trim();
        String phone = payload.phone == null ? "" : payload.phone.trim();

        String err = StudentValidation.validateName(name);
        if (err != null) {
            return err;
        }
        if (payload.score == null || payload.age == null) {
            return "score 与 age 必填（数字）";
        }
        err = StudentValidation.validateScore(payload.score);
        if (err != null) {
            return err;
        }
        err = StudentValidation.validateAge(payload.age);
        if (err != null) {
            return err;
        }
        return StudentValidation.validatePhone(phone);
    }

    private static String readBody(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String body = req.getReader().lines().collect(Collectors.joining("\n"));
            if (body == null || body.isBlank()) {
                writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "请求体为空");
                return null;
            }
            return body;
        } catch (IOException e) {
            writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "无法读取请求体");
            return null;
        }
    }

    private static void writeJsonError(HttpServletResponse resp, int status, String message)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().printf("{\"error\":%s}%n", GSON.toJson(message));
    }
}
