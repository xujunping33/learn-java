package learn.java.oa.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Set;
import learn.java.oa.api.ApiException;

/** Day136：列表查询可选筛选（白名单，防拼接注入）。非法非空参数抛 **400**。 */
public final class LeaveQueryParams {

    private static final Set<String> STATUSES =
            Set.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "CANCELLED");
    private static final Set<String> LEAVE_TYPES = Set.of("ANNUAL", "SICK", "OTHER");

    private LeaveQueryParams() {}

    /** 可为 null（未传或空串）；非空且不在白名单则抛错。 */
    public static String parseStatus(HttpServletRequest req) {
        return parseEnumParam(req.getParameter("status"), STATUSES, "status");
    }

    public static String parseLeaveType(HttpServletRequest req) {
        return parseEnumParam(req.getParameter("leaveType"), LEAVE_TYPES, "leaveType");
    }

    private static String parseEnumParam(String raw, Set<String> allowed, String name) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim().toUpperCase(Locale.ROOT);
        if (!allowed.contains(v)) {
            throw new ApiException(
                    HttpServletResponse.SC_BAD_REQUEST,
                    40004,
                    "非法 " + name + "：" + raw + "（仅允许 " + String.join("/", allowed) + "）");
        }
        return v;
    }
}
