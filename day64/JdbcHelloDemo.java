import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JdbcHelloDemo {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("day64/db.properties")) {
            props.load(in);
        }

        String url = require(props, "db.url");
        String user = require(props, "db.user");
        String password = require(props, "db.password");

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery("SELECT VERSION() AS ver, NOW() AS now_time")) {
                if (rs.next()) {
                    System.out.println("MySQL VERSION: " + rs.getString("ver"));
                    System.out.println("NOW(): " + rs.getString("now_time"));
                }
            }
        }
    }

    private static String require(Properties props, String key) {
        String v = props.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing config: " + key + " (day64/db.properties)");
        }
        return v.trim();
    }
}

