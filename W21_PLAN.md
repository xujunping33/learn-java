## 第21周学习计划：Spring 核心（IoC / DI / Bean管理 / AOP / JdbcTemplate / 声明式事务）

对应原路径：第21–22周《Spring：IoC/DI/AOP/事务/JdbcTemplate》中的 **第21周（先把 Spring 容器与数据访问跑通）**。  
学习时长：每天约 2 小时。

本周定位（很重要）
- 本周使用 **Spring Framework（非 Spring Boot）**：`AnnotationConfigApplicationContext` 作为入口，专注理解 **容器、Bean、依赖注入、AOP、事务、JdbcTemplate**。
- **Spring Boot** 按原课表在 **第25–26周**开始；本周不学 Boot，避免概念混在一起。

本周核心目标
- 理解 **IoC**：对象创建与装配交给 Spring
- 理解 **DI**：构造器注入优先、字段注入谨慎（建立好习惯）
- 掌握 **Bean 作用域与生命周期**（至少：singleton + `@PostConstruct/@PreDestroy`）
- 掌握 **AOP**：日志/事务边界/切点表达式（先会用）
- 掌握 **JdbcTemplate**：替代手写 JDBC 模板代码
- 掌握 **声明式事务**：`@Transactional` rollbackFor、传播行为先认识常用默认即可

本周交付物（必须完成）
- 新建 Maven 工程：`spring-core-demo/`
- `App.java`：启动 Spring 容器并跑通业务用例
- `StudentService` + `StudentRepository(JdbcTemplate)`：完成最小 CRUD（连你 `learn_java` 或 `oa_demo` 任一库均可，但本周建议先用 **`learn_java.student`**，干扰更少）
- `LoggingAspect`：对 service 层方法做入参/耗时日志（AOP）
- `TxDemo`：一个转账或“两步更新必须同事务”的小例子（成功提交 + 失败回滚）
- `W21-notes.md`

目录建议
- `day141` ~ `day147`

每天固定节奏（2小时）
- 20min：复盘（昨天容器能否重复启动、Bean 是否按预期注入）
- 70min：编码（必须可运行）
- 20min：总结入 `W21-notes.md`
- 10min：口述复盘（用业务语言解释 IoC/DI/AOP/事务）

---

## Day 141（IoC容器：AnnotationConfigApplicationContext）

学习要点
- Spring 容器是什么：`ApplicationContext`
- Java Config：`@Configuration` + `@Bean`

任务卡（70min）
- 创建 `spring-core-demo`
- 写 `AppConfig`：`@Bean` 提供 `Clock` 或简单 `UuidGenerator` 之类的小依赖
- `App.main`：启动容器并打印 bean

验收标准
- 你能解释：为什么不再 `new Service()`（至少说出 1 个可维护性理由）

---

## Day 142（组件扫描：@Component/@Service/@Repository + @Autowired）

学习要点
- stereotype 注解分工（语义化）
- 构造器注入：`@Autowired` 可省略（单构造器）

任务卡（70min）
- 把项目拆成 `service/repository` 包结构
- `StudentService` 依赖 `StudentRepository`（先用假数据/内存版也行）

验收标准
- 容器里能 `getBean(StudentService.class)` 并调用成功

---

## Day 143（配置外置：@PropertySource + Environment）

学习要点
- 配置与代码分离：`jdbc.url/username/password`
- `@Value` 注入（注意：不要到处 `@Value`，优先绑定到配置类）

任务卡（70min）
- 增加 `src/main/resources/application.properties`
- 写 `DataSourceConfig`：创建 `DriverManagerDataSource` 或 `HikariDataSource`（二选一）

验收标准
- 改 properties 就能换库，不需要改 Java 代码

---

## Day 144（JdbcTemplate：查询与更新）

学习要点
- `JdbcTemplate`：`query/queryForObject/update`
- RowMapper：把行映射成对象

任务卡（70min）
- 实现 `StudentRepositoryJdbc`：按 id 查询、插入、更新分数
- 写单测或小 main 用例验证 SQL 正确

验收标准
- 你能对比：JdbcTemplate 比你手写 JDBC 少了哪些样板代码

---

## Day 145（声明式事务：@EnableTransactionManagement + @Transactional）

学习要点
- 事务管理器：`DataSourceTransactionManager`
- `@Transactional` 默认 rollback 只对 RuntimeException（要理解）

任务卡（70min）
- 给 `StudentService` 的一个方法加事务：两步更新，中间抛异常应回滚
- 记录：哪些异常会回滚、哪些不会（写到笔记）

验收标准
- 你能演示：异常发生时数据库没有“半更新”

---

## Day 146（AOP：@Aspect + @Around 记录日志/耗时）

学习要点
- 连接点、切点、通知
- `@Pointcut` 复用切点表达式

任务卡（70min）
- 引入 `spring-aspectj` + `aspectjweaver`
- 写 `LoggingAspect`：拦截 `learn.java.springcoredemo..service..*.*(..)`

验收标准
- 你能解释：AOP 为什么适合横切关注点（日志/事务/安全）

---

## Day 147（周整合：把“学生CRUD + 事务 + AOP + JdbcTemplate”串成可演示用例）

整合任务（必须）
- `App.main` 依次演示：
  - 查询学生
  - 更新分数（成功）
  - 事务回滚用例（失败）
  - 观察 AOP 日志输出是否符合预期

验收标准（完成即过关）
- 你能用 2 分钟讲清楚：请求/用例进入 Spring 后，Bean 怎么装配、事务从哪里开启、AOP 在哪里织入
