# W10 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 64（JDBC 入门：能连上库就赢一半）

### 5 条要点
- JDBC 四要素：Driver / URL / User / Password
- 最小链路：`Connection -> Statement -> ResultSet`（执行查询并读取结果）
- 资源关闭优先用 `try-with-resources`（避免连接泄漏）
- 连接字符串里数据库名要对（本周用 `learn_java`）
- 先写“能跑通的最小 demo”，后面再抽工具类/DAO

### 3 个坑
- 没加 MySQL JDBC Driver（`mysql-connector-j`）会报 “No suitable driver”
- URL 写错（端口/库名/参数）会连不上或乱码
- 忘记关闭 `ResultSet/Statement/Connection`：程序跑几次就把连接占满

### 1 个模板（最小查询）

```java
try (Connection conn = DriverManager.getConnection(url, user, password);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT VERSION(), NOW()")) {
    if (rs.next()) {
        System.out.println(rs.getString(1));
        System.out.println(rs.getString(2));
    }
}
```

## Day 65（JDBC CRUD：StudentDaoJdbc）

### 5 条要点
- DAO 的目标：把“SQL + JDBC 细节”集中在一处，主程序只调用方法，不直接拼 SQL
- CRUD 最小闭环：`add / findById / updateScore / deleteById / listAll`
- 用 `PreparedStatement`：参数用 `?` 绑定，避免拼接字符串导致 SQL 注入/类型问题
- `ResultSet` 到对象映射：一行 row -> 一个 `Student` 对象（字段名要对）
- 分页模板：`ORDER BY id LIMIT offset, pageSize`，offset = (page-1)*pageSize

### 3 个坑
- `UPDATE/DELETE` 忘写 `WHERE` 是事故级错误（先写 WHERE 再写 SET）
- `name` 有唯一约束（`uk_student_name`）：插入重复会报错，Demo 建议用唯一前缀/时间戳
- 读取时间字段用 `Timestamp -> LocalDateTime` 更直观；别用字符串硬解析

### 1 个模板（分页查询）

```sql
SELECT id, name, score, age
FROM student
ORDER BY id
LIMIT ?, ?;
```

## Day 66（PreparedStatement：防注入 + 参数绑定）

### 5 条要点
- SQL 注入本质：把“用户输入”当成 SQL 语法的一部分执行（拼接字符串最危险）
- `Statement` 拼接：`"... WHERE name = '" + input + "'"` 很容易被构造输入绕过条件
- `PreparedStatement`：用 `?` 占位符 + `setXxx()` 绑定参数，输入只作为“数据”处理
- 写 JDBC 时优先用 PreparedStatement（不仅防注入，也减少类型转换/引号处理错误）
- 演示注入建议用“只读查询/绕过条件”，不要写破坏性 SQL

### 3 个坑
- 占位符 `?` 只能替代“值”，不能替代表名/列名/SQL 片段（那些需要白名单拼接）
- 忘记按顺序绑定参数（`setString(1, ...)`）会导致报错或逻辑错误
- 仍然用字符串拼接拼 WHERE（哪怕最后用了 PreparedStatement），等于没防住

### 1 个模板（安全查询）

```java
String sql = "SELECT COUNT(*) FROM student WHERE name = ?";
try (Connection conn = Db.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, inputName);
    try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        System.out.println(rs.getLong(1));
    }
}
```

## Day 67（连接池：Druid 或 C3P0）

### 5 条要点
- 连接池解决的问题：频繁 `new Connection` 的开销大；池化后重复利用连接
- 连接池里的 `close()` 含义：**不是断开**，而是“归还连接到池”
- 关键配置思路：`initialSize/min/max`（或同类参数），防止连接无限增长
- 连接池用法：从 `DataSource` 拿连接，和 JDBC 用法一致（拿到还是 `Connection`）
- 学习时先跑通：用池拿连接执行一次查询即可

