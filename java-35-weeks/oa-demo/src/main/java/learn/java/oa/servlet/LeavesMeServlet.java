package learn.java.oa.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import learn.java.oa.cache.LeavesMeRedisCache;
import learn.java.oa.db.Db;

/** Day131：当前登录用户作为申请人的请假列表（数据范围最小版：仅本人）。 */
public class LeavesMeServlet extends BaseJsonServlet {

    private static final Logger LOG = Logger.getLogger(LeavesMeServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long userId = HttpSessions.requireUserId(req);
        String st = LeaveQueryParams.parseStatus(req);
        String lt = LeaveQueryParams.parseLeaveType(req);

        boolean cacheOn = LeavesMeRedisCache.isActive();
        String cacheKey = cacheOn ? LeavesMeRedisCache.cacheKey(userId, st, lt) : null;
        if (cacheOn) {
            String cached = LeavesMeRedisCache.getJsonOrNull(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                LOG.info(() -> "leaves/me cache HIT key=" + cacheKey);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
                resp.getWriter().write(cached);
                return;
            }
            LOG.info(() -> "leaves/me cache MISS key=" + cacheKey + " -> DB");
        }

        StringBuilder sql =
                new StringBuilder(
                        "SELECT id, applicant_user_id, dept_id, leave_type, start_at, end_at, reason, status,"
                                + " current_assignee_user_id, version, created_at, updated_at"
                                + " FROM leave_requests WHERE applicant_user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        if (st != null) {
            sql.append(" AND status = ?");
            params.add(st);
        }
        if (lt != null) {
            sql.append(" AND leave_type = ?");
            params.add(lt);
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(LeaveJsons.leaveRequestRow(rs));
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("items", rows);
                data.put("total", rows.size());
                if (cacheOn) {
                    LeavesMeRedisCache.setJson(cacheKey, Jsons.toJsonOk(data));
                }
                Jsons.writeOk(resp, data);
            }
        } catch (ApiException e) {
            throw e;
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "leaves/me", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50002, "查询请假列表失败");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "leaves/me", e);
            throw new ApiException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 50002, "查询请假列表失败");
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
