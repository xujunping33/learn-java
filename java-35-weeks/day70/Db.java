import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * 统一管理数据库连接获取（Day70 最小版）。
 *
 * 约定：复用 day64/db.properties，方便你只改一处配置。
 */
public class Db {
    private static final String PROPS_PATH = "day64/db.properties";

    public static Connection getConnection() throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(PROPS_PATH)) {
            props.load(in);
        }
        String url = require(props, "db.url");
        String user = require(props, "db.user");
        String password = require(props, "db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private static String require(Properties props, String key) {
        String v = props.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing config: " + key + " (" + PROPS_PATH + ")");
        }
        return v.trim();
    }
}

