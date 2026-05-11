package learn.java.springcoredemo.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import learn.java.springcoredemo.repository.StudentRepository;

@Service
public class InnerService {

    private final StudentRepository students;

    public InnerService(StudentRepository students) {
        this.students = students;
    }

    /** 默认 REQUIRED：加入外层已有事务，不新开物理事务。 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void inner(String suffix) {
        System.out.println("[InnerService.inner] transaction active=" + TransactionSynchronizationManager.isActualTransactionActive());
        students.insert("spring_tx148_inner_" + suffix, 20);
        throw new IllegalStateException("inner: intentional failure (REQUIRED join demo)");
    }
}
