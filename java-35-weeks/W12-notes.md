# W12 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 78（MyBatis 基本流程：跑通第一条 SQL）

### 5 条要点
- MyBatis 的核心流程：`配置 -> SqlSessionFactory -> SqlSession -> Mapper -> SQL -> 结果映射`
- Mapper 接口本身没有实现类：MyBatis 运行时生成 **代理对象** 来执行
- XML 绑定关键：`namespace`（接口全限定名） + `id`（方法名）
- `resultType` / `resultMap`：决定如何把查询结果映射成对象
- 先跑通一条 `selectById`，后面再扩展 CRUD/动态 SQL

### 3 个坑
- `namespace` 写错：找不到 SQL（运行时报 “Invalid bound statement”）
- `id` 与方法名不一致：同样会找不到绑定
- 数据源连不上：账号/密码/URL 参数错误（先用能连通的配置跑通）

### 1 个模板（最小 select）

```xml
<select id="selectById" parameterType="long" resultType="xxx.Student">
  SELECT * FROM student WHERE id = #{id}
</select>
```

## Day 79（参数传递：单参/多参/对象/Map）

### 5 条要点
- 单参数：XML 里直接用 `#{参数名}`（通常与方法参数名一致，或按类型只有一个占位符）
- 多参数：**强烈建议 `@Param("x")`**，XML 里写 `#{x}`，可读性最好
- 不用 `@Param` 时只能用 `param1/param2` 或 `arg0/arg1`（容易写错、难维护）
- 对象参数：用 `#{field}`，对应 JavaBean 的 **getter 属性名**（如 `name/score/age`）
- `insert` 后若需要提交：`session.commit()`（MyBatis 默认不自动提交事务）

### 3 个坑
- 多参没 `@Param`，XML 写 `#{min}` 会找不到参数
- `insert` 后忘记 `commit`，数据不落库
- `useGeneratedKeys` 要写对 `keyProperty`（对象字段名）与表主键列

### 1 个模板（多参 + @Param）

```java
List<Student> selectByScoreRange(@Param("min") int min, @Param("max") int max);
```

```xml
<select id="selectByScoreRange" resultType="...Student">
  SELECT * FROM student WHERE score BETWEEN #{min} AND #{max}
</select>
```

## Day 80（多表关联：employee ↔ department）

### 5 条要点
- 外键语义：`employee.dept_id` → `department.id`，JOIN 用 `ON e.dept_id = d.id`
- 嵌套对象用 **`resultMap` + `<association>`**，把“一行里多组列”拆到主对象和子对象上
- JOIN 时两表常有同名列（如 `name`），SQL 里要给**一侧起别名**（如 `d.name AS d_name`），否则结果集列名冲突
- 一对多子集合用 `<collection>`（本日先建立 association 手感）
- 单表查部门仍可用 `resultType` + 驼峰映射；关联查询才需要 `resultMap`

### 3 个坑
- 只写 `SELECT e.*, d.*`：两表 `name` 会互相覆盖，映射错乱
- `association` 的 `column` 与 SQL 别名不一致：部门对象为 null 或字段对不上
- 忘记在 `mybatis-config.xml` 注册新 `Mapper.xml`：运行期找不到语句

### 1 个模板（association）

```xml
<resultMap id="EmployeeWithDeptRM" type="...Employee">
  <id column="id" property="id"/>
  <result column="name" property="name"/>
  <association property="department" javaType="...Department">
    <id column="d_id" property="id"/>
    <result column="d_name" property="name"/>
  </association>
</resultMap>
```

## Day 81（动态 SQL：where / if / foreach IN）

### 5 条要点
- `<where>`：包住多个 `<if>`，自动加 `WHERE`，并**去掉子句开头多余的 AND/OR**，不会出现 `WHERE AND ...`
- `<if test="...">`：用 OGNL 判断 null、空串；数值条件用 `scoreMin != null`
- 字符串拼接必须用 `#{}` 参数化；`LIKE` 用 `CONCAT('%', #{name}, '%')`，不要手写 `${}` 拼用户输入
- `<foreach>`：`collection` 与接口 `@Param` 名一致，`item` 是循环变量，用于 `IN (...)` 或批量语句
- `<trim prefix="WHERE" prefixOverrides="AND|OR">` 可作为 `<where>` 的替代（更细控制）

### 3 个坑
- 全条件都为空时：`<where>` 内无内容 → 生成**无 WHERE 的全表查询**；业务上有时要强制加 `1=1` 或校验至少一项
- `List` 参数未加 `@Param("deptIds")` 时，XML 里 `collection` 要用 `list` 或 `param1`（易混）
- OGNL 里 `and`/`or` 与 XML 属性冲突时注意写法；比较运算符在 XML 中用 `&gt;` `&lt;`

### 1 个模板（where + if）

