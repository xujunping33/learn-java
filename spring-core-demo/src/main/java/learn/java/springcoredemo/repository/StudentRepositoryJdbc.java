package learn.java.springcoredemo.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import learn.java.springcoredemo.model.Student;

@Repository
@Primary
public class StudentRepositoryJdbc implements StudentRepository {

    private static final RowMapper<Student> ROW =
            (rs, rowNum) -> new Student(rs.getLong("id"), rs.getString("name"), rs.getInt("score"));

    private final JdbcTemplate jdbc;

    public StudentRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Student> findAll() {
        return jdbc.query("SELECT id, name, score FROM student ORDER BY id", ROW);
    }

    @Override
    public Optional<Student> findById(long id) {
        List<Student> list =
                jdbc.query("SELECT id, name, score FROM student WHERE id = ?", ROW, id);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        if (list.size() > 1) {
            throw new IllegalStateException("expected at most one row for id=" + id);
        }
        return Optional.of(list.get(0));
    }

    @Override
    public long insert(String name, int score) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                connection -> {
                    var ps = connection.prepareStatement(
                            "INSERT INTO student (name, score, age) VALUES (?, ?, 0)",
                            Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, name);
                    ps.setInt(2, score);
                    return ps;
                },
                keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("no generated key after insert");
        }
        return key.longValue();
    }

    @Override
    public int updateScore(long id, int score) {
        return jdbc.update("UPDATE student SET score = ? WHERE id = ?", score, id);
    }

    /**
     * {@link JdbcTemplate#batchUpdate} + {@link BatchPreparedStatementSetter}；每批最多 500 条减轻单次 bind 压力。
     */
    @Override
    public void saveAll(List<Student> students) {
        if (students.isEmpty()) {
            return;
        }
        final int batchSize = 500;
        for (int from = 0; from < students.size(); from += batchSize) {
            int to = Math.min(from + batchSize, students.size());
            List<Student> chunk = students.subList(from, to);
            jdbc.batchUpdate(
                    "INSERT INTO student (name, score, age) VALUES (?, ?, 0)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Student s = chunk.get(i);
                            ps.setString(1, s.name());
                            ps.setInt(2, s.score());
                        }

                        @Override
                        public int getBatchSize() {
                            return chunk.size();
                        }
                    });
        }
    }
}
