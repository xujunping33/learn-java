# Week9 备份与恢复（Day62）

目标：你能独立完成 **导出 → 删除（模拟事故）→ 导入恢复**，并知道每一步怎么验证。

## 0. 前置检查（确认能连上 MySQL）

```bash
mysql --version
mysql -u root -p -e "SELECT VERSION();"
mysql -u root -p -e "SHOW DATABASES LIKE 'learn_java';"
```

如果你不是 `root` 用户，把 `root` 替换成你自己的用户名。

## 1. 创建备份目录（建议放到仓库外）

```bash
mkdir -p ~/mysql_backup
```

## 2. 全量导出（结构 + 数据，推荐）

```bash
mysqldump -u root -p --databases learn_java > ~/mysql_backup/learn_java_full.sql
```

### 验证导出是否成功

```bash
ls -lh ~/mysql_backup/learn_java_full.sql
head -n 20 ~/mysql_backup/learn_java_full.sql
```

你应该能看到 `CREATE DATABASE` / `USE learn_java` / `CREATE TABLE` 等语句。

## 3. 分开导出（结构 / 数据），可选

### 3.1 只导结构（不含数据）

```bash
mysqldump -u root -p --no-data learn_java > ~/mysql_backup/learn_java_schema.sql
```

### 3.2 只导数据（不含建表）

```bash
mysqldump -u root -p --no-create-info learn_java > ~/mysql_backup/learn_java_data.sql
```

## 4. 模拟“误操作”与恢复（必须练一次）

### 4.1 模拟删除一个测试表（推荐删 `account` 或 `department`，不要删你太在意的数据）

进入 MySQL：

```sql
USE learn_java;
SHOW TABLES;
DROP TABLE account;
```

验证确实删掉了：

```sql
SHOW TABLES LIKE 'account';
```

### 4.2 从备份恢复

方式 A：在 shell 里恢复（推荐）

```bash
mysql -u root -p < ~/mysql_backup/learn_java_full.sql
```

方式 B：在 MySQL 客户端里恢复

```sql
SOURCE /home/xjp/mysql_backup/learn_java_full.sql;
```

（注意：这个路径要和你实际备份路径一致）

### 4.3 恢复后验证

```bash
mysql -u root -p -e "USE learn_java; SHOW TABLES;"
mysql -u root -p -e "USE learn_java; SELECT * FROM account ORDER BY id;"
```

## 5. 常见坑（你遇到哪个就记到 W9-notes.md）

- **权限问题**：没有权限导出/导入，换有权限的用户或给权限。
- **字符集**：建议库/表统一 `utf8mb4`，避免导入后乱码。
- **外键约束导致导入失败**：表之间有依赖时，需要按顺序创建表/插入数据；全量 dump 一般会处理好。
- **导入很慢**：数据量大时是正常的，生产环境要用更谨慎的方式（这里先练会流程）。
- **路径错误**：`SOURCE` 用的是 MySQL 客户端的机器路径，不是“相对 SQL 文件里的路径”。

## 6. 你的本机命令记录（今天把你实际执行过的命令粘贴在这里）

- 导出命令：
- 删除/模拟事故：
- 导入命令：
- 验证命令：

