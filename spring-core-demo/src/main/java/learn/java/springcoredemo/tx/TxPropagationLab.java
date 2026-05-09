package learn.java.springcoredemo.tx;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Week22 Day148：{@code Propagation.REQUIRED} 默认「加入当前事务」—— inner 抛错时 outer 中的写操作一并回滚。
 */
@Component
public class TxPropagationLab {

    private final OuterService outerService;
    private final AuditBusinessDemo auditBusinessDemo;
    private final RollbackForLabService rollbackForLabService;
    private final JdbcTemplate jdbc;

    public TxPropagationLab(
            OuterService outerService,
            AuditBusinessDemo auditBusinessDemo,
            RollbackForLabService rollbackForLabService,
            JdbcTemplate jdbc) {
        this.outerService = outerService;
        this.auditBusinessDemo = auditBusinessDemo;
        this.rollbackForLabService = rollbackForLabService;
        this.jdbc = jdbc;
    }

    /**
     * outer、inner 均为 REQUIRED：同一物理事务。inner 抛运行时异常 → 整段回滚，库里不应留下 {@code spring_tx148_*} 行。
     */
    public void requiredJoin() {
        jdbc.update("DELETE FROM student WHERE name LIKE 'spring_tx148_%'");
        String suffix = String.valueOf(System.currentTimeMillis());
        try {
            outerService.outer(suffix);
            throw new AssertionError("expected inner to throw");
        } catch (IllegalStateException e) {
            System.out.println("[TxPropagationLab] caught: " + e.getMessage());
        }
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student WHERE name LIKE 'spring_tx148_%'", Integer.class);
        if (n == null || n != 0) {
            throw new AssertionError("REQUIRED join: expected 0 rows after rollback, got " + n);
        }
        System.out.println("[TxPropagationLab.requiredJoin] OK — COUNT(spring_tx148_*)=" + n + "（outer+inner 均已回滚）");
    }

    /**
     * {@code REQUIRES_NEW}：{@link learn.java.springcoredemo.audit.AuditLogService#append} 单独提交；
     * 外层 {@link AuditBusinessDemo#writeAuditThenFail} 回滚后，{@code student} 中不应有失败行，{@code audit_log} 仍应有记录。
     */
    public void requiresNewAudit() {
        jdbc.update("DELETE FROM student WHERE name LIKE 'spring_d149_fail_%'");
        jdbc.update("DELETE FROM audit_log WHERE correlation_id LIKE 'd149_%'");
        String cid = "d149_" + System.currentTimeMillis();
        try {
            auditBusinessDemo.writeAuditThenFail(cid);
            throw new AssertionError("expected business to throw");
        } catch (IllegalStateException e) {
            System.out.println("[TxPropagationLab] caught: " + e.getMessage());
        }
        Integer studentCnt =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM student WHERE name = ?",
                        Integer.class,
                        "spring_d149_fail_" + cid);
        Integer auditCnt =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM audit_log WHERE correlation_id = ?", Integer.class, cid);
        if (studentCnt == null || studentCnt != 0) {
            throw new AssertionError("REQUIRES_NEW demo: expected 0 student rows, got " + studentCnt);
        }
        if (auditCnt == null || auditCnt != 1) {
            throw new AssertionError(
                    "REQUIRES_NEW demo: expected 1 audit row (survives rollback), got " + auditCnt
                            + " — 是否已在 learn_java 执行 sql/w22_audit_log.sql ?");
        }
        System.out.println(
                "[TxPropagationLab.requiresNewAudit] OK — student 已回滚，audit_log 仍保留 correlation_id="
                        + cid);
    }

    /**
     * 默认仅对 {@link RuntimeException}/{@link Error} 回滚；受检 {@link Exception} 需显式
     * {@code rollbackFor} 才会回滚。
     */
    public void rollbackForDemo() throws Exception {
        jdbc.update("DELETE FROM student WHERE name LIKE 'spring_d150_%'");
        String ts = String.valueOf(System.currentTimeMillis());

        String rt = "spring_d150_rt_" + ts;
        try {
            rollbackForLabService.insertThenThrowRuntime(rt);
            throw new AssertionError("expected runtime");
        } catch (IllegalStateException e) {
            System.out.println("[rollbackForDemo] caught runtime: " + e.getMessage());
        }
        Integer cRt =
                jdbc.queryForObject("SELECT COUNT(*) FROM student WHERE name = ?", Integer.class, rt);
        if (cRt == null || cRt != 0) {
            throw new AssertionError("expected 0 rows after RuntimeException, got " + cRt);
        }
        System.out.println("[rollbackForDemo] RuntimeException → 行已回滚 ✓");

        String chk = "spring_d150_chk_" + ts;
        try {
            rollbackForLabService.insertThenThrowChecked(chk);
            throw new AssertionError("expected checked exception");
        } catch (Exception e) {
            System.out.println("[rollbackForDemo] caught checked: " + e.getMessage());
        }
        Integer cChk =
                jdbc.queryForObject("SELECT COUNT(*) FROM student WHERE name = ?", Integer.class, chk);
        if (cChk == null || cChk != 1) {
            throw new AssertionError(
                    "expected 1 row survives after checked Exception (default commit), got " + cChk);
        }
        System.out.println(
                "[rollbackForDemo] checked Exception 默认不回滚 → 库里仍有 1 行（注意数据一致性风险）✓");

        jdbc.update("DELETE FROM student WHERE name = ?", chk);

        String chkRb = "spring_d150_chk_rb_" + ts;
        try {
            rollbackForLabService.insertThenThrowCheckedWithRollbackFor(chkRb);
            throw new AssertionError("expected checked exception");
        } catch (Exception e) {
            System.out.println("[rollbackForDemo] caught checked (rollbackFor): " + e.getMessage());
        }
        Integer cRb =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM student WHERE name = ?", Integer.class, chkRb);
        if (cRb == null || cRb != 0) {
            throw new AssertionError("expected 0 rows after rollbackFor=Exception, got " + cRb);
        }
        System.out.println("[rollbackForDemo] rollbackFor=Exception.class → 受检异常也回滚 ✓");
        System.out.println("[TxPropagationLab.rollbackForDemo] OK");
    }
}
