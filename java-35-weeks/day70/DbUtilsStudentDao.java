import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Day70：DBUtils 版 DAO 实现。
 *
 * 为什么这样写：
 * - 仍然是 JDBC，但 DBUtils 帮你省掉大量模板代码（创建/关闭 statement、遍历 ResultSet 等）
 * - DataSource 用 Druid，保证连接复用；DBUtils 用 QueryRunner 执行 SQL
 *
 * 配置来源：
 * - 直接复用 day67/druid.properties（保持全项目一份连接池配置）
 */
public class DbUtilsStudentDao implements StudentDao {
    private static final String DRUID_PROPS_PATH = "day67/druid.properties";

    private final QueryRunner runner;

    public DbUtilsStudentDao() throws Exception {
        this(loadDataSource());
    }

    public DbUtilsStudentDao(DataSource dataSource) {
        this.runner = new QueryRunner(dataSource);
    }

    private static DataSource loadDataSource() throws Exception {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(DRUID_PROPS_PATH)) {
            props.load(in);
        }
        return DruidDataSourceFactory.createDataSource(props);
    }

    @Override
    public long add(Student s) throws Exception {
        String sql = "INSERT INTO student (name, score, age) VALUES (?, ?, ?)";
        // MySQL 驱动返回的 generated key 类型可能是 Long / BigInteger 等，这里统一按 Number 处理。
        Number id = runner.insert(sql, new ScalarHandler<>(), s.getName(), s.getScore(), s.getAge());
        if (id == null) throw new IllegalStateException("Insert succeeded but no generated key returned.");
        return id.longValue();
    }

    @Override
    public int updateScore(long id, int score) throws Exception {
        String sql = "UPDATE student SET score = ? WHERE id = ?";
        return runner.update(sql, score, id);
    }

    @Override
    public int deleteById(long id) throws Exception {
        String sql = "DELETE FROM student WHERE id = ?";
        return runner.update(sql, id);
    }

    @Override
    public Optional<Student> findById(long id) throws Exception {
        String sql = "SELECT id, name, score, age, created_at, updated_at FROM student WHERE id = ?";
        Student one = runner.query(sql, studentOneHandler(), id);
        return Optional.ofNullable(one);
    }

    @Override
    public List<Student> listAll() throws Exception {
        String sql = "SELECT id, name, score, age, created_at, updated_at FROM student ORDER BY id";
        return runner.query(sql, studentListHandler());
    }

    private ResultSetHandler<Student> studentOneHandler() {
        return rs -> rs.next() ? mapRowUnchecked(rs) : null;
    }

    private ResultSetHandler<List<Student>> studentListHandler() {
        return rs -> {
            List<Student> out = new ArrayList<>();
            while (rs.next()) out.add(mapRowUnchecked(rs));
            return out;
        };
    }

    private Student mapRowUnchecked(ResultSet rs) {
        try {
            Student s = new Student();
            s.setId(rs.getLong("id"));
            s.setName(rs.getString("name"));
            s.setScore(rs.getInt("score"));
            s.setAge(rs.getInt("age"));
            s.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
            s.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
            return s;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map Student row", e);
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}

