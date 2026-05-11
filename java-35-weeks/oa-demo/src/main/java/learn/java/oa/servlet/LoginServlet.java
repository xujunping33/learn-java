package learn.java.oa.servlet;

import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import learn.java.oa.api.ApiException;
import learn.java.oa.api.BaseJsonServlet;
import learn.java.oa.api.Jsons;
import learn.java.oa.auth.SessionKeys;
import learn.java.oa.db.Db;
import learn.java.oa.security.Passwords;

public class LoginServlet extends BaseJsonServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());

    private static final int BIZ_BAD_REQUEST = 40001;
    private static final int BIZ_UNAUTHORIZED = 40100;
    private static final int BIZ_FORBIDDEN = 40300;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = Jsons.readBody(req);
        LoginPayload payload;
        try {
            payload = Jsons.fromJson(body, LoginPayload.class);
        } catch (JsonSyntaxException e) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "JSON 格式错误");
        }
        if (payload == null || payload.username == null || payload.password == null) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "需要 username 与 password");
        }
        String username = payload.username.trim();
        String password = payload.password;
        if (username.isEmpty() || password.isEmpty()) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "用户名或密码不能为空");
        }

        try (Connection conn = Db.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                "SELECT id, password_hash, salt, display_name, status FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    LOG.info(() -> "login failed: user not found " + username);
                    throw new ApiException(
                            HttpServletResponse.SC_UNAUTHORIZED, BIZ_UNAUTHORIZED, "用户名或密码错误");
                }
                long userId = rs.getLong("id");
                String hash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                String displayName = rs.getString("display_name");
                int status = rs.getInt("status");
                if (status != 1) {
                    LOG.info(() -> "login failed: disabled " + username);
                    throw new ApiException(HttpServletResponse.SC_FORBIDDEN, BIZ_FORBIDDEN, "账号已禁用");
                }
                String computed = Passwords.md5HexPasswordPlusSalt(password, salt);
                if (!computed.equalsIgnoreCase(hash)) {
                    LOG.info(() -> "login failed: bad password " + username);
                    throw new ApiException(
                            HttpServletResponse.SC_UNAUTHORIZED, BIZ_UNAUTHORIZED, "用户名或密码错误");
                }

                Set<String> roleCodes = new HashSet<>();
                try (PreparedStatement pr =
                                conn.prepareStatement(
                                        "SELECT r.code FROM user_roles ur JOIN roles r ON r.id = ur.role_id"
                                                + " WHERE ur.user_id = ?")) {
                    pr.setLong(1, userId);
                    try (ResultSet rs2 = pr.executeQuery()) {
                        while (rs2.next()) {
                            roleCodes.add(rs2.getString(1));
                        }
                    }
                }

                HttpSession old = req.getSession(false);
                if (old != null) {
                    old.invalidate();
                }
                HttpSession session = req.getSession(true);
                session.setAttribute(SessionKeys.USER_ID, userId);
                session.setAttribute(SessionKeys.USERNAME, username);
                session.setAttribute(SessionKeys.DISPLAY_NAME, displayName);
                session.setAttribute(SessionKeys.ROLE_CODES, roleCodes);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("userId", userId);
                data.put("username", username);
                data.put("displayName", displayName);
                List<String> roles = new ArrayList<>(roleCodes);
                roles.sort(String::compareTo);
                data.put("roles", roles);
                LOG.info(() -> "login ok: " + username + " id=" + userId + " roles=" + roles);
                Jsons.writeOk(resp, data);
            }
        } catch (ApiException e) {
            throw e;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "login sql", e);
            String m = e.getMessage() == null ? "" : e.getMessage();
            String hint;
            if (m.contains("No suitable driver")) {
                hint = "未加载 MySQL JDBC 驱动：请确认 WEB-INF/lib 含 mysql-connector-j，并重新打包部署 war。";
            } else if (m.contains("Access denied")) {
                hint =
                        "数据库拒绝登录：请检查 db.properties 的 db.user/db.password。"
                                + " Ubuntu 上 root 常为 auth_socket，JDBC 无法使用；请为 oa_demo 单独建带密码用户并授权（见 README）。";
            } else if (m.contains("Communications link failure") || m.contains("Connection refused")) {
                hint = "连不上 MySQL：请确认 mysqld 已启动，且 db.url 的主机、端口正确。";
            } else if (m.contains("Unknown database")) {
                hint = "库 oa_demo 不存在：请先在 MySQL 中建库并执行 sql/oa_schema.sql 与 sql/oa_seed.sql。";
            } else {
                hint = "数据库不可用或配置错误（详情见 Tomcat 日志 login sql）。";
            }
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50001, hint);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "login db error", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50001, "数据库不可用或配置错误");
        }
    }

    @SuppressWarnings("unused")
    private static class LoginPayload {
        String username;
        String password;
    }
}
