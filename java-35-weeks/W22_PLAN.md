## 第22周学习计划：Spring 深化（事务传播 / JdbcTemplate批量 / AOP进阶 / 测试与工程化习惯）

对应原路径：第21–22周《Spring：IoC/DI/AOP/事务/JdbcTemplate》中的 **第22周（把 Spring 用在“更像真实业务”的场景里）**。  
学习时长：每天约 2 小时。

本周定位（很重要）
- 继续在 `spring-core-demo/` 上迭代（**非 Spring Boot**）。
- 本周重点不是“更多注解”，而是 **事务边界**、**批量 SQL**、**AOP 与事务的关系**、以及 **可重复运行的用例/测试**。

本周核心目标
- 掌握 `@Transactional` 的关键参数：`propagation`、`isolation`、`readOnly`、`rollbackFor/noRollbackFor`、`timeout`（先会用常用组合）
- 理解 **事务传播** 的典型场景：`REQUIRED` / `REQUIRES_NEW` / `NESTED`（至少前两个要会演示）
- 掌握 `JdbcTemplate` **batchUpdate**（批量插入/批量更新）
- AOP 进阶：多个切面顺序、`@Pointcut` 组合、不同通知类型（around/before/afterReturning）
- 建立工程习惯：**JUnit5** 跑 Spring 集成测试（`@ExtendWith(SpringExtension.class)` + `@ContextConfiguration`）

本周交付物（必须完成）
- `spring-core-demo/src/test/java/...`：至少 5 个测试用例（事务/仓库/服务各覆盖）
- `TxPropagationLab`：一组可重复运行的小实验（打印事务名/连接是否相同）
- `StudentBatchImporter`：从 CSV 或内存列表批量导入学生（batchUpdate）
- `AuditAspect`（可选但推荐）：记录关键方法调用（谁调用了什么）
- `W22-notes.md`

目录建议
- `day148` ~ `day154`

每天固定节奏（2小时）
- 20min：复盘（`mvn test` 全绿再进入下一天）
- 70min：编码
- 20min：总结入 `W22-notes.md`
- 10min：口述复盘（用例子解释传播行为）

---

## Day 148（事务传播-1：REQUIRED 默认行为 + 事务边界）

学习要点
- 默认传播：`REQUIRED`（加入当前事务）
- 事务从哪里开始：通常是 **public 方法** + 代理生效

任务卡（70min）
- 写 `TxPropagationLab.requiredJoin()`：
  - `OuterService.outer()` `@Transactional` 调用 `InnerService.inner()`（也 `@Transactional`）
  - inner 抛异常，验证 outer 是否回滚（预期：一起回滚）

验收标准
- 你能解释：为什么 inner 没有“新开事务”也会跟着回滚

---

## Day 149（事务传播-2：REQUIRES_NEW 独立事务）

学习要点
- `REQUIRES_NEW`：挂起当前事务，开新事务
- 典型用途：审计日志必须落库，即使业务回滚

任务卡（70min）
- 写 `AuditLogService.append(...)`：`@Transactional(propagation = REQUIRES_NEW)`
- 业务方法失败回滚后，审计表仍有记录（你需要一张很简单的 `audit_log` 表或用现有表扩展）

验收标准
- 你能口述一个真实业务场景：哪些数据必须“跟业务同事务”，哪些必须“独立提交”

---

## Day 150（rollbackFor / checked exception：把规则写清楚）

学习要点
- Spring 默认：checked exception **不回滚**
- `rollbackFor = Exception.class` 的含义与风险

任务卡（70min）
- 写两个方法对比：
  - 抛 `RuntimeException`：回滚
  - 抛 `Exception`（checked）：默认不回滚；加 `rollbackFor` 后回滚

验收标准
- 你能解释：为什么“默认不回滚 checked”是设计取舍

---

## Day 151（JdbcTemplate 批量：batchUpdate + 参数绑定）

学习要点
- `batchUpdate(String sql, BatchPreparedStatementSetter)`
- 大批量导入：分批（每批 500/1000）避免内存与 SQL 过长

任务卡（70min）
- 实现 `StudentRepositoryJdbc.saveAll(List<Student>)` 使用 batchUpdate
- 导入 1000 条假数据（可用随机 name 前缀 `w22_`）

验收标准
- 你能对比：逐条 insert vs batch 的性能差异（粗略计时即可）

---

## Day 152（AOP进阶：多切面顺序 + 不同通知类型）

学习要点
- `@Order` 控制切面顺序
- `@Before/@AfterReturning/@AfterThrowing` 与 `@Around` 的选择

任务卡（70min）
- 增加第二个切面 `MetricsAspect`：统计异常次数/耗时分布（简单即可）
- 调整顺序：日志切面与指标切面谁在前谁在后，并记录差异（写到笔记）

验收标准
- 你能解释：`@Around` 为什么最强但也最容易写错

---

## Day 153（Spring Test：JUnit5 集成测试）

学习要点
- `@ExtendWith(SpringExtension.class)`
- `@ContextConfiguration(classes = AppConfig.class)`
- 测试数据隔离：使用单独测试库或固定前缀数据 + 清理 SQL

任务卡（70min）
- 给 `StudentService` 写 3 个集成测试：新增/查询/更新
- 给事务传播 lab 写 1 个测试（可用断言看数据库状态）

验收标准
- `mvn test` 可稳定重复运行（不依赖手工改库）

---

## Day 154（周整合：一份“可演示的 Spring 能力清单”）

整合任务（必须）
- 更新 `spring-core-demo/README.md`
  - 如何运行 `App`
  - 如何运行测试
  - 本周实现的 4 个关键点：传播、REQUIRES_NEW、batch、AOP order
- 录制一份“口述演示稿”（写到 `W22-notes.md` 即可）

验收标准（完成即过关）
- 你能用 3 分钟讲清楚：Spring 事务代理怎么影响 `@Transactional` 生效
- 你能演示：业务回滚但审计不回滚（若你做了 REQUIRES_NEW）