```xml
<select id="selectByCondition" parameterType="...StudentQuery" resultType="...Student">
  SELECT ... FROM student
  <where>
    <if test="name != null and name != ''">
      AND name LIKE CONCAT('%', #{name}, '%')
    </if>
    <if test="scoreMin != null">AND score &gt;= #{scoreMin}</if>
  </where>
</select>
```

## Day 82（foreach：批量插入 / IN / 批量删除）

### 5 条要点
- `<foreach collection="..." item="..." open="(" separator="," close=")">`：拼 `IN (?,?,?)` 或 `VALUES (...),(...)`
- `collection` 必须与 Mapper 方法上的 **`@Param` 名**一致（如 `ids`、`list`）
- 批量 `INSERT ... VALUES` 一次网络往返，比循环单条 insert 更高效（仍注意单包大小上限）
- `separator`：行间用 `,`；`open`/`close` 包住整段 IN 或括号
- `deleteByIds` / `selectByIds` 在 **集合为空** 时不要生成 `IN ()`（非法 SQL），应在 Java 层判空

### 3 个坑
- `IN ()` 空列表：MySQL 语法错误；`foreach` 前加 `if test="ids != null and ids.size()>0"` 或在 Service 直接 return
- 批量 insert 想用自增主键回填：依赖驱动 + MyBatis 版本；多行 insert 时确认各 `Student` 是否都写回 `id`
- `item` 名与占位符：`#{s.name}` 对应 `item="s"`，不要写错层级

### 1 个模板（批量 VALUES）

```xml
<insert id="insertBatch" useGeneratedKeys="true" keyProperty="id" keyColumn="id">
  INSERT INTO student (name, score, age) VALUES
  <foreach collection="list" item="s" separator=",">
    (#{s.name}, #{s.score}, #{s.age})
  </foreach>
</insert>
```

## Day 83（分页：LIMIT + PageRequest）

### 5 条要点
- MySQL：`LIMIT offset, pageSize`，其中 **`offset = (page - 1) * pageSize`**
- Java 侧用 **`PageRequest(page, pageSize)`** 封装页码与每页条数，统一算 `getOffset()`
- 分页查询必须带 **`ORDER BY`**（常用主键 `id`），保证结果顺序稳定
- 无 `ORDER BY` 时，MySQL 不保证行顺序；并发插入/更新后，同一 `offset` 可能看到不同行（“跳动”）
- `pageSize` 可在封装类里做上限（如 500），避免一次拉爆内存

### 3 个坑
- 只写 `LIMIT` 不写排序：翻页或数据变化时体验异常，难以排查
- `page` 从 0 还是从 1：团队约定要统一；本示例为 **从 1 开始**
- 大 offset 深分页性能差（如 `LIMIT 100000, 10`），线上常改用游标/搜索 after id 等方案

### 1 个模板（Mapper）

```xml
<select id="selectPage" resultType="...Student">
  SELECT ... FROM student
  ORDER BY id ASC
  LIMIT #{offset}, #{pageSize}
</select>
```

```java
List<Student> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
```

## Day 84（缓存 + 日志：一级 / 二级、logback）

### 5 条要点
- **一级缓存**：默认开启，绑定 **SqlSession**；同一 session 内相同 Mapper 语句 + 相同参数，可走缓存，减少重复查库
- **二级缓存**：Mapper 命名空间级，跨 SqlSession；需在 Mapper XML 根节点加 `<cache/>`（且对实体序列化等有要求），默认不开启
- **logback**：`org.apache.ibatis` + `java.sql` 设 **DEBUG** 可看 SQL、参数绑定；生产常降为 INFO 并配合专用 SQL 审计
- 写操作（insert/update/delete）或 `commit`/`rollback`/`SqlSession.clearCache()` 可能使一级缓存失效或刷新，避免读到脏数据
- 观察缓存：对照日志里 **JDBC Preparing/Executing 出现次数**（同一 session 第二次 select 往往不再出现）

### 3 个坑
- 把一级缓存当“分布式缓存”：它只是会话内优化，**不跨 JVM、不跨节点**
- 盲目开二级缓存：多副本、写多读少、关联表变更时易出现**脏读**，需结合业务与失效策略
- 日志全开 DEBUG：量大且可能带敏感数据，**仅开发/排障**时开

### 1 个模板（口述验收）

- **一级缓存为什么默认“相对安全”**：作用域小（仅当前 SqlSession），随会话结束而释放，不易像二级那样在多会话间长期持有过期数据。
- **二级为什么要谨慎**：跨会话共享，别的连接改了表，缓存若未及时失效，会返回旧行；集群下还要考虑缓存一致性。

### 运行 `CacheDemo`

```bash
cd mybatis-demo
mvn compile exec:java@cache-demo -Dexec.args="21"
```

同一 SqlSession 内两次 `selectById` 只应出现 **一组** `==> Preparing`；新开 SqlSession 后再查会出现第二组。

（可选）在 `StudentMapper.xml` 的 `<mapper>` 内、紧接在 `namespace` 后增加 `<cache/>` 可试验二级缓存；实体需可序列化等，实验后建议删掉，避免误用。

