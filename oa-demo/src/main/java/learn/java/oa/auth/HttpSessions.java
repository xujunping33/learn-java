package learn.java.oa.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import learn.java.oa.api.ApiException;

/** 从 Session 读取当前登录用户（与 {@link learn.java.oa.servlet.LoginServlet} 写入一致）。 */
public final class HttpSessions {

    private HttpSessions() {}

    public static Long userId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object uid = session.getAttribute(SessionKeys.USER_ID);
        if (uid instanceof Long l) {
            return l;
        }
        if (uid instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    public static long requireUserId(HttpServletRequest req) {
        Long id = userId(req);
        if (id == null) {
            throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, 40101, "未登录：请先 POST /api/login");
        }
        return id;
    }
}
