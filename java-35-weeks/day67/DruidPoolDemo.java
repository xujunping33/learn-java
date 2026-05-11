import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Day67：连接池 Demo
 */
public class DruidPoolDemo {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("day67/druid.properties")) {
            props.load(in);
        }
        DataSource ds = DruidDataSourceFactory.createDataSource(props);

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT VERSION() AS ver, NOW() AS now_time")) {
            if (rs.next()) {
                System.out.println("MySQL VERSION: " + rs.getString("ver"));
                System.out.println("NOW(): " + rs.getString("now_time"));
            }
        }

        // 再取一次连接，证明 close() 后连接可以复用（close=归还连接到池）
        try (Connection conn2 = ds.getConnection();
             Statement stmt2 = conn2.createStatement();
             ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) AS cnt FROM student")) {
            rs2.next();
            System.out.println("student count: " + rs2.getLong("cnt"));
        }
    }
}

