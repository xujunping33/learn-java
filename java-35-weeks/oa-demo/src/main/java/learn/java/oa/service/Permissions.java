package learn.java.oa.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** 基于 RBAC 的权限判断（最小实现：查 role_permissions）。 */
public final class Permissions {

    private Permissions() {}

    public static boolean hasPermission(Connection conn, long userId, String permissionCode)
            throws SQLException {
        String sql =
                "SELECT 1 FROM user_roles ur"
                        + " JOIN role_permissions rp ON rp.role_id = ur.role_id"
                        + " JOIN permissions p ON p.id = rp.permission_id"
                        + " WHERE ur.user_id = ? AND p.code = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, permissionCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
