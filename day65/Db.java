import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Db {
    private static final String DEFAULT_PROPS_PATH = "day64/db.properties";

    public static Connection getConnection() throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(DEFAULT_PROPS_PATH)) {
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
            throw new IllegalArgumentException("Missing config: " + key + " (" + DEFAULT_PROPS_PATH + ")");
        }
        return v.trim();
    }
}

