## 第8周学习计划：MySQL（数据库设计 + 建库建表 + 基础CRUD）

对应原路径：第8–9周《数据管理必备利器-MySQL》的第8周部分。  
学习时长：每天约 2 小时。

本周核心目标
- 会按“需求→表→字段→约束→索引”的思路设计数据库
- 熟练建库建表、修改表结构
- 熟练单表 CRUD（增删改查）+ 常用过滤/排序/分页
- 会使用常用函数（数字/日期时间/字符串）
- 为第10周 JDBC 铺路：SQL 要写得规范、可复用、可解释

本周交付物（必须完成）
- `sql/week8_schema.sql`：学生/员工管理的建库建表脚本（含约束与索引）
- `sql/week8_crud.sql`：至少 30 条 CRUD SQL（覆盖常见查询场景）
- `sql/week8_functions.sql`：至少 15 条函数练习 SQL（数值/日期/字符串）
- `W8-notes.md`：每天 5 条要点 + 3 个坑 + 1 个模板（例如“分页查询模板”）

目录建议
- `day50` ~ `day56`
- 新建 `sql/` 目录统一放脚本（从这周开始，脚本也是“代码资产”）

每天固定节奏（2小时）
- 20min：复盘（把昨天的 SQL 重新跑一遍，修 1 个不严谨点）
- 70min：写 SQL + 在 MySQL 客户端执行验证
- 20min：总结写入 `W8-notes.md`
- 10min：口述复盘（说清：这条 SQL 为什么这么写）

---

## Day 50（安装/连接MySQL + 数据库与表的基本概念）

学习要点
- 连接方式：命令行/客户端（你选一个即可）
- 库/表/行/列；主键/外键/索引（先有直觉）
- 字符集与排序规则（知道 UTF8MB4 的意义）

任务卡（70min）
- 确认能登录 MySQL 并执行最简单 SQL
  - `SELECT VERSION();`
  - `SHOW DATABASES;`
- 建库：`learn_java`（或你自己的库名）
- 建一个练习表 `t_user`（最小字段：id、username、created_at）

验收标准
- 你能把“库/表/主键”用 3 句话讲清楚

---

## Day 51（表设计：字段类型 + 约束 + 命名规范）

学习要点
- 常用类型：`INT/BIGINT/VARCHAR/DECIMAL/DATETIME/TINYINT`
- 约束：`PRIMARY KEY`、`NOT NULL`、`UNIQUE`、`DEFAULT`
- 命名：表名/字段名统一风格（推荐下划线）

任务卡（70min）
- 设计并创建两张表（对齐你已有控制台项目）
  - `student`：id、name、score、created_at、updated_at
  - `employee`：id、name、base_salary、dept_id、created_at、updated_at
- 增加必要约束（name 非空、score 范围先用应用层保证即可）

验收标准
- 你能解释：为什么金额建议用 `DECIMAL` 而不是 `DOUBLE`

---

## Day 52（DDL：修改表结构 + 索引入门）

学习要点
- `ALTER TABLE`：加列/改列/删列
- 索引概念：为什么能快；什么时候会慢
- 最基本的索引策略：经常作为查询条件的列

任务卡（70min）
- 用 `ALTER TABLE` 给 `student` 增加 `age` 或 `class_name`（任选）
- 给 `student(name)` 或 `employee(dept_id)` 建索引
- 写 5 条 `EXPLAIN`（先观察有没有用到索引，不要求看懂全部）

验收标准
- 你能说清楚：索引不是越多越好（写入成本、占空间）

---

## Day 53（DML：插入/更新/删除）

学习要点
- `INSERT` 单行/多行
- `UPDATE ... WHERE ...`（先强调：一定要带 where）
- `DELETE ... WHERE ...`

任务卡（70min）
- 向 `student/employee` 各插入 20 条数据（可手写，也可多行 insert）
- 写更新语句：改成绩、改部门、批量加工资（注意 where）
- 写删除语句：按 id 删除、按条件删除（比如 score<60）

验收标准
- 你养成习惯：所有 UPDATE/DELETE 先写 WHERE，再写 SET（防误操作）

---

## Day 54（查询：过滤/排序/分页/聚合）

学习要点
- `WHERE`、`ORDER BY`、`LIMIT offset,size`
- 聚合：`COUNT/SUM/AVG/MAX/MIN`
- 分组：`GROUP BY`、`HAVING`

任务卡（70min）
- 写至少 15 条查询覆盖：
  - 按姓名模糊查询
  - 按分数区间查询 + 排序
  - 分页查询（第1页/第2页）
  - 统计不及格人数
  - 按部门统计人数/平均工资（employee）

验收标准
- 你能写出“分页查询模板”，并解释 offset 的含义

---

## Day 55（常用函数：数值/日期时间/字符串）

学习要点
- 数值：`ROUND/CEIL/FLOOR`
- 日期：`NOW/DATE_FORMAT/DATEDIFF`
- 字符串：`CONCAT/LENGTH/SUBSTRING`

任务卡（70min）
- 写至少 15 条函数练习 SQL（保存到 `week8_functions.sql`）
- 把 `created_at/updated_at` 自动填充（用 DEFAULT 或应用层先行）

验收标准
- 你能把“日期格式化”和“字符串截取”各举 1 个实战例子

---

## Day 56（周整合：把“学生/员工管理”用SQL完整表达一遍）

整合任务（必须）
- 输出三份脚本（放到 `sql/`）
  - `week8_schema.sql`：建库建表 + 约束 + 索引
  - `week8_crud.sql`：增删改查（覆盖你控制台菜单里所有功能对应的 SQL）
  - `week8_functions.sql`：函数练习

验收标准（完成即过关）
- 你能在没有 Java 的情况下，仅用 SQL 完成：新增、修改、删除、查询、列表、统计
- 你能口述：为啥这么设计表，索引建在哪里，查询场景是什么

