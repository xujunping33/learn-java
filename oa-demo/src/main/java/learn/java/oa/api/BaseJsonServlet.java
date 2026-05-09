package learn.java.oa.api;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 统一将 {@link ApiException} 与未预期异常转为 {@link ApiResult} JSON。
 */
public abstract class BaseJsonServlet extends HttpServlet {

    private final Logger log = Logger.getLogger(getClass().getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (ApiException e) {
            Jsons.write(resp, e.getHttpStatus(), e.getBizCode(), e.getMessage(), null);
        } catch (Exception e) {
            log.log(Level.SEVERE, req.getMethod() + " " + req.getRequestURI(), e);
            Jsons.write(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50000, "服务器内部错误", null);
        }
    }
}
