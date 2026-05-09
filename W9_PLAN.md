## 第9周学习计划：MySQL进阶（事务/隔离级别/索引与EXPLAIN/导入导出）

对应原路径：第8–9周《MySQL》的第9周部分。  
学习时长：每天约 2 小时。

本周核心目标
- **范式与约束**：把 employee 的 `dept_id` 从“数字字段”升级为“部门表 + 外键约束”（理解为何要这样做）
- **事务**：理解 ACID，掌握 `BEGIN/COMMIT/ROLLBACK`，能演示并发问题与隔离级别
- **索引与 EXPLAIN**：会用执行计划判断是否走索引，能做最基础的索引优化
- **导入导出**：会用 `mysqldump` 导出，能用 SQL 文件导入恢复

本周交付物（必须完成）
- `sql/week9_schema_upgrade.sql`：新增 `department` 表 + 外键 + 必要索引，并完成从旧数据结构的升级脚本
- `sql/week9_tx_isolation.sql`：事务与隔离级别演示脚本（含可复现步骤）
- `sql/week9_explain_index.sql`：至少 15 条 EXPLAIN（覆盖 student/employee/department 常见查询）
- `sql/week9_backup_restore.md`：你自己的导入/导出流程记录（命令 + 注意事项）
- `W9-notes.md`

目录建议
- `day57` ~ `day63`
- 本周所有脚本放 `sql/`

每天固定节奏（2小时）
- 20min：复盘（重跑昨天脚本，修 1 个不严谨点）
- 70min：写 SQL + 在 MySQL 里执行验证
- 20min：总结入 `W9-notes.md`
- 10min：口述复盘（解释“为什么这样设计/这样加索引/这样设事务”）

---

## Day 57（数据库规范与范式：把 dept_id 规范化）

学习要点
- 规范化的目的：减少冗余、避免不一致、提升可维护性
- 典型做法：部门抽表 `department`，employee 保存 `dept_id` 外键

任务卡（70min）
- 新建 `department` 表（建议字段）
  - `id BIGINT PK AI`
  - `name VARCHAR(50) NOT NULL UNIQUE`
  - `created_at/updated_at`
- 插入 3–5 个部门（如：研发/测试/产品/人事）
- 更新 `employee.dept_id`：让它指向真实部门 id（把 0 修正为一个默认部门或不允许 0）

验收标准
- 你能解释：为什么 `dept_id` 不应该随便填一个数字

---

## Day 58（外键与约束：让“错误数据”进不来）

学习要点
- 外键：`FOREIGN KEY (dept_id) REFERENCES department(id)`
- 级联策略：`ON DELETE`/`ON UPDATE`（理解含义即可）
- 约束优先级：数据库约束 + 应用校验（双保险）

任务卡（70min）
- 给 `employee.dept_id` 加外键（InnoDB）
- 写 5 个“会失败”的 SQL 来验证约束（例如插入不存在 dept_id）
- 记录错误信息（写到 `W9-notes.md`）

验收标准
- 你能解释：为什么外键在一些公司会“慎用”（性能/迁移/分库分表），但学习阶段必须会

---

## Day 59（事务基础：BEGIN/COMMIT/ROLLBACK）

学习要点
- ACID（先能用自己的话解释每个词的意义）
- 事务边界：一组操作要么都成功，要么都失败
- 典型例子：转账（扣款+加款）

任务卡（70min）
- 写 `week9_tx_isolation.sql` 的第一部分：事务基础
  - 创建 `account` 表（id, name, balance）
  - 插入两条账号数据
  - 写转账：成功提交一次；再故意制造失败并回滚一次

验收标准
- 你能解释：为什么“只扣款成功但加款失败”是灾难

---

## Day 60（隔离级别：脏读/不可重复读/幻读）

学习要点
- 四个隔离级别：RU/RC/RR/Serializable（重点记住 RC/RR 常用）
- MySQL InnoDB 默认：通常是 RR（不同版本/配置可能不同）
- 演示方法：开两个会话（两个终端/两个客户端）

任务卡（70min）
- 在 `week9_tx_isolation.sql` 写出“两个会话的操作步骤”
  - 在不同隔离级别下观察现象（至少观察“不可重复读”的差异）
- 记录你观察到的结果：在你机器上默认隔离级别是什么

验收标准
- 你能说清楚：不可重复读 vs 幻读 的区别（先用例子解释即可）

---

## Day 61（索引与 EXPLAIN：看懂“有没有走索引”）

学习要点
- 常见索引：单列索引、联合索引（先理解“最左前缀”即可）
- EXPLAIN 看什么：type、key、rows（先看这三个就够）

任务卡（70min）
- 对你 `week8_crud.sql` 里常用查询做 EXPLAIN（至少 15 条）
  - student：按 name 精确查、按 score 区间查、分页
  - employee：按 dept_id 过滤、按 name 模糊
  - department：按 name 精确查
- 发现一条“走全表扫描”的查询，尝试通过加索引优化（写明前后对比）

验收标准
- 你能解释：为什么 `LIKE '%a%'` 很难走索引（以及可选的改法：前缀匹配/全文索引，先知道即可）

---

## Day 62（导入导出：mysqldump 与 SQL 文件恢复）

学习要点
- 导出：结构+数据
- 导入：从 SQL 文件恢复
- 备份习惯：关键数据先备份再做大改动

任务卡（70min）
- 导出 `learn_java` 库（写进 `week9_backup_restore.md`）
  - 结构+数据各导出一次（或一次全量）
- 删除一个测试库/表后，再从备份恢复（确保你真的会恢复）

验收标准
- 你能独立完成：导出 → 删除 → 导入恢复

---

## Day 63（周整合：把 week8 升级为“更真实的数据库”）

整合任务（必须）
- 输出三份文件：
  - `sql/week9_schema_upgrade.sql`：升级脚本（department+外键+索引）
  - `sql/week9_tx_isolation.sql`：事务与隔离级别演示（含双会话步骤）
  - `sql/week9_explain_index.sql`：EXPLAIN 与索引优化记录
- 完成一次“数据库升级演练”
  - 备份
  - 执行升级脚本
  - 验证 CRUD 正常
  - 出问题能回滚/恢复

验收标准（完成即过关）
- 你能口述：本周做了哪些“更像真实项目”的改造（外键/事务/索引/备份）

