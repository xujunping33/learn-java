import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class JdbcTxDemo {
    public static void main(String[] args) throws Exception {
        ensureAccountTable();
        resetAccounts();

        System.out.println("== Initial balances ==");
        printBalances();

        System.out.println();
        System.out.println("== Transfer with failure (should rollback) ==");
        try {
            transferWithFailure("alice", "bob", 100.00);
        } catch (Exception e) {
            System.out.println("Caught expected error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        printBalances();

        System.out.println();
        System.out.println("== Transfer success (should commit) ==");
        transfer("alice", "bob", 100.00);
        printBalances();
    }

    private static void ensureAccountTable() throws Exception {
        String ddl = """
                CREATE TABLE IF NOT EXISTS account (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  name VARCHAR(50) NOT NULL UNIQUE,
                  balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB
                  DEFAULT CHARSET=utf8mb4
                  COLLATE=utf8mb4_0900_ai_ci;
                """;
        try (Connection conn = Db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
        }
    }

    private static void resetAccounts() throws Exception {
        String upsert = """
                INSERT INTO account (name, balance) VALUES ('alice', 1000.00), ('bob', 500.00)
                ON DUPLICATE KEY UPDATE balance = VALUES(balance)
                """;
        try (Connection conn = Db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(upsert);
        }
    }

    private static void transfer(String from, String to, double amount) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                decrease(conn, from, amount);
                increase(conn, to, amount);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void transferWithFailure(String from, String to, double amount) throws Exception {
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                decrease(conn, from, amount);

                // 人为制造错误：插入重复 name，触发 UNIQUE 约束失败
                String badSql = "INSERT INTO account (name, balance) VALUES ('alice', 1.00)";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(badSql);
                }

                increase(conn, to, amount);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static void decrease(Connection conn, String name, double amount) throws Exception {
        String sql = "UPDATE account SET balance = balance - ? WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, name);
            int rows = ps.executeUpdate();
            if (rows != 1) throw new IllegalStateException("decrease affected rows=" + rows);
        }
    }

    private static void increase(Connection conn, String name, double amount) throws Exception {
        String sql = "UPDATE account SET balance = balance + ? WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, name);
            int rows = ps.executeUpdate();
            if (rows != 1) throw new IllegalStateException("increase affected rows=" + rows);
        }
    }

    private static void printBalances() throws Exception {
        String sql = "SELECT name, balance FROM account WHERE name IN ('alice','bob') ORDER BY name";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString("name") + ": " + rs.getBigDecimal("balance"));
            }
        }
    }
}

