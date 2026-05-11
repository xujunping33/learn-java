package learn.java.oa.servlet;

import com.google.gson.JsonSyntaxException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import learn.java.oa.api.ApiException;
import learn.java.oa.api.BaseJsonServlet;
import learn.java.oa.api.Jsons;
import learn.java.oa.auth.HttpSessions;
import learn.java.oa.cache.LeavesMeRedisCache;
import learn.java.oa.db.Db;
import learn.java.oa.service.Permissions;

/**
 * Day132：经理审批通过 / 驳回；状态更新 + {@code leave_actions} 同事务。
 *
 * <p>TODO（并发收紧）：前端携带 {@code version} 与乐观锁重试；同一单多人同时点「通过」时仅靠 DB 行数校验已能挡一道，仍可加强重试 UX。
 */
public class LeaveDecisionServlet extends BaseJsonServlet {

    private static final Logger LOG = Logger.getLogger(LeaveDecisionServlet.class.getName());

    private static final Pattern PATH =
            Pattern.compile("^/api/leaves/(\\d+)/(approve|reject)$");

    private static final int BIZ_BAD_REQUEST = 40003;
    private static final int BIZ_FORBIDDEN_APPROVE = 40305;
    private static final int BIZ_FORBIDDEN_REJECT = 40306;
    private static final int BIZ_NOT_ASSIGNEE = 40304;
    private static final int BIZ_NOT_FOUND = 40402;
    private static final int BIZ_CONFLICT = 40901;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long actorId = HttpSessions.requireUserId(req);
        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());
        Matcher m = PATH.matcher(path);
        if (!m.matches()) {
            throw new ApiException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    BIZ_BAD_REQUEST,
                    "路径须为 /api/leaves/{id}/approve 或 /api/leaves/{id}/reject");
        }
        long leaveId = Long.parseLong(m.group(1));
        String op = m.group(2);

        String remark = null;
        if ("reject".equals(op)) {
            String body = Jsons.readBody(req);
            RejectPayload rp;
            try {
                rp = Jsons.fromJson(body, RejectPayload.class);
            } catch (JsonSyntaxException e) {
                throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "JSON 格式错误");
            }
            if (rp == null || rp.remark == null || rp.remark.isBlank()) {
                throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "驳回需要 remark");
            }
            remark = rp.remark.trim();
            if (remark.length() > 500) {
                throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, BIZ_BAD_REQUEST, "remark 过长");
            }
        }

        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String needPerm = "approve".equals(op) ? "leave:approve" : "leave:reject";
                if (!Permissions.hasPermission(conn, actorId, needPerm)) {
                    int code = "approve".equals(op) ? BIZ_FORBIDDEN_APPROVE : BIZ_FORBIDDEN_REJECT;
                    String msg = "approve".equals(op) ? "无审批通过权限" : "无驳回权限";
                    throw new ApiException(HttpServletResponse.SC_FORBIDDEN, code, msg);
                }

                String status;
                Long assigneeUserId;
                int version;
                long applicantUserId;
                try (PreparedStatement ps =
                                conn.prepareStatement(
                                        "SELECT applicant_user_id, status, current_assignee_user_id, version"
                                                + " FROM leave_requests WHERE id = ? FOR UPDATE")) {
                    ps.setLong(1, leaveId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ApiException(HttpServletResponse.SC_NOT_FOUND, BIZ_NOT_FOUND, "请假单不存在");
                        }
                        applicantUserId = rs.getLong("applicant_user_id");
                        status = rs.getString("status");
                        long asg = rs.getLong("current_assignee_user_id");
                        assigneeUserId = rs.wasNull() ? null : asg;
                        version = rs.getInt("version");
                    }
                }

                if (!"SUBMITTED".equals(status)) {
                    throw new ApiException(
                            HttpServletResponse.SC_CONFLICT,
                            BIZ_CONFLICT,
                            "仅可处理已提交 SUBMITTED 的请假单，当前状态：" + status);
                }
                if (assigneeUserId == null || assigneeUserId != actorId) {
                    throw new ApiException(
                            HttpServletResponse.SC_FORBIDDEN, BIZ_NOT_ASSIGNEE, "当前处理人不是您，不能审批此单");
                }

                String newStatus = "approve".equals(op) ? "APPROVED" : "REJECTED";
                try (PreparedStatement ps =
                                conn.prepareStatement(
                                        "UPDATE leave_requests SET status = ?, current_assignee_user_id = NULL,"
                                                + " version = version + 1 WHERE id = ? AND status = 'SUBMITTED'"
                                                + " AND version = ?")) {
                    ps.setString(1, newStatus);
                    ps.setLong(2, leaveId);
                    ps.setInt(3, version);
                    int n = ps.executeUpdate();
                    if (n != 1) {
                        throw new ApiException(
                                HttpServletResponse.SC_CONFLICT,
                                BIZ_CONFLICT,
                                "请假单状态已变更或已被审批（请勿重复提交）");
                    }
                }

                String action = "approve".equals(op) ? "APPROVE" : "REJECT";
                try (PreparedStatement ps =
                                conn.prepareStatement(
                                        "INSERT INTO leave_actions (leave_request_id, actor_user_id, action,"
                                                + " remark) VALUES (?,?,?,?)")) {
                    ps.setLong(1, leaveId);
                    ps.setLong(2, actorId);
                    ps.setString(3, action);
                    ps.setString(4, "approve".equals(op) ? null : remark);
                    ps.executeUpdate();
                }

                conn.commit();
                LeavesMeRedisCache.invalidateUser(applicantUserId);
                LOG.info(() -> "leave " + op + " id=" + leaveId + " by user=" + actorId);
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("id", leaveId);
                data.put("status", newStatus);
                data.put("version", version + 1);
                Jsons.writeOk(resp, data);
            } catch (ApiException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                LOG.log(Level.WARNING, "leave decision tx", e);
                throw new ApiException(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50004, "审批处理失败：" + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "leave decision", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50004, "数据库错误");
        }
    }

    @SuppressWarnings("unused")
    private static class RejectPayload {
        String remark;
    }
}
