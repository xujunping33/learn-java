import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class PreparedStatementDemo {
    public static void main(String[] args) throws Exception {
        // 演示输入：试图把数据当 SQL 语法执行（不会删库，只做“绕过条件”演示）
        String normalInput = "not_exist_name";
        String injectedInput = "not_exist_name' OR '1'='1";

        System.out.println("=== Statement (unsafe) ===");
        System.out.println("normalInput count = " + countByNameUnsafe(normalInput));
        System.out.println("injectedInput count = " + countByNameUnsafe(injectedInput));

        System.out.println();
        System.out.println("=== PreparedStatement (safe) ===");
        System.out.println("normalInput count = " + countByNameSafe(normalInput));
        System.out.println("injectedInput count = " + countByNameSafe(injectedInput));
    }

    // 危险写法：直接拼接字符串（演示用）
    private static long countByNameUnsafe(String name) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM student WHERE name = '" + name + "'";
        try (Connection conn = Db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getLong("cnt");
        }
    }

    // 安全写法：参数绑定，不会把参数当 SQL 语法解析
    private static long countByNameSafe(String name) throws Exception {
        String sql = "SELECT COUNT(*) AS cnt FROM student WHERE name = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("cnt");
            }
        }
    }
}

