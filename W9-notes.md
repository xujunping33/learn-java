# W9 Notes（每日 5 要点 + 3 个坑 + 1 个模板）

## Day 57（数据库规范与范式：把 dept_id 规范化）

### 5 条要点
- 规范化的核心目的：减少冗余、避免不一致、提升可维护性（把“部门信息”从 employee 抽出来）
- 典型做法：新增 `department` 表（`id/name`），`employee` 只保存 `dept_id` 作为引用
- `dept_id` 不应该“随便填数字”：应当指向真实存在的 `department.id`
- 历史数据清洗很重要：把 `dept_id=0/NULL/不存在` 修正为一个“未分配”部门，避免后续加外键失败
- 先做结构+数据清洗（Day57），再加外键约束（Day58），迁移会更稳

### 3 个坑
- 忘记先清洗脏数据就加外键：`ALTER TABLE ... ADD CONSTRAINT` 会直接失败
- 没有“默认部门”兜底：旧数据里 `dept_id=0` 会让你很难收敛
- 只在应用层校验不在数据库层约束：多人/多服务写入时容易出现不一致（学习阶段先把约束练熟）

### 1 个模板（清洗 dept_id）

```sql
-- 兜底部门：未分配
INSERT INTO department (name) VALUES ('未分配')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 把 0/NULL 修正为未分配
UPDATE employee
SET dept_id = (SELECT id FROM department WHERE name = '未分配')
WHERE dept_id IS NULL OR dept_id = 0;
```

## Day 58（外键与约束：让“错误数据”进不来）

### 5 条要点
- 外键让引用关系“在数据库层生效”：`employee(dept_id)` 必须存在于 `department(id)`
- `ON DELETE RESTRICT`：部门被员工引用时不允许删除（避免孤儿记录）
- `ON UPDATE CASCADE`：部门主键变更时联动更新（实际很少改 PK，但理解机制）
- MySQL 的 `ADD CONSTRAINT` 没有 `IF NOT EXISTS`：迁移脚本要考虑“重复执行”的幂等性
- 验证外键是否生效：写“必失败 SQL”比只看 DDL 更可靠（记录错误信息）

### 3 个坑
- 外键列和被引用列类型不一致（例如一个是 INT 一个是 BIGINT）会导致加约束失败或隐患
- 表引擎不是 InnoDB（外键需要 InnoDB）
- 级联策略没想清楚就上 `CASCADE`：可能造成误删/误更新扩大影响面（学习先用 RESTRICT 更安全）

### 1 个模板（幂等添加外键：存在则跳过）

```sql
SET @existing_fk_name := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = DATABASE()
    AND table_name = 'employee'
    AND column_name = 'dept_id'
    AND referenced_table_name = 'department'
  LIMIT 1
);

SET @add_fk_sql := IF(
  @existing_fk_name IS NULL,
  'ALTER TABLE employee ADD CONSTRAINT fk_employee_dept_id FOREIGN KEY (dept_id) REFERENCES department(id) ON UPDATE CASCADE ON DELETE RESTRICT',
  'SELECT ''fk_employee_dept_id already exists'' AS msg'
);

PREPARE stmt_add_fk FROM @add_fk_sql;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;
```

## Day 59（事务基础：BEGIN/COMMIT/ROLLBACK）

### 5 条要点
- 事务边界：一组操作要么都成功（COMMIT），要么都失败（ROLLBACK），避免“半成功”
- 典型例子：转账必须把“扣款 + 加款”放在同一个事务里
- `START TRANSACTION` / `BEGIN`：开启事务；`COMMIT` 提交；`ROLLBACK` 回滚
- 事务里出错后要及时回滚，否则你可能在同一连接里处于未提交状态，造成后续操作混乱
- 演示最可靠的方法：故意制造错误（比如写错表名）观察回滚效果

### 3 个坑
- 把两条 UPDATE 分开执行（不在事务里）：第一条成功第二条失败就会出现脏账
- 忘记 COMMIT：你以为写入成功了，但断开连接后可能被回滚（取决于 autocommit/客户端行为）
- 余额字段用浮点（DOUBLE/FLOAT）：转账会遇到精度问题（建议 DECIMAL）

### 1 个模板（转账事务）

```sql
START TRANSACTION;

UPDATE account SET balance = balance - 200.00 WHERE name = 'alice';
UPDATE account SET balance = balance + 200.00 WHERE name = 'bob';

COMMIT;
```

## Day 60（隔离级别：脏读/不可重复读/幻读）

### 5 条要点
- 四个隔离级别：RU / RC / RR / Serializable（重点掌握 RC、RR）
- RC 常见现象：同一事务内两次查询可能读到不同值（不可重复读）
- RR 常见现象：同一事务内“快照读”通常保持一致（用于对比不可重复读）
- 演示隔离级别必须用“双会话”（A/B 两个连接），单连接很难观察并发现象
- 先记录你机器的默认隔离级别：`SELECT @@global.transaction_isolation, @@session.transaction_isolation;`

