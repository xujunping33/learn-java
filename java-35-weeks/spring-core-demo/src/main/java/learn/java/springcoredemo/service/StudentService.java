package learn.java.springcoredemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import learn.java.springcoredemo.model.Student;
import learn.java.springcoredemo.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository students;

    public StudentService(StudentRepository students) {
        this.students = students;
    }

    public List<Student> listStudents() {
        return students.findAll();
    }

    public Optional<Student> getStudent(long id) {
        return students.findById(id);
    }

    public long addStudent(String name, int score) {
        return students.insert(name, score);
    }

    public int setScore(long id, int score) {
        return students.updateScore(id, score);
    }

    /**
     * 第一步更新分数后抛出运行时异常，整段事务应回滚，数据库不应留下「改了一半」的状态。
     */
    @Transactional
    public void twoUpdatesFailAfterFirst(long id1, int newScore1) {
        students.updateScore(id1, newScore1);
        throw new IllegalStateException("intentional rollback after first update (tx demo)");
    }

    /** 两步更新均成功，一次提交。 */
    @Transactional
    public void twoUpdatesCommit(long id1, int newScore1, long id2, int newScore2) {
        students.updateScore(id1, newScore1);
        students.updateScore(id2, newScore2);
    }
}
