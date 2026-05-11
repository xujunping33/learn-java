# 第 21 周笔记（Spring Framework 核心：IoC / DI / Bean / AOP / JdbcTemplate / 事务）

本周用 **Spring Framework（非 Spring Boot）**，入口 **`AnnotationConfigApplicationContext`**，与第 25–26 周的 Spring Boot 分开学，避免概念搅在一起。

---

## Day 141：IoC 容器（`AnnotationConfigApplicationContext` + Java Config）

### 学习要点

- **容器**：**`ApplicationContext`** 负责创建、缓存、装配 Bean；应用从容器 **取** 实例而不是到处 **`new`**。
- **Java Config**：**`@Configuration`** 标注配置类；**`@Bean`** 标注工厂方法，返回值注册为容器中的 Bean。
- **`@Configuration` 的 CGLIB 代理**：控制台里 **`AppConfig$$SpringCGLIB$$0`** 表示配置类被子类化，保证 **`@Bean` 方法互相调用** 时仍拿到容器管理的单例（与直接 **`new AppConfig()`** 调方法不同）。

### 交付

- Maven 工程 **`spring-core-demo/`**（**`spring-context`**）。
- **`AppConfig`**：**`Clock.systemUTC()`**、依赖 **`Clock`** 的 **`UuidGenerator`**。
- **`App.main`**：**`AnnotationConfigApplicationContext(AppConfig.class)`**，列出 Bean，**`getBean(UuidGenerator.class)`** 调用 **`next()`**。

运行：

```bash
cd spring-core-demo && mvn -q compile exec:java
```

### 口述验收（约 60s）

1. 说清 **IoC**：谁创建 **`UuidGenerator`**、谁把 **`Clock`** 传进去。  
2. 说至少一条「少写 **`new Service()`**」的好处：例如 **依赖集中配置**、换实现（测试用 **`Clock.fixed`**）时 **业务类不用改构造里的 `new`**。

---

## Day 142：组件扫描（`@Component` / `@Service` / `@Repository` + `@Autowired`）

### 学习要点

- **Stereotype**：**`@Repository`** 表数据访问、**`@Service`** 表业务层，语义上帮助分层（与 **`@Component`** 在容器里本质类似，额外带少量语义/异常翻译等约定）。
- **扫描**：在 **`AppConfig`** 上使用 **`@ComponentScan`**，只扫 **`…service`**、**`…repository`**，避免把同包里的 **`AppConfig`** 再扫一遍导致重复定义。
- **构造器注入**：**`StudentService`** 只有一个构造器且参数是 **`StudentRepository`** 时，**`@Autowired` 可省略**；容器会注入唯一的实现 **`InMemoryStudentRepository`**。

### 交付

- **`model/Student`**：**`record`**（**`id, name, score`**）。
- **`repository/StudentRepository`** + **`InMemoryStudentRepository`**（**`@Repository`**，内置 Alice/Bob）。
- **`service/StudentService`**（**`@Service`**，依赖接口 **`StudentRepository`**）。
- **`App.main`**：**`getBean(StudentService.class)`**，**`listStudents()`** / **`getStudent(2)`**。

### 口述验收（约 60s）

1. 说明 **`@Service` / `@Repository`** 各自放在哪一层、**`StudentService` 为何依赖接口而不是具体类**。  
2. 说明 **`@ComponentScan` 的包范围** 若写成整个 **`learn.java.springcoredemo`** 时，要注意 **`AppConfig`** 已被 **`AnnotationConfigApplicationContext(AppConfig.class)`** 注册，可能和扫描到的 **`@Configuration`** 冲突，本周做法是用 **子包扫描** 规避。

---

## Day 143：配置外置（`@PropertySource` + `Environment` / `DataSourceConfig`）

### 学习要点

