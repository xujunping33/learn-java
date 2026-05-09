package learn.java.springcoredemo.repository;

import java.util.List;
import java.util.Optional;

import learn.java.springcoredemo.model.Student;

public interface StudentRepository {

    List<Student> findAll();

    Optional<Student> findById(long id);

    /** Insert row; returns generated primary key. */
    long insert(String name, int score);

    /** @return rows updated (expect 0 or 1) */
    int updateScore(long id, int score);

    /** 批量插入；{@link Student#id()} 忽略，由库生成主键。 */
    void saveAll(List<Student> students);
}
