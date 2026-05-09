import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service：承载业务规则（这里做最小版校验），并调用 DAO 完成持久化。
 *
 * 规则示例：
 * - score 取值范围 0..100
 * - name 不能为空
 */
public class StudentService {
    private final StudentDao dao;

    public StudentService(StudentDao dao) {
        this.dao = dao;
    }

    public long addStudent(String name, int score, int age) throws Exception {
        requireNonBlank(name, "name");
        requireRange(score, 0, 100, "score");
        requireRange(age, 0, 120, "age");
        String trimmed = name.trim();
        try {
            return dao.add(new Student(0, trimmed, score, age));
        } catch (SQLException e) {
            // SQLState 23000: integrity constraint violation（例如 UNIQUE 冲突）
            if ("23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("name 已存在（唯一约束），请换一个: " + trimmed, e);
            }
            throw e;
        }
    }

    public boolean updateScore(long id, int score) throws Exception {
        requireRange(score, 0, 100, "score");
        return dao.updateScore(id, score) == 1;
    }

    public boolean deleteById(long id) throws Exception {
        return dao.deleteById(id) == 1;
    }

    public Optional<Student> findById(long id) throws Exception {
        return dao.findById(id);
    }

    public List<Student> listAll() throws Exception {
        return dao.listAll();
    }

    private static void requireNonBlank(String s, String field) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireRange(int v, int min, int max, String field) {
        if (v < min || v > max) {
            throw new IllegalArgumentException(field + " out of range: " + v + " (expected " + min + ".." + max + ")");
        }
    }
}

