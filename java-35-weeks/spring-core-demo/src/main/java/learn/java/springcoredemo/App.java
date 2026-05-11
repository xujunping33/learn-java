package learn.java.springcoredemo;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import learn.java.springcoredemo.model.Student;
import learn.java.springcoredemo.aop.MetricsAspect;
import learn.java.springcoredemo.batch.StudentBatchImporter;
import learn.java.springcoredemo.service.StudentService;
import learn.java.springcoredemo.tx.RollbackForLabService;
import learn.java.springcoredemo.tx.TxPropagationLab;

/**
 * Week21 Day147 + Week22 Day148~152：容器、JdbcTemplate（含 batch）、事务传播、rollbackFor、多切面 AOP。
 */
public final class App {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(AppConfig.class)) {

            System.out.println("========== Week21 / Day147 整合演示 ==========");
            System.out.println("容器: AnnotationConfigApplicationContext(AppConfig)");
            System.out.println("装配: @Configuration + @Import(DataSourceConfig) + @ComponentScan(service,repository,aop)");
            System.out.println("开关: @EnableTransactionManagement + @EnableAspectJAutoProxy");
            System.out.println("DI 示例: UuidGenerator → " + ctx.getBean(UuidGenerator.class).next());

            DataSource dataSource = ctx.getBean(DataSource.class);
            if (dataSource instanceof DriverManagerDataSource dmds) {
                System.out.println("DataSource URL: " + dmds.getUrl());
            }
            try (Connection c = dataSource.getConnection()) {
                System.out.println("JDBC: OK → " + c.getMetaData().getURL());
            } catch (Exception e) {
                System.out.println("JDBC 不可用，后续 DB 用例会失败: " + e.getMessage());
                return;
            }

            StudentService svc = ctx.getBean(StudentService.class);

            System.out.println();
            System.out.println("--- [1] 查询学生（JdbcTemplate / Repository）---");
            List<Student> all = svc.listStudents();
            System.out.println("listStudents: 共 " + all.size() + " 条；前 5 条: " + all.stream().limit(5).toList());
            if (!all.isEmpty()) {
                long sid = all.get(0).id();
                System.out.println("getStudent(" + sid + "): " + svc.getStudent(sid).orElse(null));
            }

            System.out.println();
            System.out.println("--- [2] 更新分数（成功）---");
            String tag = "spring_d147_" + System.currentTimeMillis();
            long rowId = svc.addStudent(tag, 80);
            System.out.println("insert: " + svc.getStudent(rowId).orElse(null));
            svc.setScore(rowId, 91);
            System.out.println("setScore 80→91: " + svc.getStudent(rowId).orElse(null));

            System.out.println();
            System.out.println("--- [3] 声明式事务：两步更新 + 中途异常 → 回滚（无半更新）---");
            String t = String.valueOf(System.currentTimeMillis());
            long idA = svc.addStudent("spring_d147_tx_a_" + t, 40);
            long idB = svc.addStudent("spring_d147_tx_b_" + t, 40);
            System.out.println("准备两行 idA=" + idA + " idB=" + idB + " score=40");
            try {
                svc.twoUpdatesFailAfterFirst(idA, 999);
            } catch (IllegalStateException e) {
                System.out.println("捕获预期异常: " + e.getMessage());
            }
            int scoreA = svc.getStudent(idA).orElseThrow().score();
            System.out.println("回滚后 A.score=" + scoreA + "（期望 40，说明第一步 UPDATE 已回滚）");
            svc.twoUpdatesCommit(idA, 51, idB, 52);
            System.out.println("提交成功: A=" + svc.getStudent(idA).orElse(null) + " B=" + svc.getStudent(idB).orElse(null));

            System.out.println();
            System.out.println("--- [4] 观察 AOP 日志 ---");
            System.out.println("上面每一步 Service 调用前后出现的 [AOP] 行来自 LoggingAspect（@Around + 切点 …service..*.*(..)）。");
            System.out.println("@Transactional 由事务拦截器开启/提交/回滚；与日志切面一样，都是代理上的横切织入。");

            System.out.println();
            System.out.println("========== Week22 / Day148：REQUIRED 加入当前事务 ==========");
            ctx.getBean(TxPropagationLab.class).requiredJoin();
            System.out.println("说明：inner 也是 REQUIRED，不会单独提交；inner 抛错 → 与 outer 同一事务整体回滚。");

            System.out.println();
            System.out.println("========== Week22 / Day149：REQUIRES_NEW 独立事务（审计落库，业务可回滚）==========");
            System.out.println("需已在 learn_java 执行 sql/w22_audit_log.sql 建表 audit_log。");
            try {
                ctx.getBean(TxPropagationLab.class).requiresNewAudit();
                System.out.println("说明：审计与业务「同事务」会一起回滚；审计用 REQUIRES_NEW 才能在业务回滚后仍查到审计行。");
            } catch (Exception e) {
                System.out.println("Day149 跳过或失败: " + e.getMessage());
            }

            System.out.println();
            System.out.println("========== Week22 / Day150：rollbackFor 与受检异常 ==========");
            try {
                ctx.getBean(TxPropagationLab.class).rollbackForDemo();
                System.out.println("说明：默认不回滚 checked，是避免把「可预期的业务异常」误当故障；需要时用 rollbackFor 收紧。");
            } catch (Exception e) {
                System.out.println("Day150 失败: " + e.getMessage());
                e.printStackTrace(System.out);
            }

            System.out.println();
            System.out.println("========== Week22 / Day151：JdbcTemplate batchUpdate ==========");
            try {
                ctx.getBean(StudentBatchImporter.class).importThousandAndCompare();
                System.out.println("说明：逐条路径在本示例中为每次 insert 单独提交；批量路径一次发送一批 PreparedStatement。");
            } catch (Exception e) {
                System.out.println("Day151 失败: " + e.getMessage());
                e.printStackTrace(System.out);
            }

            System.out.println();
            System.out.println("========== Week22 / Day152：多切面 @Order + MetricsAspect ==========");
            System.out.println("@Order(1)=MetricsAspect（外层 @Around 计时）；@Order(2)=LoggingAspect（内层 @Around 打日志）。");
            MetricsAspect metrics = ctx.getBean(MetricsAspect.class);
            metrics.reset();
            StudentService probe = ctx.getBean(StudentService.class);
            probe.getStudent(9_999_999L);
            try {
                ctx.getBean(RollbackForLabService.class)
                        .insertThenThrowRuntime("spring_d152_probe_" + System.currentTimeMillis());
            } catch (IllegalStateException ignored) {
                // 触发一次 AfterThrowing
            }
            metrics.printSnapshot("micro-run after reset（2 次调用 + 1 次失败）");
            System.out.println(
                    "@Around 最强：前后都能拦；写错 proceed() 会跳过目标方法或重复调用。@Before/@AfterThrowing 更窄但不易踩坑。");
        }
    }
}
