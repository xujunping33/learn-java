# W8 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 50（安装/连接MySQL + 数据库与表的基本概念）

### 5 条要点
- 数据库（库）像“项目/文件夹”，表像“Excel 表”，行是记录，列是字段
- 主键（Primary Key）用于唯一标识一行数据，常用自增 id
- 索引可以加快查询（先有直觉：像目录），但会占空间、影响写入
- 字符集建议用 `utf8mb4`，能完整支持 Emoji/更多字符
- 最小可验证步骤：能登录并跑 `SELECT VERSION();`、能建库建表并插入查询

### 3 个坑
- 忘记 `USE 数据库;`，导致建表/查表不在你以为的库里
- `username` 需要唯一时忘记加 `UNIQUE`，后面会出现重复脏数据
- 字符集用错（如 `utf8`）可能导致部分字符存不进去（推荐 `utf8mb4`）

### 1 个模板（建库 + 选库）

```sql
CREATE DATABASE IF NOT EXISTS learn_java
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE learn_java;
```

## Day 51（表设计：字段类型 + 约束 + 命名规范）

### 5 条要点
- 表/字段命名统一风格：推荐下划线（如 `created_at`、`base_salary`）
- 常用类型：`BIGINT`(id)、`VARCHAR`(name)、`INT`(score)、`DECIMAL`(money)、`DATETIME`(time)
- 约束优先：`PRIMARY KEY`、`NOT NULL`、`DEFAULT`、必要时 `UNIQUE`
- `updated_at` 常用写法：`DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- 金额/工资建议用 `DECIMAL`：避免 `DOUBLE` 的二进制浮点精度问题

### 3 个坑
- 金额用 `DOUBLE` 容易出现 0.1+0.2 != 0.3 这类精度误差
- 忘记 `NOT NULL/DEFAULT`，会导致数据出现大量 null/脏数据
- `UNIQUE` 约束要慎用：确认业务上确实唯一（例如姓名可能不唯一，真实项目更常用唯一学号/工号）

### 1 个模板（自动更新时间）

```sql
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

## Day 52（DDL：修改表结构 + 索引入门）

### 5 条要点
- `ALTER TABLE` 用于修改表结构：加列/改列/删列
- 索引的价值：加快查询（尤其是经常作为查询条件的列）
- 索引也有成本：占空间、写入/更新更慢（所以不是越多越好）
- `EXPLAIN` 可以观察 SQL 是否用到索引（先看 `key/rows/type` 即可）
- 设计思路：先按查询场景建索引（例如 `employee(dept_id)`、`student(score)`）

### 3 个坑
- `ALTER TABLE` 在大表上会很慢/锁表（生产环境要谨慎）
- 索引列选择不当：对选择性很差的列建索引可能收益不大
- 写了索引但查询没命中（条件写法导致无法走索引），需要用 `EXPLAIN` 验证

### 1 个模板（分页查询模板）

```sql
SELECT * FROM student
ORDER BY id
LIMIT 0, 10;
```

## Day 53（DML：插入/更新/删除）

### 5 条要点
- `INSERT` 可单行或多行：多行一次提交，写法紧凑、网络往返更少
- `UPDATE` / `DELETE` 几乎总要带 `WHERE`；不带 `WHERE` 会作用到全表（极危险）
- 习惯顺序：**先确定要动哪些行（WHERE）**，再写 `SET` / 再执行（防手滑）
- 批量更新用同一条件：如按 `dept_id` 加薪、按比例调薪，注意金额用 `DECIMAL` + `ROUND`
- 按主键删除最稳妥：可先 `SELECT id ...` 或 `SET @id = (...)` 再 `DELETE ... WHERE id = @id`

### 3 个坑
- `UPDATE`/`DELETE` 漏写 `WHERE`：生产事故级错误，练习时也要当真的防
- `UNIQUE` 列（如 `student.name`）插入重复值会报错，多行 `INSERT` 前要确认不撞名
- 先删后插的脚本若外键/业务依赖复杂，要注意顺序；本课练习表无 FK，用命名前缀便于重复跑

### 1 个模板（安全 UPDATE：先限定行）

```sql
-- 先 SELECT 同样 WHERE 看影响行数，再执行 UPDATE
SELECT id, name, score FROM student WHERE name = '某唯一条件';
UPDATE student SET score = 95 WHERE name = '某唯一条件';
```

## Day 54（查询：过滤/排序/分页/聚合）