### 3 个坑
- 忘了 `START TRANSACTION` 就开始对比：没进事务，现象会完全不同
- 两个会话没在同一张表/同一行上操作（或条件写错），导致你以为“没现象”
- 混淆“不可重复读 vs 幻读”：不可重复读是同一行值变化；幻读是“行数/结果集变化”（通常涉及范围查询）

### 1 个模板（双会话 RC 演示不可重复读）

```sql
-- Session A
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
START TRANSACTION;
SELECT balance FROM account WHERE name = 'alice';  -- v1

-- Session B
START TRANSACTION;
UPDATE account SET balance = balance - 100.00 WHERE name = 'alice';
COMMIT;

-- Session A（同一事务中再次读）
SELECT balance FROM account WHERE name = 'alice';  -- v2（可能 != v1）
COMMIT;
```

## Day 61（索引与 EXPLAIN：看懂“有没有走索引”）

### 5 条要点
- `EXPLAIN` 先看三件事：`type`（访问方式）、`key`（用到的索引）、`rows`（预估扫描行数）
- 常见对比：`=` 精确匹配（更容易走索引） vs `LIKE '%x%'`（通常难走索引）
- 范围查询（`BETWEEN` / `>`）常见是 `range`；无索引时容易变成 `ALL`（全表扫描）
- 联合索引的价值：把 “WHERE 过滤 + ORDER BY 排序” 尽量一起覆盖，减少扫描/排序成本
- 索引不是越多越好：占空间、写入变慢；**以查询场景驱动**来加

### 3 个坑
- 以为加了索引就一定会用：写法不匹配（函数包裹列、前导 `%`、类型隐式转换）可能导致不用索引
- `ORDER BY` 没被索引覆盖会出现 `Using filesort`（不等于一定慢，但要会识别）
- 低选择性的列（比如只有 0/1）建索引收益可能很小，需要结合数据分布判断

### 1 个模板（EXPLAIN 前后对比）

```sql
-- 1) BEFORE
EXPLAIN SELECT id, name, base_salary FROM employee WHERE dept_id = 1 ORDER BY base_salary DESC LIMIT 10;

-- 2) 加索引（示例）
CREATE INDEX idx_employee_dept_salary_id ON employee(dept_id, base_salary, id);

-- 3) AFTER
EXPLAIN SELECT id, name, base_salary FROM employee WHERE dept_id = 1 ORDER BY base_salary DESC LIMIT 10;
```

## Day 62（导入导出：mysqldump 与 SQL 文件恢复）

### 5 条要点
- 备份习惯：做大改动/升级脚本前，先备份（能恢复才算备份）
- 全量导出最省心：`mysqldump --databases learn_java > full.sql`（结构+数据都在）
- 分离导出也要会：`--no-data`（只结构）+ `--no-create-info`（只数据）
- 恢复最常用两种：`mysql < full.sql`（shell）或 MySQL 内 `SOURCE ...`
- 恢复后必须验证：`SHOW TABLES` + 关键表 `SELECT`（例如 `account/employee/department`）

### 3 个坑
- `SOURCE` 路径是“你当前客户端所在机器”的真实路径，路径错就导不进来
- 有外键时可能出现导入顺序问题；全量 dump 通常能处理，但你要会看报错并定位是哪张表
- 备份文件很大时导入变慢正常；练习阶段重点是流程正确与可验证

### 1 个模板（导出 → 删除 → 恢复）

```bash
# 导出
mysqldump -u root -p --databases learn_java > ~/mysql_backup/learn_java_full.sql

# 模拟事故：删表（在 mysql 客户端里执行）
# DROP TABLE learn_java.account;

# 恢复
mysql -u root -p < ~/mysql_backup/learn_java_full.sql

# 验证
mysql -u root -p -e "USE learn_java; SHOW TABLES;"
```

## Day 63（周整合：把 week8 升级为“更真实的数据库”）

### 5 条要点
- 升级演练的闭环：**备份 → 执行升级 → 验证 → 可恢复**
- 外键与规范化让数据更可信：department 表 + employee.dept_id 外键约束
- 事务让“多步写入”具备原子性：转账必须同事务提交/回滚
- 用 `EXPLAIN` 判断是否走索引，并用“前后对比”证明优化有效
- 脚本要尽量幂等（能重复执行），这在真实迁移中非常关键

### 3 个坑
- 没备份就升级：一旦误操作基本不可逆（学习阶段就按生产流程练）
- 只跑脚本不做验证：没有检查点就不知道升级是否真的成功
- 追求“跑通”但不记录现象：Week9 的价值在于你能解释现象（外键报错/回滚/EXPLAIN变化）

### 1 个模板（升级演练检查点）

```sql
-- 1) 结构与外键
SHOW TABLES;
SELECT * FROM department ORDER BY id;
SELECT dept_id, COUNT(*) FROM employee GROUP BY dept_id ORDER BY dept_id;

-- 2) 事务示例结果
SELECT * FROM account ORDER BY id;

-- 3) EXPLAIN 关键查询
EXPLAIN SELECT id, name, base_salary FROM employee WHERE dept_id = 1 ORDER BY base_salary DESC LIMIT 10;
```

