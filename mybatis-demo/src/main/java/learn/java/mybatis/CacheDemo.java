package learn.java.mybatis;

import learn.java.mybatis.mapper.StudentMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

/**
 * Day84：一级缓存（SqlSession 级）与日志观察。
 * <p>
 * 同一 SqlSession 内相同查询第二次命中一级缓存，通常不再打 JDBC SQL；
 * 换 SqlSession 后一级缓存失效，会再次访问数据库。
 * </p>
 * 运行（与 App 不同入口，见 {@code pom.xml} 中 exec 的 {@code cache-demo} execution）：
 * <pre>
 *   mvn compile exec:java@cache-demo
 *   mvn exec:java@cache-demo -Dexec.args="22"
 * </pre>
 * 配合 {@code logback.xml} 中 {@code learn.java.mybatis.mapper} 的 DEBUG，看 {@code ==> Preparing} 出现次数：
 * 同一会话第二次 {@code selectById} 不应再打印 Preparing。
 */
public class CacheDemo {
    public static void main(String[] args) throws Exception {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
            long id = args != null && args.length > 0 ? Long.parseLong(args[0]) : 21L;

            System.out.println("=== (1) 同一 SqlSession：selectById 两次 — 第二次应命中一级缓存（SQL 日志通常只一组）===");
            try (SqlSession session = factory.openSession()) {
                StudentMapper mapper = session.getMapper(StudentMapper.class);
                System.out.println("first  => " + mapper.selectById(id));
                System.out.println("second => " + mapper.selectById(id));
            }

            System.out.println("=== (2) 新 SqlSession：再查同一 id — 一级缓存不跨会话，应再次访问库 ===");
            try (SqlSession session2 = factory.openSession()) {
                StudentMapper mapper2 = session2.getMapper(StudentMapper.class);
                System.out.println("third  => " + mapper2.selectById(id));
            }
        } finally {
            try {
                Class<?> t = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
                t.getMethod("checkedShutdown").invoke(null);
            } catch (Throwable ignored) {
            }
        }
    }
}
