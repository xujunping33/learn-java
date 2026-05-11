package learn.java.springcoredemo.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import learn.java.springcoredemo.repository.StudentRepository;

@Service
public class OuterService {

    private final InnerService innerService;
    private final StudentRepository students;

    public OuterService(InnerService innerService, StudentRepository students) {
        this.innerService = innerService;
        this.students = students;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void outer(String suffix) {
        System.out.println("[OuterService.outer] transaction active=" + TransactionSynchronizationManager.isActualTransactionActive());
        students.insert("spring_tx148_outer_" + suffix, 10);
        innerService.inner(suffix);
    }
}
