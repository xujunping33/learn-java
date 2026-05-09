package learn.java.springcoredemo.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import learn.java.springcoredemo.repository.StudentRepository;

/**
 * Week22 Day150：对比默认回滚规则与 {@code rollbackFor}。受检异常默认<strong>不</strong>触发回滚。
 */
@Service
public class RollbackForLabService {

    private final StudentRepository students;

    public RollbackForLabService(StudentRepository students) {
        this.students = students;
    }

    @Transactional
    public void insertThenThrowRuntime(String rowName) {
        students.insert(rowName, 1);
        throw new IllegalStateException("d150 runtime → rollback by default");
    }

    /** 受检异常：默认<strong>不会</strong>令事务回滚（插入可能已提交）。 */
    @Transactional
    public void insertThenThrowChecked(String rowName) throws Exception {
        students.insert(rowName, 1);
        throw new Exception("d150 checked → commit by default (!)");
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertThenThrowCheckedWithRollbackFor(String rowName) throws Exception {
        students.insert(rowName, 1);
        throw new Exception("d150 checked but rollbackFor=Exception → rollback");
    }
}
