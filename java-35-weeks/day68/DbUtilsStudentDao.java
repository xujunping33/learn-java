import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DbUtilsStudentDao {
    private final QueryRunner runner;

    public DbUtilsStudentDao(DataSource dataSource) {
        this.runner = new QueryRunner(dataSource);
    }

    public long add(Student s) throws Exception {
        String sql = "INSERT INTO student (name, score, age) VALUES (?, ?, ?)";
        // DBUtils 提供 insert：可以直接拿到 generated key（比 JDBC 少写很多模板代码）
        Long id = runner.insert(sql, new ScalarHandler<>(), s.getName(), s.getScore(), s.getAge());
        if (id == null) throw new IllegalStateException("Insert succeeded but no generated key returned.");
        return id;
    }

    public int updateScore(long id, int score) throws Exception {
        String sql = "UPDATE student SET score = ? WHERE id = ?";
        return runner.update(sql, score, id);
    }

    public int deleteById(long id) throws Exception {
        String sql = "DELETE FROM student WHERE id = ?";
        return runner.update(sql, id);
    }

    public Optional<Student> findById(long id) throws Exception {
        String sql = "SELECT id, name, score, age, created_at, updated_at FROM student WHERE id = ?";
        Student one = runner.query(sql, studentOneHandler(), id);
        return Optional.ofNullable(one);
    }

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
            while (rs.next()) {
                out.add(mapRowUnchecked(rs));
            }
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

