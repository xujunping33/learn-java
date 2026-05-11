package learn.java.springcoredemo.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审计写入使用 {@link Propagation#REQUIRES_NEW}：即使外层业务事务回滚，已提交的审计行仍保留。
 */
@Service
public class AuditLogService {

    private final JdbcTemplate jdbc;

    public AuditLogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void append(String event, String correlationId) {
        jdbc.update(
                "INSERT INTO audit_log (event, correlation_id) VALUES (?, ?)",
                event,
                correlationId);
    }
}
