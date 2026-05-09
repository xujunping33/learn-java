package learn.java.oa.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** 从 classpath 读取 {@code db.properties}（与 {@code db.properties.example} 对照）。 */
public final class Db {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Db() {}

    public static Connection getConnection() throws SQLException {
        try {
            Properties props = new Properties();
            try (InputStream in = Db.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IllegalStateException("Classpath 缺少 db.properties，请复制 db.properties.example");
                }
                props.load(in);
            }
            String url = require(props, "db.url");
            String user = require(props, "db.user");
            String password = props.getProperty("db.password", "");
            return DriverManager.getConnection(url, user, password);
        } catch (IOException e) {
            throw new SQLException("读取 db.properties 失败", e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    private static String require(Properties props, String key) {
        String v = props.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("db.properties 缺少: " + key);
        }
        return v.trim();
    }
}
