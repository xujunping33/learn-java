package learn.java.springcoredemo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import learn.java.springcoredemo.model.Student;

/**
 * Day 142 内存实现（留作对照）；当前应用默认使用 {@link StudentRepositoryJdbc}（Day 144）。
 */
public class InMemoryStudentRepository implements StudentRepository {

    private final Map<Long, Student> rows = new ConcurrentHashMap<>();

    public InMemoryStudentRepository() {
        rows.put(1L, new Student(1L, "Alice", 90));
        rows.put(2L, new Student(2L, "Bob", 78));
    }

    @Override
    public List<Student> findAll() {
        return rows.values().stream().sorted((a, b) -> Long.compare(a.id(), b.id())).collect(Collectors.toList());
    }

    @Override
    public Optional<Student> findById(long id) {
        return Optional.ofNullable(rows.get(id));
    }

    @Override
    public long insert(String name, int score) {
        long id = rows.keySet().stream().mapToLong(Long::longValue).max().orElse(0) + 1;
        rows.put(id, new Student(id, name, score));
        return id;
    }

    @Override
    public int updateScore(long id, int score) {
        return findById(id)
                .map(s -> {
                    rows.put(id, new Student(id, s.name(), score));
                    return 1;
                })
                .orElse(0);
    }

    @Override
    public void saveAll(List<Student> students) {
        for (Student s : students) {
            insert(s.name(), s.score());
        }
    }
}
