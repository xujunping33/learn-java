-- Week8 CRUD（Day56 周整合）
-- 目标：仅用 SQL 覆盖“学生/员工管理”的常见功能：新增、修改、删除、查询、列表、统计
-- 提示：
-- - UPDATE/DELETE 永远先写 WHERE（防误操作）
-- - 分页查询务必配稳定 ORDER BY（常用 id）

USE learn_java;

-- ============================================================
-- A. Student CRUD（student）
-- 字段：id, name, score, age, created_at, updated_at
-- ============================================================

-- A1 新增学生（单行）
INSERT INTO student (name, score, age) VALUES ('alice', 88, 18);

-- A2 新增学生（多行）
INSERT INTO student (name, score, age) VALUES
  ('bob', 76, 19),
  ('carol', 92, 18),
  ('dave', 59, 20);

-- A3 查询：按 id 精确查（详情页）
SELECT * FROM student WHERE id = 1;

-- A4 查询：按 name 精确查（name 有 UNIQUE 更适合做唯一定位）
SELECT * FROM student WHERE name = 'alice';

-- A5 查询：按 name 模糊查（搜索框）
SELECT id, name, score, age
FROM student
WHERE name LIKE '%a%'
ORDER BY id;

-- A6 查询：按分数区间 + 排序（列表筛选）
SELECT id, name, score, age
FROM student
WHERE score BETWEEN 60 AND 90
ORDER BY score DESC, id ASC;

-- A7 查询：不及格列表（score < 60）
SELECT id, name, score, age
FROM student
WHERE score < 60
ORDER BY score ASC, id ASC;

-- A8 列表：按 id 排序（基础列表）
SELECT id, name, score, age, created_at, updated_at
FROM student
ORDER BY id DESC;

-- A9 分页：第 1 页（pageSize=10）
SELECT id, name, score, age
FROM student
ORDER BY id
LIMIT 0, 10;

-- A10 分页：第 2 页（pageSize=10）
SELECT id, name, score, age
FROM student
ORDER BY id
LIMIT 10, 10;

-- A11 修改：按 id 改分数/年龄（编辑功能）
UPDATE student
SET score = 90, age = 19
WHERE id = 1;

-- A12 修改：按 name 改分数（当 name 是唯一定位时）
UPDATE student
SET score = score + 5
WHERE name = 'bob';

-- A13 修改：批量加分（示例：全部及格线以下 +10 分，上限不超过 100）
UPDATE student
SET score = LEAST(score + 10, 100)
WHERE score < 60;

-- A14 删除：按 id 删除（最安全）
DELETE FROM student WHERE id = 2;

-- A15 删除：按条件删除（示例：分数为 0 的无效数据）
DELETE FROM student WHERE score = 0;

-- A16 统计：总人数/平均分/最高分/最低分
SELECT
  COUNT(*) AS total_cnt,
  ROUND(AVG(score), 2) AS avg_score,
  MAX(score) AS max_score,
  MIN(score) AS min_score
FROM student;

-- A17 统计：及格/不及格人数
SELECT
  SUM(CASE WHEN score >= 60 THEN 1 ELSE 0 END) AS pass_cnt,
  SUM(CASE WHEN score < 60 THEN 1 ELSE 0 END) AS fail_cnt
FROM student;

-- A18 统计：按年龄分组（示例：每个年龄段人数/平均分）
SELECT
  age,
  COUNT(*) AS headcount,
  ROUND(AVG(score), 2) AS avg_score
FROM student
GROUP BY age
ORDER BY age;


-- ============================================================
-- B. Employee CRUD（employee）
-- 字段：id, name, base_salary, dept_id, created_at, updated_at
-- ============================================================

-- B1 新增员工（单行）
INSERT INTO employee (name, base_salary, dept_id) VALUES ('erin', 6500.00, 2);

-- B2 新增员工（多行）
INSERT INTO employee (name, base_salary, dept_id) VALUES
  ('frank', 5200.00, 1),
  ('grace', 8800.00, 3),
  ('heidi', 5900.00, 1);

-- B3 查询：按 id 精确查
SELECT * FROM employee WHERE id = 1;

-- B4 查询：按 name 模糊查
SELECT id, name, base_salary, dept_id
FROM employee
WHERE name LIKE '%e%'
ORDER BY id;

-- B5 列表：按部门 + 工资排序（列表筛选）
SELECT id, name, base_salary, dept_id
FROM employee
ORDER BY dept_id ASC, base_salary DESC, id ASC;

-- B6 查询：按部门过滤（dept_id）
SELECT id, name, base_salary
FROM employee
WHERE dept_id = 2
ORDER BY base_salary DESC;

-- B7 查询：工资区间
SELECT id, name, base_salary, dept_id
FROM employee
WHERE base_salary BETWEEN 5000 AND 8000
ORDER BY base_salary DESC;

-- B8 分页：第 1 页（pageSize=10）
SELECT id, name, base_salary, dept_id
FROM employee
ORDER BY id
LIMIT 0, 10;

-- B9 修改：按 id 改部门
UPDATE employee
SET dept_id = 3
WHERE id = 1;

-- B10 修改：按部门批量加薪（固定值）
UPDATE employee
SET base_salary = base_salary + 300.00
WHERE dept_id = 1;

-- B11 修改：按部门批量调薪（比例 + ROUND）
UPDATE employee
SET base_salary = ROUND(base_salary * 1.03, 2)
WHERE dept_id = 2;

-- B12 删除：按 id 删除
DELETE FROM employee WHERE id = 2;

-- B13 删除：按条件删除（示例：dept_id=0 的脏数据）
DELETE FROM employee WHERE dept_id = 0;

-- B14 统计：各部门人数
SELECT dept_id, COUNT(*) AS headcount
FROM employee
GROUP BY dept_id
ORDER BY dept_id;

-- B15 统计：各部门平均工资 + 总工资
SELECT
  dept_id,
  COUNT(*) AS headcount,
  ROUND(AVG(base_salary), 2) AS avg_salary,
  SUM(base_salary) AS total_pay
FROM employee
GROUP BY dept_id
ORDER BY dept_id;

-- B16 HAVING：只看人数 >= 2 的部门
SELECT dept_id, COUNT(*) AS headcount
FROM employee
GROUP BY dept_id
HAVING COUNT(*) >= 2
ORDER BY dept_id;

-- B17 HAVING：只看平均工资 > 6000 的部门
SELECT dept_id, ROUND(AVG(base_salary), 2) AS avg_salary
FROM employee
GROUP BY dept_id
HAVING AVG(base_salary) > 6000
ORDER BY avg_salary DESC;

