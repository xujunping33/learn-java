package learn.java.springcoredemo.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import learn.java.springcoredemo.model.Student;
import learn.java.springcoredemo.repository.StudentRepository;

/**
 * Week22 Day151：对比逐条 {@link StudentRepository#insert} 与 {@link StudentRepository#saveAll}（底层 {@code batchUpdate}）。
 */
@Component
public class StudentBatchImporter {

    private static final int IMPORT_SIZE = 1000;

    private final StudentRepository repository;
    private final JdbcTemplate jdbc;

    public StudentBatchImporter(StudentRepository repository, JdbcTemplate jdbc) {
        this.repository = repository;
        this.jdbc = jdbc;
    }

    /** 生成 {@value IMPORT_SIZE} 条 {@code w22_<时间戳>_序号}，计时逐条插入 vs 批量插入后删除。 */
    public void importThousandAndCompare() {
        String ts = String.valueOf(System.currentTimeMillis());
        String likePrefix = "w22_" + ts + "_";
        jdbc.update("DELETE FROM student WHERE name LIKE ?", likePrefix + "%");

        List<Student> rows = new ArrayList<>(IMPORT_SIZE);
        for (int i = 0; i < IMPORT_SIZE; i++) {
            rows.add(new Student(0L, likePrefix + i, i % 100));
        }

        long t0 = System.nanoTime();
        for (Student s : rows) {
            repository.insert(s.name(), s.score());
        }
        long sequentialMs = (System.nanoTime() - t0) / 1_000_000L;

        jdbc.update("DELETE FROM student WHERE name LIKE ?", likePrefix + "%");

        t0 = System.nanoTime();
        repository.saveAll(rows);
        long batchMs = (System.nanoTime() - t0) / 1_000_000L;

        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM student WHERE name LIKE ?", Integer.class, likePrefix + "%");
        if (count == null || count != IMPORT_SIZE) {
            throw new AssertionError("batch path: expected " + IMPORT_SIZE + " rows, got " + count);
        }
        jdbc.update("DELETE FROM student WHERE name LIKE ?", likePrefix + "%");

        System.out.println("[StudentBatchImporter] " + IMPORT_SIZE + " 逐条 insert（默认多次提交）: " + sequentialMs + " ms");
        System.out.println("[StudentBatchImporter] " + IMPORT_SIZE + " saveAll(batchUpdate，每批≤500): " + batchMs + " ms");
    }
}