### 5 条要点
- `WHERE` 过滤行；`ORDER BY` 决定排序，**分页前务必固定排序**（常用主键 `id`），否则每页可能“跳行”
- `LIMIT offset, size`：`offset` 表示跳过前多少行，`size` 表示本页取几行；第 1 页常写 `LIMIT 0, size`，第 2 页 `LIMIT size, size`
- 聚合函数：`COUNT` / `SUM` / `AVG` / `MAX` / `MIN` 对一组行做汇总；`COUNT(*)` 统计行数
- `GROUP BY` 按列分组后再聚合（如按 `dept_id` 统计人数、平均工资）；`SELECT` 里非聚合列一般要出现在 `GROUP BY` 中（MySQL 宽松模式除外，建议写规范）
- `HAVING` 在分组**之后**过滤（对聚合结果设条件）；`WHERE` 在分组**之前**过滤行

### 3 个坑
- 模糊查询 `%` 在前（如 `LIKE '%张'`）往往难走索引，数据大时要谨慎
- `LIMIT 大 offset` 深分页可能很慢（进阶用“游标/上次最大 id”等优化）
- `GROUP BY` 与 `WHERE` 混用时：先 `WHERE` 缩小数据，再 `GROUP BY`，语义更清晰

### 1 个模板（分页查询 + 稳定排序）

```sql
-- pageSize=10，第 n 页（n 从 1 开始）：offset = (n-1)*pageSize
SELECT id, name, score
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 10, 10;   -- 第 2 页：跳过 10 行再取 10 行
```


## Day 55（常用函数：数值/日期时间/字符串）

### 5 条要点
- 数值函数：`ROUND(x, n)` 常用来做金额/薪资结果保留小数；`CEIL/FLOOR` 分别向上/向下取整
- 日期时间：`NOW()` 取当前时间；`DATE_FORMAT(dt, fmt)` 做展示格式化；`DATEDIFF(a,b)` 计算相差天数（\(a-b\)）
- 字符串：`CONCAT(...)` 拼接；`LENGTH(s)` 返回字节长度；`SUBSTRING(s, pos, len)` 截取子串（pos 从 1 开始）
- 把“计算展示”和“写回更新”分开：先用 `SELECT` 算出来看对不对，再决定是否 `UPDATE` 写回
- 养成习惯：所有练习脚本都加 `USE learn_java;`，避免跑错库

### 3 个坑
- `LENGTH` 是**字节长度**（UTF8MB4 下中文可能 > 字符数）；想要字符数用 `CHAR_LENGTH`
- `DATEDIFF` 只算“天”，不含时分秒；更细粒度需要 `TIMESTAMPDIFF`（进阶）
- `SUBSTRING` 截取出来是字符串；需要数字比较/排序时要 `CAST(... AS UNSIGNED)` 或 `CAST(... AS SIGNED)`

### 1 个模板（先算后写）

```sql
-- 先计算展示（确认范围/四舍五入是否符合预期）
SELECT id, name, base_salary, ROUND(base_salary * 1.03, 2) AS after_raise
FROM employee
WHERE dept_id = 2;

-- 确认无误再写回（一定要带 WHERE）
UPDATE employee
SET base_salary = ROUND(base_salary * 1.03, 2)
WHERE dept_id = 2;
```


## Day 56（周整合：用 SQL 完整表达学生/员工管理）

### 5 条要点
- 三份脚本的分工：`week8_schema.sql` 负责“结构”（库/表/约束/索引），`week8_crud.sql` 负责“业务操作”，`week8_functions.sql` 负责“函数练习/表达能力”
- 把控制台菜单映射成 SQL：新增/编辑/删除/详情/列表/筛选/分页/统计，本质就是 `INSERT/UPDATE/DELETE/SELECT`
- 安全习惯：`UPDATE/DELETE` 先写 `WHERE`，必要时先用同条件 `SELECT` 预览影响行数
- 列表/分页稳定性：分页必须有稳定 `ORDER BY`（通常用 `id`）；否则页与页可能“漏/重”
- 统计类需求：先想清“维度”（按什么分组）再写 `GROUP BY`，再用 `HAVING` 过滤聚合结果

### 3 个坑
- `week8_schema.sql` 里有 `DROP TABLE`：会清空数据，执行前要确认（练习环境 OK，生产环境绝对慎用）
- `GROUP BY` 下写非聚合列：建议写规范（非聚合列进 `GROUP BY`），避免依赖 MySQL 宽松行为
- 深分页（大 offset）会慢：数据大时要考虑“按上次最大 id 翻页”等策略（进阶）

### 1 个模板（安全更新/删除三步法）

```sql
-- 1) 先 SELECT 验证条件是否正确
SELECT id, name, score FROM student WHERE id = 1;

-- 2) 再执行 UPDATE/DELETE（一定带 WHERE）
UPDATE student SET score = 95 WHERE id = 1;
-- DELETE FROM student WHERE id = 1;

-- 3) 最后再查一次确认结果
SELECT id, name, score FROM student WHERE id = 1;
```


