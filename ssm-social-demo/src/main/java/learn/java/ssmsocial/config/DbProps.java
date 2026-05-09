package learn.java.ssmsocial.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** 从 classpath 读取 {@code db.properties}（与 {@code db.properties.example} 对照）。 */
public final class DbProps {

    private DbProps() {}

    public static Properties load() {
        try {
            Properties props = new Properties();
            try (InputStream in = DbProps.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IllegalStateException("Classpath 缺少 db.properties，请复制 db.properties.example");
                }
                props.load(in);
            }
            require(props, "db.url");
            require(props, "db.user");
            props.putIfAbsent("db.password", "");
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("读取 db.properties 失败", e);
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

