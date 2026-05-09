import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDaoJdbc {

    public long add(Student s) throws Exception {
        String sql = "INSERT INTO student (name, score, age) VALUES (?, ?, ?)";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setInt(2, s.getScore());
            ps.setInt(3, s.getAge());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new IllegalStateException("Insert succeeded but no generated key returned.");
    }

    public int deleteById(long id) throws Exception {
        String sql = "DELETE FROM student WHERE id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    public int updateScore(long id, int score) throws Exception {
        String sql = "UPDATE student SET score = ? WHERE id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, score);
            ps.setLong(2, id);
            return ps.executeUpdate();
        }
    }

    public Optional<Student> findById(long id) throws Exception {
        String sql = "SELECT id, name, score, age, created_at, updated_at FROM student WHERE id = ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        }
    }

    public List<Student> listAll() throws Exception {
        String sql = "SELECT id, name, score, age, created_at, updated_at FROM student ORDER BY id";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Student> out = new ArrayList<>();
            while (rs.next()) {
                out.add(mapRow(rs));
            }
            return out;
        }
    }

    public List<Student> listPage(int page, int pageSize) throws Exception {
        if (page < 1) throw new IllegalArgumentException("page must be >= 1");
        if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");

        int offset = (page - 1) * pageSize;
        String sql = "SELECT id, name, score, age, created_at, updated_at FROM student ORDER BY id LIMIT ?, ?";
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offset);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
                return out;
            }
        }
    }

    private Student mapRow(ResultSet rs) throws Exception {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setScore(rs.getInt("score"));
        s.setAge(rs.getInt("age"));
        s.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        s.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return s;
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}

