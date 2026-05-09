-- Week9 EXPLAIN 与索引练习（Day61）
--
-- 目标：
-- - 至少 15 条 EXPLAIN，覆盖 student/employee/department 常用查询
-- - 做 1 个“加索引前后对比”小案例
--
-- 提示：先关注这几个字段
-- - key：用到哪个索引
-- - type：访问类型（越接近 const/ref/range 越好；ALL 是全表扫描）
-- - rows：预估扫描行数（越小越好）

USE learn_java;

-- 0) 确保 department 结构存在（如果你已跑过 day57/day58，这里不会改变数据含义）
SOURCE /home/xjp/xjp/code/learn/java/sql/week9_schema_upgrade.sql;

-- ============================================================
-- A. student：常见查询的 EXPLAIN
-- 已有索引：
-- - uk_student_name(name)
-- - idx_student_score(score)
-- ============================================================

-- S1：按主键查（const）
EXPLAIN SELECT * FROM student WHERE id = 1;

-- S2：按唯一键 name 精确查（const/ref）
EXPLAIN SELECT * FROM student WHERE name = 'alice';

-- S3：按 score 范围（range）
EXPLAIN SELECT id, name, score FROM student WHERE score BETWEEN 60 AND 90 ORDER BY score DESC, id ASC;

-- S4：按 score 排序分页（注意是否 filesort）
EXPLAIN SELECT id, name, score FROM student ORDER BY score DESC LIMIT 0, 10;

-- S5：按 name 前缀匹配（可能走索引）
EXPLAIN SELECT id, name FROM student WHERE name LIKE 'a%' ORDER BY id;

-- S6：按 name 包含匹配（通常难走索引）
EXPLAIN SELECT id, name FROM student WHERE name LIKE '%a%' ORDER BY id;

-- S7：按 age 过滤（无索引：可能 ALL）
EXPLAIN SELECT id, name, age FROM student WHERE age = 18 ORDER BY id;

-- S8：统计：COUNT(*)
EXPLAIN SELECT COUNT(*) FROM student;

-- S9：按 age 分组（观察 rows/type）
EXPLAIN SELECT age, COUNT(*) FROM student GROUP BY age;

-- ============================================================
-- B. department：常见查询的 EXPLAIN
-- 已有索引：
-- - uk_department_name(name)
-- ============================================================

-- D1：按 name 精确查（唯一索引）
EXPLAIN SELECT * FROM department WHERE name = '研发';

-- D2：按 name 前缀匹配（可能走索引）
EXPLAIN SELECT id, name FROM department WHERE name LIKE '研%';

-- ============================================================
-- C. employee：常见查询 + join 的 EXPLAIN
-- 已有索引：
-- - idx_employee_dept_id(dept_id)
-- - idx_employee_name(name)
-- ============================================================

-- E1：按 dept_id 过滤（ref/range）
EXPLAIN SELECT id, name, base_salary, dept_id FROM employee WHERE dept_id = 1 ORDER BY id;

-- E2：按 name 前缀（可能走 idx_employee_name）
EXPLAIN SELECT id, name FROM employee WHERE name LIKE 'e%';

-- E3：按 name 包含（通常难走索引）
EXPLAIN SELECT id, name FROM employee WHERE name LIKE '%e%';

-- E4：工资区间（无索引：可能 ALL）
EXPLAIN SELECT id, name, base_salary FROM employee WHERE base_salary BETWEEN 5000 AND 8000 ORDER BY base_salary DESC;

-- E5：按部门 + 工资排序（常见列表）
EXPLAIN SELECT id, name, base_salary, dept_id FROM employee WHERE dept_id = 1 ORDER BY base_salary DESC, id ASC;

-- E6：join（员工列表带部门名）
EXPLAIN
SELECT e.id, e.name, e.base_salary, d.name AS dept_name
FROM employee e
JOIN department d ON e.dept_id = d.id
WHERE d.name = '研发'
ORDER BY e.base_salary DESC, e.id ASC;

-- E7：按部门统计人数（group by）
EXPLAIN SELECT dept_id, COUNT(*) AS headcount FROM employee GROUP BY dept_id;

-- ============================================================
-- D. 索引优化小案例（前后对比）
-- 查询：WHERE dept_id=? ORDER BY base_salary DESC, id ASC
-- 目标：用联合索引减少扫描与排序成本
-- ============================================================

-- BEFORE：观察 key/type/rows/Extra
EXPLAIN
SELECT id, name, base_salary, dept_id
FROM employee
WHERE dept_id = 1
ORDER BY base_salary DESC, id ASC
LIMIT 0, 10;

-- 幂等添加联合索引：idx_employee_dept_salary_id(dept_id, base_salary, id)
SET @idx_exists := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'employee'
    AND index_name = 'idx_employee_dept_salary_id'
);

SET @add_idx_sql := IF(
  @idx_exists = 0,
  'CREATE INDEX idx_employee_dept_salary_id ON employee(dept_id, base_salary, id)',
  'SELECT ''idx_employee_dept_salary_id already exists'' AS msg'
);

PREPARE stmt_add_idx FROM @add_idx_sql;
EXECUTE stmt_add_idx;
DEALLOCATE PREPARE stmt_add_idx;

-- AFTER：再看一次 EXPLAIN，对比 key/type/Extra 是否更理想
EXPLAIN
SELECT id, name, base_salary, dept_id
FROM employee
WHERE dept_id = 1
ORDER BY base_salary DESC, id ASC
LIMIT 0, 10;

