## 第10周学习计划：Java连接数据库（JDBC + PreparedStatement + 连接池 + DBUtils）

对应原路径：第10周《Java与数据库连接的桥梁》。  
学习时长：每天约 2 小时。

本周核心目标
- 会用 JDBC 完成对 `learn_java` 数据库的 CRUD
- 重点掌握 **PreparedStatement**（预编译、防 SQL 注入）
- 会用连接池（Druid 或 C3P0 二选一，推荐 Druid）
- 会用 DBUtils 简化 JDBC 模板代码
- 为后续 MyBatis/Spring 做铺垫：DAO 分层清晰、异常处理规范、资源关闭正确

本周交付物（必须完成）
- `day64`：`JdbcHelloDemo`（连库、查版本、查当前时间）
- `day65`：`StudentDaoJdbc`（student 表完整 CRUD：增删改查/列表/分页/统计至少1项）
- `day66`：`PreparedStatementDemo`（演示注入风险与防护）
- `day67`：`DruidPoolDemo`（使用连接池获取连接并执行查询）
- `day68`：`DbUtilsStudentDao`（用 DBUtils 重写 student DAO）
- `day69`：`JdbcTxDemo`（事务：转账 or 批量更新，必须体现 rollback）
- `day70`：小整合：把你控制台 `StudentScoreManager` 改为“数据库版”（最小可用即可）
- `W10-notes.md`

目录建议
- `day64` ~ `day70`

每天固定节奏（2小时）
- 20min：复盘（重跑昨天 demo，修 1 个资源/异常问题）
- 70min：写代码并运行（必须可运行）
- 20min：总结入 `W10-notes.md`
- 10min：口述复盘（说清：为什么用 PreparedStatement / 事务边界是什么）

---

## Day 64（JDBC入门：能连上库就赢一半）

学习要点
- JDBC 的四要素：Driver/URL/User/Password
- `Connection`、`Statement`、`ResultSet`
- `try-with-resources` 关闭资源

任务卡（70min）
- 写 `JdbcHelloDemo`
  - 查询版本：`SELECT VERSION()`
  - 查询时间：`SELECT NOW()`

验收标准
- 你能独立写出“连接 -> 执行 -> 读取结果 -> 关闭资源”的最小模板

---

## Day 65（JDBC CRUD：StudentDaoJdbc）

学习要点
- DAO 分层：把 SQL 放到 DAO，Main/Service 不直接拼 SQL
- ResultSet 取值与对象映射（row -> Student）

任务卡（70min）
- 写 `StudentDaoJdbc`
  - `add(Student s)`
  - `deleteById(long id)`
  - `updateScore(long id, int score)`
  - `findById(long id)`
  - `listAll()`
  - （加分但建议做）`listPage(int page, int pageSize)`

验收标准
- CRUD 全部跑通；更新/删除一定带 WHERE

---

## Day 66（PreparedStatement：防注入 + 参数绑定）

学习要点
- SQL 注入的本质：把数据当成代码执行
- PreparedStatement：参数占位符 `?` + `setXxx`

任务卡（70min）
- 写 `PreparedStatementDemo`
  - 用 Statement 做一次“危险拼接”（只演示，不要删库）
  - 用 PreparedStatement 改写为安全版本

验收标准
- 你能解释：PreparedStatement 为什么能防注入（参数不会作为 SQL 语法解析）

---

## Day 67（连接池：Druid 或 C3P0）

学习要点
- 连接池解决：频繁创建/关闭连接的开销
- 常见配置：url/username/password/initialSize/maxActive

任务卡（70min）
- 选择一个（推荐 Druid）
  - Druid：写 `druid.properties` 并在代码里读取，获取 DataSource
  - 或 C3P0：基础配置拿到连接
- 写 `DruidPoolDemo`：从池里拿连接并执行一次查询

验收标准
- 你能解释：为什么“连接一定要 close”，即使是连接池（close 是归还）

---

## Day 68（DBUtils：把模板代码砍掉）

学习要点
- DBUtils 的价值：减少样板代码（try/catch/ResultSet mapping）
- `QueryRunner` + `ResultSetHandler` 思路

任务卡（70min）
- 写 `DbUtilsStudentDao`
  - 至少实现 `findById`、`listAll`、`add`
- 对比：JDBC 版 vs DBUtils 版差了哪些重复代码

验收标准
- 你能说清楚：DBUtils 帮你省掉了哪些步骤（但底层仍是 JDBC）

---

## Day 69（JDBC事务：手动提交与回滚）

学习要点
- `setAutoCommit(false)`
- `commit/rollback`
- 事务边界：一组操作必须打包成功

任务卡（70min）
- 写 `JdbcTxDemo`
  - 复用 week9 的 `account` 表做转账
  - 或者做一个“批量更新 + 中途失败回滚”的演示

验收标准
- 你能做到：人为制造异常后数据不“半成功”

---

## Day 70（周整合：StudentScoreManager数据库版）

目标：把你原来的控制台学生管理升级为“数据库持久化版”（最小可用）。

必须功能（最小版）
- 新增学生（写入 DB）
- 修改成绩（更新 DB）
- 删除学生（删除 DB）
- 按 id 查询（从 DB 查）
- 列表展示（从 DB 查）

结构建议（保持清晰）
- `Student`（model）
- `StudentDao`（接口）
- `StudentDaoJdbc` 或 `DbUtilsStudentDao`（实现）
- `StudentService`（业务）
- `Main`（菜单）

验收标准（完成即过关）
- 程序退出再启动，数据仍在（因为存 DB 了）
- 你能口述：DAO/Service/Main 各自职责是什么