- **`@PropertySource("classpath:application.properties")`**：把 **JDBC 四要素** 从代码里挪到配置文件。
- **`Environment`**：在 **`DataSourceConfig`** 里用 **`getRequiredProperty("jdbc.*")`** 绑定到 **`DriverManagerDataSource`**，避免在 **`StudentService`** 等类上到处 **`@Value`**。
- **`AppConfig`** 使用 **`@Import(DataSourceConfig.class)`** 注册数据源配置（**`config` 包**不参与 **`@ComponentScan`**，职责清晰）。

### 交付

- **`src/main/resources/application.properties`**：**`jdbc.driver` / `jdbc.url` / `jdbc.username` / `jdbc.password`**。
- **`config/DataSourceConfig`**：**`@Bean` `DataSource`**（**`spring-jdbc`** 的 **`DriverManagerDataSource`** + **`mysql-connector-j`**）。
- **`App.main`**：打印 **`DriverManagerDataSource#getUrl()`**，并 **`try` `getConnection()`**（失败时提示改 **`jdbc.*`** / 检查 MySQL）。

### 口述验收（约 60s）

1. 说明 **改库、改账号** 时只动 **`application.properties`** 即可，**Java 不用改**。  
2. 说明 **`Environment` 集中在 `DataSourceConfig`** 的好处：**配置入口单一**，业务类不感知属性名。

---

## Day 144：`JdbcTemplate`（查询 / 更新 / `RowMapper`）

### 学习要点

- **`JdbcTemplate`**：**`query` / `update`** 封装 **`PreparedStatement`** 与 **`ResultSet`** 关闭；**`RowMapper`** 把一行映射成 **`Student`**。
- **主键回填**：**`GeneratedKeyHolder` + `Statement.RETURN_GENERATED_KEYS`** 拿 **`AUTO_INCREMENT`**。
- **相对手写 JDBC 少写的样板**：手动 **`try-with-resources`**、**`finally` 关流**、循环 **`rs.next()`**、把 **`SQLException`** 转成 **`DataAccessException`** 等，由模板代劳。

### 交付

- **`DataSourceConfig`**：**`@Bean` `JdbcTemplate`**。
- **`StudentRepositoryJdbc`**（**`@Repository` + `@Primary`**）：**`findAll` / `findById`**（**`RowMapper`**）、**`insert(name,score)`**（**`age=0`** 满足表结构）、**`updateScore`**。
- **`StudentRepository`** 接口扩展 **`insert` / `updateScore`**；**`StudentService`** 增加 **`addStudent` / `setScore`**。
- **`InMemoryStudentRepository`**：去掉 **`@Repository`**，避免与 JDBC 实现 **双候选**；**`App.main`** 演示 **插入 → 查询 → 改分**。

### 口述验收（约 60s）

1. 指一段 **`StudentRepositoryJdbc`**：若没有 **`JdbcTemplate`**，手写 JDBC 要多写哪些步骤。  
2. 说明 **`@Primary`**：为何 **`StudentService`** 注入的是 **`StudentRepositoryJdbc`** 而不是内存类。

---

## Day 145：声明式事务（`@EnableTransactionManagement` + `@Transactional`）

### 学习要点

- **`@EnableTransactionManagement`**：打开 Spring **声明式事务**（基于 AOP 代理）。
- **`DataSourceTransactionManager`**：与 **`DataSource`** / **`JdbcTemplate`** 同一数据源绑定，**`@Transactional`** 方法内同一 **`JdbcTemplate`** 自动参与同一物理事务（默认传播 **REQUIRED**）。
- **默认回滚规则**：**`@Transactional`** 默认 **`rollbackFor = RuntimeException` 和 `Error`**；**受检异常（checked）** 默认 **不回滚**（事务仍可能提交），需要时用 **`rollbackFor = Exception.class`** 或 **`@Transactional(rollbackFor = …)`** 显式声明。

### 交付

