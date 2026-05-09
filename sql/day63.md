# Day63：Week9 周整合（升级演练）

目标：把 Week8 的数据库升级为更真实的结构，并完成一次“升级演练”闭环。

## 0) 演练原则（很重要）

- **先备份**：升级前必须备份（能恢复才算备份）
- **可重复执行**：脚本最好能重复跑（幂等/可跳过已存在对象）
- **可验证**：每一步都有检查点（SELECT/SHOW/EXPLAIN）
- **可恢复**：出问题能回滚/恢复（至少会从 dump 恢复）

## 1) 备份（Day62）

按 `sql/week9_backup_restore.md` 做一次全量导出：

- [ ] `mysqldump --databases learn_java > ~/mysql_backup/learn_java_full.sql`
- [ ] `ls -lh ~/mysql_backup/learn_java_full.sql` 确认文件存在且不为空

## 2) 执行升级脚本（一键）

在 MySQL 里执行：

```sql
SOURCE /home/xjp/xjp/code/learn/java/sql/day63.sql;
```

## 3) 检查点（必须验证）

### 3.1 外键与部门

```sql
USE learn_java;
SELECT * FROM department ORDER BY id;
SELECT dept_id, COUNT(*) AS headcount FROM employee GROUP BY dept_id ORDER BY dept_id;
```

### 3.2 事务脚本是否可重复跑

```sql
SELECT * FROM account ORDER BY id;
```

### 3.3 EXPLAIN 与索引对比

重点看：`type/key/rows/Extra`，并记录“加联合索引前后”的变化。

## 4) 隔离级别演示（Day60，双会话）

打开两个 MySQL 连接，按 `sql/week9_tx_isolation.sql` 的 Day60 注释步骤操作，并把观察结果写进 `W9-notes.md`。

## 5) 出问题如何恢复（必须会）

如果升级脚本跑崩或数据被你误操作：

```bash
mysql -u root -p < ~/mysql_backup/learn_java_full.sql
```

恢复后，再跑 `SHOW TABLES;` 和关键表 `SELECT` 验证。

## 6) 今日验收（完成即过关）

- [ ] 我能完成：备份 → 执行升级 → 验证 →（出问题时）恢复
- [ ] 我能口述：本周做了哪些“更像真实项目”的改造（外键/事务/索引/备份）

