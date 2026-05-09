package learn.java.springcoredemo.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import learn.java.springcoredemo.audit.AuditLogService;
import learn.java.springcoredemo.repository.StudentRepository;

/**
 * 业务事务（REQUIRED）内先写审计（REQUIRES_NEW 已单独提交），再写业务表并失败 → 业务回滚、审计不落回滚。
 */
@Service
public class AuditBusinessDemo {

    private final AuditLogService auditLogService;
    private final StudentRepository students;

    public AuditBusinessDemo(AuditLogService auditLogService, StudentRepository students) {
        this.auditLogService = auditLogService;
        this.students = students;
    }

    @Transactional
    public void writeAuditThenFail(String correlationId) {
        auditLogService.append("biz_attempt", correlationId);
        students.insert("spring_d149_fail_" + correlationId, 1);
        throw new IllegalStateException("d149 business rollback after audit");
    }
}
