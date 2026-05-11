package learn.java.oa.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import learn.java.oa.api.ApiException;
import learn.java.oa.api.BaseJsonServlet;
import learn.java.oa.api.Jsons;
import learn.java.oa.auth.HttpSessions;
import learn.java.oa.db.Db;
import learn.java.oa.service.Permissions;

/**
 * Day135 / Day136：本部门请假列表（数据范围核心：**`dept_id`** 必须等于当前用户所在部门，与课表「经理看本部门」一致）。
 * 默认 **`status=SUBMITTED`**（待审）；可通过 **`?status=`**、**`?leaveType=`** 白名单筛选（见 **`LeaveQueryParams`**）。
 * 须具备 **`leave:pending_dept`**。
 */
public class LeavesPendingServlet extends BaseJsonServlet {

    private static final Logger LOG = Logger.getLogger(LeavesPendingServlet.class.getName());

    private static final int BIZ_FORBIDDEN = 40307;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = HttpSessions.requireUserId(req);
        String st = LeaveQueryParams.parseStatus(req);
        String lt = LeaveQueryParams.parseLeaveType(req);

        StringBuilder sql =
                new StringBuilder(
                        "SELECT lr.id, lr.applicant_user_id, lr.dept_id, lr.leave_type, lr.start_at, lr.end_at,"
                                + " lr.reason, lr.status, lr.current_assignee_user_id, lr.version, lr.created_at,"
                                + " lr.updated_at FROM leave_requests lr WHERE lr.dept_id = (SELECT e.dept_id FROM"
                                + " employees e WHERE e.user_id = ? LIMIT 1)");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (st != null) {
            sql.append(" AND lr.status = ?");
            params.add(st);
        } else {
            sql.append(" AND lr.status = 'SUBMITTED'");
        }
        if (lt != null) {
            sql.append(" AND lr.leave_type = ?");
            params.add(lt);
        }
        sql.append(" ORDER BY lr.created_at DESC");

        try (Connection conn = Db.getConnection()) {
            if (!Permissions.hasPermission(conn, userId, "leave:pending_dept")) {
                throw new ApiException(HttpServletResponse.SC_FORBIDDEN, BIZ_FORBIDDEN, "无查看本部门待审权限");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String, Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        rows.add(LeaveJsons.leaveRequestRow(rs));
                    }
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("items", rows);
                    data.put("total", rows.size());
                    Jsons.writeOk(resp, data);
                }
            }
        } catch (ApiException e) {
            throw e;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "leaves/pending", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50005, "查询待审列表失败");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "leaves/pending", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50005, "查询待审列表失败");
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object o = Objects.requireNonNull(params.get(i));
            if (o instanceof Long l) {
                ps.setLong(i + 1, l);
            } else {
                ps.setString(i + 1, (String) o);
            }
        }
    }
}