### 3 个坑
- 忘记 close：连接不会回到池，最终“池耗尽”表现为卡住/超时
- 池大小设置太小或太大：太小会抢连接，太大浪费资源（学习阶段用 3~10 即可）
- 把连接当线程安全对象到处共享：正确做法是每次用完就 close（归还）

### 1 个模板（拿连接执行查询）

```java
try (Connection conn = dataSource.getConnection();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT NOW()")) {
    rs.next();
    System.out.println(rs.getString(1));
}
```

## Day 68（DBUtils：把模板代码砍掉）

### 5 条要点
- DBUtils 的核心价值：把“创建/关闭 Statement、循环 ResultSet”这些模板代码交给库处理
- `QueryRunner`：提供 `query/update`，你只写 SQL + 参数 + 结果映射
- `ResultSetHandler`：把 ResultSet -> 你的对象/列表（这是 DBUtils 的核心扩展点）
- DBUtils 仍然是 JDBC：底层依然是连接/PreparedStatement，只是帮你封装重复劳动
- 迁移思路：先把 JDBC DAO 跑通，再用 DBUtils 重写一版对比差异

### 3 个坑
- 以为 DBUtils 能自动搞定所有类型映射：时间字段（Timestamp/LocalDateTime）经常需要你自己处理
- 忘记 DataSource（连接池）：DBUtils 最佳搭配是 DataSource，不要自己每次 DriverManager 连
- SQL 写错时异常会直接抛出：要在上层统一处理并给出可读提示

### 1 个模板（QueryRunner + ResultSetHandler）

```java
QueryRunner runner = new QueryRunner(dataSource);
String sql = "SELECT id, name, score, age FROM student WHERE id = ?";
Student s = runner.query(sql, rs -> rs.next() ? mapRow(rs) : null, 1L);
```

## Day 69（JDBC事务：手动提交与回滚）

### 5 条要点
- 默认是自动提交：每条 SQL 都是一个“隐式事务”（要手动事务必须 `setAutoCommit(false)`）
- 手动事务三件套：`setAutoCommit(false)` → 成功 `commit()` → 失败 `rollback()`
- 事务边界要包住“一组必须一起成功的操作”（转账：扣款 + 加款）
- 发生异常后必须 rollback，否则会出现“半成功”或连接处于不确定状态
- `finally` 里通常要把 `autoCommit` 复原（避免影响后续逻辑）

### 3 个坑
- 只 catch 不 rollback：扣款成功、加款失败就会脏账
- 在不同连接里执行两条更新：事务只能保证“同一连接”内的一致性
- 忘记关闭连接：连接池下 close 是归还；不 close 会耗尽连接

### 1 个模板（事务模板）

```java
try (Connection conn = Db.getConnection()) {
    conn.setAutoCommit(false);
    try {
        // do multi-step updates...
        conn.commit();
    } catch (Exception e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
    }
}
```

## Day 70（周整合：StudentScoreManager 数据库版）

### 5 条要点
- “数据库版”最核心的变化：增删改查都落到 DB，程序重启数据仍在
- 分层价值：Main 不写 SQL；DAO 只管数据访问；Service 负责业务校验与流程
- JDBC 仍要坚持：PreparedStatement + try-with-resources（资源关闭正确）
- 失败要可恢复：捕获异常并提示，不让程序直接崩掉
- 接口抽象（StudentDao）让你后续替换 DBUtils/MyBatis 更顺滑

### 3 个坑
- 直接在 Main 里拼 SQL：短期快，长期会乱（难维护/难测试）
- 忘记 WHERE：update/delete 事故级（你现在要养成“先写 WHERE”习惯）
- 一次操作报错导致程序退出：要统一兜底并继续提供服务

### 1 个模板（菜单层统一兜底）

```java
try {
    // call service
} catch (Exception e) {
    System.out.println("操作失败：" + e.getMessage());
}
```