- **`AppConfig`**：**`@EnableTransactionManagement`**。
- **`DataSourceConfig`**：**`@Bean` `DataSourceTransactionManager`**。
- **`StudentService`**：**`twoUpdatesFailAfterFirst`**（**`@Transactional`**，第一次 **`updateScore`** 后抛 **`IllegalStateException`**）、**`twoUpdatesCommit`**（两步更新成功）。
- **`App.main`**：插入两行 **50 分** → 失败事务后 **A 仍为 50** → 成功事务 **A=60、B=70**。

### 口述验收（约 60s）

1. 说明 **异常抛出后第一步 `UPDATE` 为何在库里看不到**：**事务边界** + **回滚**。  
2. 各举一例：**哪些异常默认会回滚**、**哪些默认不会**（受检异常），以及不想提交时该怎么配 **`rollbackFor`**。

---

## Day 146：AOP（`@Aspect` + `@Around`、切点表达式）

### 学习要点

- **连接点**：方法执行等可被拦截的点；**切点**：用表达式选出哪些连接点；**通知**：在切点上执行的额外逻辑（如 **`@Around`** 包住 **`proceed()`** 前后）。
- **`@Pointcut`**：复用 **`execution(* learn.java.springcoredemo..service..*.*(..))`**，避免 **`@Around`** 上重复长表达式。
- **横切关注点**：日志、事务、安全、指标等与「单条业务规则」正交，用 **AOP** 集中在一处，业务类保持瘦、不复制粘贴样板代码。

### 交付

- **`pom.xml`**：**`aspectjweaver`**；**`spring-context`** 已传递 **`spring-aop`**（**`spring-aspectj` 6.2.1 在 Central 上无对应坐标**，本仓库用此组合即可跑通 **`@Aspect`**）。
- **`AppConfig`**：**`@EnableAspectJAutoProxy`**；**`@ComponentScan`** 增加 **`…aop`**。
- **`aop/LoggingAspect`**：**`@Aspect` `@Component`**，**`@Around("serviceLayer()")`**：打印 **耗时、方法、入参、返回值摘要**（**`Collection`** 只打 **size**）；异常时打 **FAILED** 再抛出。

### 口述验收（约 60s）

1. 指出一次 **`[AOP]`** 行对应 **哪个连接点**、**切点表达式**匹配了谁。  
2. 说明为何 **日志/事务** 适合用 AOP 做，而不是在每个 **`StudentService`** 方法里手写相同代码。

---

## Day 147：周整合（CRUD + 事务 + AOP + `App.main` 演示脚本）

### 学习要点

- **一条 `main` 串起来**：先 **查**（**`listStudents` / `getStudent`**），再 **改分成功**（**`addStudent` + `setScore`**），再 **事务失败回滚 + 事务成功提交**，全程可看 **`[AOP]`** 与 **`FAILED`** 行。
- **口述 2 分钟路线**：**`AppConfig`** 如何把 **`DataSource` / `JdbcTemplate` / `TransactionManager` / 各 `@Service` `@Repository` `@Aspect`** 注册进容器 → 业务从 **`getBean(StudentService)`** 拿到的是 **代理** → **`@Transactional`** 在 **代理入口** 开事务 → **`LoggingAspect`** 的 **`@Around`** 在 **同一套代理链** 上织入（顺序细节可标为进阶）。

### 交付

- **`App.main`**：**Day147 整合脚本**（容器说明 → JDBC 探活 → **[1] 查询** → **[2] 更新成功** → **[3] 回滚 + 再提交** → **[4] AOP 说明**）；若 JDBC 失败则 **提前 return**。

### 口述验收（约 2min）

1. **Bean 装配**：谁提供 **`DataSource`**、谁提供 **`StudentRepositoryJdbc`**、**`StudentService`** 的构造参数从哪来。  
2. **事务**：**`twoUpdatesFailAfterFirst`** 抛 **`RuntimeException`** 后为何库里 **没有 999 分**。  
3. **AOP**：**`[AOP]`** 是谁打的、**不写在 `StudentService` 方法体里**的原因是什么。
