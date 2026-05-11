package learn.java.oa.servlet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

/** 请假单列表行 → JSON 友好 Map（供 {@link LeavesMeServlet} / {@link LeavesPendingServlet} 复用）。 */
public final class LeaveJsons {

    private LeaveJsons() {}

    public static Map<String, Object> leaveRequestRow(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getLong("id"));
        m.put("applicantUserId", rs.getLong("applicant_user_id"));
        m.put("deptId", rs.getLong("dept_id"));
        m.put("leaveType", rs.getString("leave_type"));
        m.put("startAt", ts(rs.getTimestamp("start_at")));
        m.put("endAt", ts(rs.getTimestamp("end_at")));
        m.put("reason", rs.getString("reason"));
        m.put("status", rs.getString("status"));
        long assignee = rs.getLong("current_assignee_user_id");
        m.put("currentAssigneeUserId", rs.wasNull() ? null : assignee);
        m.put("version", rs.getInt("version"));
        m.put("createdAt", ts(rs.getTimestamp("created_at")));
        m.put("updatedAt", ts(rs.getTimestamp("updated_at")));
        return m;
    }

    private static String ts(Timestamp t) {
        return t == null ? null : t.toInstant().toString();
    }
}
