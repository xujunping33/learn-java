package learn.java.oa.servlet;

import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import learn.java.oa.api.ApiException;
import learn.java.oa.api.BaseJsonServlet;
import learn.java.oa.api.Jsons;
import learn.java.oa.auth.HttpSessions;
import learn.java.oa.cache.LeavesMeRedisCache;
import learn.java.oa.db.Db;
import learn.java.oa.service.Permissions;

/**
 * Day132：提交请假（{@code SUBMITTED}）+ 写 {@code leave_actions}（{@code SUBMIT}），同事务。
 */
public class LeaveSubmitServlet extends BaseJsonServlet {

    private static final Logger LOG = Logger.getLogger(LeaveSubmitServlet.class.getName());

    private static final int BIZ_BAD_REQUEST = 40002;
    private static final int BIZ_FORBIDDEN = 40302;
    private static final int BIZ_CONFLICT = 40902;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long actorId = HttpSessions.requireUserId(req);
        String body = Jsons.readBody(req);
        SubmitPayload p;
        try {
            p = Jsons.fromJson(body, SubmitPayload.class);
        } catch (JsonSyntaxException e) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "JSON 格式错误");
        }
        if (p == null) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "请求体不能为空");
        }
        String err = validate(p);
        if (err != null) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, err);
        }

        LocalDateTime start;
        LocalDateTime end;
        try {
            start = parseStartDateTime(p.startAt.trim());
            end = parseEndDateTime(p.endAt.trim());
        } catch (DateTimeParseException e) {
            throw new ApiException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    BIZ_BAD_REQUEST,
                    "startAt/endAt 格式错误：可为 ISO 日期时间（如 2026-06-01T09:00:00）或仅日期（如 2026-06-01；结束日按当天 23:59:59）");
        }
        if (!end.isAfter(start)) {
            throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "结束时间须晚于开始时间");
        }

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!Permissions.hasPermission(conn, actorId, "leave:submit")) {
                    throw new ApiException(HttpServletResponse.SC_FORBIDDEN, BIZ_FORBIDDEN, "无提交请假权限");
                }

                Long deptId;
                Long assigneeUserId;
                try (PreparedStatement ps =
                                conn.prepareStatement(
                                        "SELECT e.dept_id, um.id AS manager_user_id"
                                                + " FROM employees e"
                                                + " LEFT JOIN employees mgr ON mgr.id = e.manager_employee_id"
                                                + " LEFT JOIN users um ON um.id = mgr.user_id"
                                                + " WHERE e.user_id = ?")) {
                    ps.setLong(1, actorId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ApiException(
                                    HttpServletResponse.SC_CONFLICT,
                                    BIZ_CONFLICT,
                                    "当前用户未关联员工档案，无法提交请假");
                        }
                        deptId = rs.getLong("dept_id");
                        long mgrUid = rs.getLong("manager_user_id");
                        assigneeUserId = rs.wasNull() ? null : mgrUid;
                    }
                }

                if (assigneeUserId == null) {
                    throw new ApiException(
                            HttpServletResponse.SC_CONFLICT, BIZ_CONFLICT, "未配置直属上级，无法进入待审流程");
                }
                long assignee = assigneeUserId;

                long leaveId;
                try (PreparedStatement ps =
                                conn.prepareStatement(
                                        "INSERT INTO leave_requests (applicant_user_id, dept_id, leave_type,"
                                                + " start_at, end_at, reason, status, current_assignee_user_id,"
                                                + " version) VALUES (?,?,?,?,?,?,?,?,0)",
                                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, actorId);
                    ps.setLong(2, deptId);
                    ps.setString(3, p.leaveType.trim());
                    ps.setTimestamp(4, Timestamp.valueOf(start));
                    ps.setTimestamp(5, Timestamp.valueOf(end));
                    ps.setString(6, p.reason.trim());
                    ps.setString(7, "SUBMITTED");
                    ps.setLong(8, assignee);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("no generated key");
                        }
                        leaveId = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps =
                                conn.prepareStatement(
                                        "INSERT INTO leave_actions (leave_request_id, actor_user_id, action,"
                                                + " remark) VALUES (?,?,?,?)")) {
                    ps.setLong(1, leaveId);
                    ps.setLong(2, actorId);
                    ps.setString(3, "SUBMIT");
                    ps.setString(4, null);
                    ps.executeUpdate();
                }

                conn.commit();
                LeavesMeRedisCache.invalidateUser(actorId);
                LOG.info(() -> "leave submitted id=" + leaveId + " by user=" + actorId);
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("id", leaveId);
                data.put("status", "SUBMITTED");
                data.put("currentAssigneeUserId", assignee);
                Jsons.writeOk(resp, data);
            } catch (ApiException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                LOG.log(Level.WARNING, "leave submit tx", e);
                throw new ApiException(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50003, "提交请假失败：" + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "leave submit", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50003, "数据库错误");
        }
    }

    /** 支持 `2026-06-01T09:00:00` 或仅日期 `2026-06-01`（当天 0 点起）。 */
    private static LocalDateTime parseStartDateTime(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(s).atStartOfDay();
        }
    }

    /** 支持带时间字符串；仅日期时视为该日 **`23:59:59`**，避免与开始同日零点相等导致「结束须晚于开始」失败。 */
    private static LocalDateTime parseEndDateTime(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException ignored) {
            return LocalDate.parse(s).atTime(23, 59, 59);
        }
    }

    private static String validate(SubmitPayload p) {
        if (p.leaveType == null || p.leaveType.isBlank()) {
            return "需要 leaveType";
        }
        String lt = p.leaveType.trim();
        if (!lt.equals("ANNUAL") && !lt.equals("SICK") && !lt.equals("OTHER")) {
            return "leaveType 须为 ANNUAL / SICK / OTHER";
        }
        if (p.startAt == null || p.startAt.isBlank() || p.endAt == null || p.endAt.isBlank()) {
            return "需要 startAt 与 endAt（如 2026-06-01T09:00:00 或仅日期 2026-06-01）";
        }
        if (p.reason == null || p.reason.isBlank()) {
            return "需要 reason";
        }
        if (p.reason.length() > 500) {
            return "reason 过长";
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static class SubmitPayload {
        String leaveType;
        String startAt;
        String endAt;
        String reason;
    }
}
