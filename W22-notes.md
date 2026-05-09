# 第 22 周笔记（Spring 深化：事务传播 / 批量 / AOP 进阶 / 测试）

继续在 **`spring-core-demo/`**（非 Spring Boot）上迭代。

---

## Day 148：事务传播-1（`REQUIRED` 默认 + 事务边界）

### 学习要点

- **`Propagation.REQUIRED`（默认）**：若当前已有事务则**加入**该事务；若没有则**新建**。外层 **`outer()`** 开启事务后，内层 **`inner()`** 不会另起一条独立物理事务。
- **代理与边界**：**`@Transactional`** 加在 **public** 方法上且通过 **Spring 代理** 调用时，事务拦截器在方法入口挂接连接与提交/回滚。

### 交付

- **`tx/OuterService`**：**`@Transactional(REQUIRED)`** 的 **`outer(suffix)`**：先 **`insert spring_tx148_outer_*`**，再调 **`innerService.inner(suffix)`**。
- **`tx/InnerService`**：**`@Transactional(REQUIRED)`** 的 **`inner(suffix)``**：再 **`insert spring_tx148_inner_*`**，然后抛 **`IllegalStateException`**。
- **`tx/TxPropagationLab.requiredJoin()`**：清理 **`spring_tx148_%`** → 调 **`outer`** 并捕获异常 → **`COUNT(*)` 断言为 0**（证明 outer 的 insert 也被回滚）。
- **`LoggingAspect`**：切点增加 **`…tx..*.*(..)`**，便于在控制台观察 **`OuterService` / `InnerService`** 调用。
- **`App.main`**：在 Day147 演示后调用 **`TxPropagationLab.requiredJoin()`**。

### 口述验收（约 60s）

1. 为什么说 **inner 没有新开事务**，但 **outer 里的写操作仍会回滚**？（同一事务、同一提交边界，异常冒泡后整段回滚。）  
2. **`REQUIRED`** 与「每个方法自己 `commit`」相比，协作方式差在哪里？

---

## Day 149：事务传播-2（`REQUIRES_NEW` 独立事务）

### 学习要点

- **`Propagation.REQUIRES_NEW`**：若当前已存在事务，会**挂起**它，**另开新事务**并在该方法结束时**提交或回滚**；返回后**恢复**外层事务。因此「内层」写入可在**外层仍回滚**时已经**持久化**。
- **典型场景**：**审计、操作留痕、消息投递记录**需要**无论业务成败都留下一条**；与订单/库存等**必须与业务强一致**的数据不同。

### 交付

- **`sql/w22_audit_log.sql`**：在 **`learn_java`** 中建 **`audit_log(event, correlation_id, …)`**（执行一次）。
- **`audit/AuditLogService`**：**`append(event, correlationId)`** 标注 **`@Transactional(propagation = REQUIRES_NEW)`**。
- **`tx/AuditBusinessDemo`**：**`writeAuditThenFail(correlationId)`** 整体 **`@Transactional`**：先 **`append`**，再 **`insert student`**，再抛错。
- **`TxPropagationLab.requiresNewAudit()`**：清理 → 调用 **`writeAuditThenFail`** → 断言 **`student`** 中失败插入不存在、**`audit_log`** 中 **`correlation_id`** 对应 **1 条**。
- **`App.main`**：Day148 之后调用 **`requiresNewAudit()`**（缺表时捕获提示执行 **`sql/w22_audit_log.sql`**）。

### 口述验收（约 60s）

1. **哪些数据必须跟业务同事务**（例如订单总额与明细要么全成要么全撤）？  
2. **哪些必须独立提交**（例如「有人尝试过下单」的审计）？ **`REQUIRES_NEW`** 在这里解决什么问题？

---

## Day 150：`rollbackFor` 与受检异常（checked）

### 学习要点

- Spring **`@Transactional`** 默认 **`rollbackFor`** 只对 **`RuntimeException`** 和 **`Error`** 触发回滚；**受检 `Exception`**（checked）方法签名里 **`throws Exception`** 的那种）**默认提交事务**（方法以异常结束时事务拦截器认为「业务异常」不一定等于故障）。
- **`rollbackFor = Exception.class`**：任意 **`Exception` 子类**（含受检）都触发回滚；范围变大，可能把「本可恢复的校验失败」也打成回滚，需按业务取舍。

### 交付

- **`tx/RollbackForLabService`**：  
  - **`insertThenThrowRuntime`**：抛 **`IllegalStateException`** → 应回滚；  
  - **`insertThenThrowChecked`**：抛 **`Exception`**（受检）→ 默认**不回滚**（演示库里仍有一行）；  
  - **`insertThenThrowCheckedWithRollbackFor`**：**`rollbackFor = Exception.class`** → 回滚。
- **`TxPropagationLab.rollbackForDemo()`**：依次断言三种行为；中间步骤后 **`DELETE`** 掉「默认提交」留下的那一行，避免污染后续断言。
- **`App.main`**：调用 **`rollbackForDemo()`**。

### 口述验收（约 60s）

1. 为何默认**不回滚**受检异常？（历史上区分「业务声明的异常」与「系统故障」；**`RuntimeException`** 更常当作不可继续。）  
2. 何时需要 **`rollbackFor = Exception.class`**？何时反而要 **`noRollbackFor`** 指定某业务异常不回滚？

---

## Day 151：`JdbcTemplate` 批量（`batchUpdate`）

### 学习要点

- **`JdbcTemplate.batchUpdate(sql, BatchPreparedStatementSetter)`**：一条 SQL 模板，多组参数在一次网络往返中批量绑定执行（驱动支持 JDBC batch）。
- **分批**：超大列表按 **500**（本章约定）切块，避免单次 batch 过大。
- **与逐条 insert**：逐条在本示例中多为 **每次独立提交**；批量显著减少 **往返与提交次数**（粗略计时即可看出数量级差异）。

### 交付

- **`StudentRepository.saveAll(List<Student>)`**：**`StudentRepositoryJdbc`** 用 **`batchUpdate`** + **`BatchPreparedStatementSetter`**；**`InMemoryStudentRepository`** 循环 **`insert`**。
- **`batch/StudentBatchImporter`**：**1000** 条 **`w22_<ts>_序号`**，打印 **逐条 insert 耗时** vs **`saveAll` 耗时**，最后 **`DELETE`** 清理。
- **`App.main`**：调用 **`importThousandAndCompare()`**。

### 口述验收（约 60s）

1. **`batchUpdate` 比你手写循环 `PreparedStatement.executeUpdate` 少了哪些样板？  
2. 为何大批量还要 **分批（500/1000）**？（内存、单次 SQL/绑定上限、失败重试粒度等，答出一两点即可。）

---

## Day 152：AOP 进阶（多切面顺序 + 通知类型）

### 学习要点

- **`@Order(n)`**：**数值越小，切面优先级越高**；多个 **`@Around`** 时，**外层**切面先进入、后退出（像洋葱皮）。
- 本仓库约定：**`MetricsAspect @Order(1)`** 在外，**`LoggingAspect @Order(2)`** 在内（业务方法在最里层）。若**对调 Order**，嵌套顺序颠倒，但通常仍都能执行；**谁先打印日志**取决于谁在 **`proceed()`** 链路上更靠近目标方法。
- **`@Around`**：包住 **`proceed()`**，可改返回值、吞异常、不调用目标——**能力最强**，漏写 **`proceed()`** 或重复调用最易出 bug。**`@Before` / `@AfterReturning` / `@AfterThrowing`** 覆盖面窄，各司其职。

### 交付

- **`aop/MetricsAspect`**：**`@Order(1)`**，与日志层相同的 **`applicationLayer`** 切点（含 **`batch`**）；**`@Around`** 统计调用次数与总耗时；**`@AfterThrowing`** 统计异常次数；**`reset()` / `printSnapshot`** 供演示。
- **`LoggingAspect`**：**`@Order(2)`**，切点与 **`MetricsAspect`** 对齐（含 **`batch`**）。
- **`App.main`**：Day151 后 **`reset`** → 两次 probe 调用（一次 **`getStudent`**、一次 **`RollbackForLabService.insertThenThrowRuntime`**）→ **`printSnapshot`**。

### 口述验收（约 60s）

1. **同一切点**上两个 **`@Around`**，**`@Order`** 如何决定谁先包谁？  
2. 为什么说 **`@Around`**「最强也最容易写错」？

---

## Day 153：Spring Test（JUnit5 集成测试）

### 学习要点

- **`@ExtendWith(SpringExtension.class)`**：JUnit5 接入 Spring Test。
- **`@ContextConfiguration(classes = AppConfig.class)`**：与 **`App`** 同源 Java Config 启动容器。
- **测试数据隔离**：统一前缀 **`w153_`**，**`@BeforeEach`** 执行 **`DELETE … LIKE 'w153_%'`**，避免残留、支持重复 **`mvn test`**。

### 交付

- **`spring-test`** + **`junit-jupiter`**（**`scope=test`**），**`maven-surefire-plugin`**。
- **`StudentServiceIntegrationTest`**：新增 / 查不存在 / 改分 / 列表包含（**4** 个 **`@Test`**）。
- **`TxPropagationLabIntegrationTest`**：调用 **`requiredJoin()`**（内部断言 **`spring_tx148_%`** 为 **0**）。

### 口述验收（约 60s）

1. 集成测试与单元测试（mock 掉 **`JdbcTemplate`**）各适合验证什么？  
2. 为何本仓库用 **前缀 + 清理** 而不是单独 **`test` 库**？（课表允许二者之一；本示例走最小依赖路径。）

### 运行

```bash
cd spring-core-demo && mvn test
```

需本机 **`application.properties`** 能连 **`learn_java`** 且存在 **`student`** 表。

---

## Day 154：周整合（能力清单 + 口述演示稿）

### 交付

- **`spring-core-demo/README.md`**：如何运行 **`App`**、如何 **`mvn test`**、W22 四个关键点表。
- 本节：**约 3 分钟口述演示稿**（可与 **`App`** 输出、`README` 对照演示）。

### 口述演示稿（约 3min）

**（1）Spring 事务与 `@Transactional` 怎么生效（约 90s）**

1. 我们用 **`AnnotationConfigApplicationContext(AppConfig.class)`** 启动容器；**`AppConfig`** 上有 **`@EnableTransactionManagement`**，并 **`@Import(DataSourceConfig)`** 得到 **`DataSource`** 和 **`DataSourceTransactionManager`**。  
2. **`StudentService`、`OuterService`** 等带 **`@Transactional`** 的 Bean，容器注入的是 **代理对象**，不是裸类实例。  
3. 外部通过代理调用 **`public`** 方法时，事务拦截器在 **进入方法前** 开启/加入事务，在 **正常返回** 时提交，在 **未捕获的运行时异常**（默认）时回滚。  
4. **同类内部 `this.xxx()`** 不会经过代理，因此 **`@Transactional` 往往不生效**——这就是课程里强调「从外部调 Service」的原因。

**（2）演示：业务回滚但审计不回滚（约 60s）**

1. 执行 **`App`** 里 Day149 段（或单独跑 **`TxPropagationLab.requiresNewAudit()`**）：业务方法 **`AuditBusinessDemo.writeAuditThenFail`** 整体 **`@Transactional(REQUIRED)`**。  
2. 其中 **`AuditLogService.append`** 使用 **`REQUIRES_NEW`**：进入时 **挂起**外层事务，**单独提交**审计行，返回后再执行 **`student` 插入** 并抛错。  
3. 结果：**`student`** 侧业务回滚，无失败行；**`audit_log`** 仍有 **`correlation_id`** 对应的一条——这就是「跟业务同事务」与「必须落一条痕」的拆分。

**（3）批量与 AOP 顺序（约 30s）**

1. **`StudentRepositoryJdbc.saveAll`** 用 **`JdbcTemplate.batchUpdate`**，按 **500** 条分批，减少往返与提交次数；与 **`StudentBatchImporter`** 的计时输出一起看即可。  
2. **`MetricsAspect` `@Order(1)`** 在外，**`LoggingAspect` `@Order(2)`** 在内：同一 **join point** 上多个 **`@Around`**，**order 越小越靠外**，像洋葱皮包住业务方法。

### 验收自检

- [ ] 能不看代码说出：**代理 → `@Transactional` 生效边界**。  
- [ ] 能演示或复述：**`requiresNewAudit`** 后 **student 无脏数据、audit_log 有一条**。  
- [ ] **`README`** 能指导新人：**一条命令跑 App、一条命令跑测试**。
