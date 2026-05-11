# spring-core-demo

非 Spring Boot 的 **Spring Framework** 学习工程（W21 容器 / JDBC / 事务 / AOP + W22 传播、批量、测试）。入口为 **`App.main`** 与 **JUnit5 集成测试**。

## 环境

- **JDK 17+**、**Maven 3.6+**
- **MySQL**，库名 **`learn_java`**，表 **`student`**（参见与本工程同级目录下的 **`sql/week8_schema.sql`**）
- **`src/main/resources/application.properties`**：配置 **`jdbc.url` / `jdbc.username` / `jdbc.password`**（可与本机一致）
- Day149 审计演示需要 **`audit_log`**：执行 **`sql/w22_audit_log.sql`** 一次

## 运行 `App`

```bash
cd spring-core-demo
mvn compile exec:java
```

将依次执行 W21 Day147 演示与 W22 Day148～Day152 实验（连接数据库、学生 CRUD 示例、事务与批量与 AOP 等）。需数据库可用。

## 运行测试

```bash
cd spring-core-demo
mvn test
```

集成测试使用 **`AppConfig`** 启动容器，数据前缀 **`w153_`** 并在用例前清理。需 **`application.properties`** 能连上 **`learn_java`**。

## 本周（W22）四个关键点速览

| 主题 | 在工程中的落点 |
|------|----------------|
| **事务传播 `REQUIRED`** | `tx/OuterService` + `tx/InnerService`；`TxPropagationLab.requiredJoin()`：内层失败则外层插入一并回滚。 |
| **传播 `REQUIRES_NEW`（业务回滚、审计仍落库）** | `audit/AuditLogService.append`；`tx/AuditBusinessDemo`；`TxPropagationLab.requiresNewAudit()`（需 **`audit_log`** 表）。 |
| **`JdbcTemplate` 批量 `batchUpdate`** | `StudentRepositoryJdbc.saveAll`；`batch/StudentBatchImporter.importThousandAndCompare()`。 |
| **多切面顺序 `@Order`** | `aop/MetricsAspect` `@Order(1)`（外层计时）；`aop/LoggingAspect` `@Order(2)`（内层 `[AOP]` 日志）。 |

更多分层笔记见仓库根目录 **`W22-notes.md`**（含 Day154 口述演示稿）。
