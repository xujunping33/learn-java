-- Day54：查询（WHERE / ORDER BY / LIMIT / 聚合 / 分组）
-- 建议先执行 day53.sql 插入演示数据，再跑本文件（结果更直观）。
-- 不执行 day53 时，下列 SQL 仍合法，只是行数可能为 0。

USE learn_java;

-- ========== 1) WHERE + 模糊查询（LIKE）==========
-- Q1 姓名包含 stu（与 day53 演示数据匹配）
SELECT id, name, score, age
FROM student
WHERE name LIKE '%stu%'
ORDER BY id;

-- Q2 姓名以 d53_stu_ 开头
SELECT id, name, score
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY score DESC;

-- Q3 员工姓名包含 emp
SELECT id, name, base_salary, dept_id
FROM employee
WHERE name LIKE '%emp%'
ORDER BY dept_id, id;

-- Q4 右模糊（前缀固定）：d53_emp_1% 匹配 emp_10~19 等
SELECT id, name
FROM employee
WHERE name LIKE 'd53_emp_1%'
ORDER BY name;

-- ========== 2) 分数/工资区间 + 排序 ==========
-- Q5 分数区间 [60, 90]，按分数升序、同分按 id
SELECT id, name, score
FROM student
WHERE score BETWEEN 60 AND 90
ORDER BY score ASC, id ASC;

-- Q6 高分榜（>=85）
SELECT name, score
FROM student
WHERE score >= 85
ORDER BY score DESC, id ASC;

-- Q7 底薪区间（示例）
SELECT name, base_salary, dept_id
FROM employee
WHERE base_salary BETWEEN 5000 AND 7000
ORDER BY base_salary DESC;

-- ========== 3) 分页（LIMIT offset, size）==========
-- 约定：每页 5 条，按 id 稳定排序（分页必备稳定 ORDER BY）
-- offset：跳过多少行；size：本页取多少行
-- 第 1 页：跳过 0 行
SELECT id, name, score
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 0, 5;

-- 第 2 页：跳过 5 行
SELECT id, name, score
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 5, 5;

-- 全表分页示例（无前缀条件时，注意总行数大时 OFFSET 深分页性能问题——进阶再学）
SELECT id, name, score
FROM student
ORDER BY id
LIMIT 10, 10;

-- ========== 4) 聚合：统计不及格人数等 ==========
-- Q8 不及格人数（score < 60）
SELECT COUNT(*) AS fail_cnt
FROM student
WHERE score < 60;

-- Q9 及格人数
SELECT COUNT(*) AS pass_cnt
FROM student
WHERE score >= 60;

-- Q10 全体平均分、最高分、最低分
SELECT
  ROUND(AVG(score), 2) AS avg_score,
  MAX(score) AS max_score,
  MIN(score) AS min_score
FROM student
WHERE name LIKE 'd53_stu_%';

-- Q11 员工总人数
SELECT COUNT(*) AS emp_total FROM employee WHERE name LIKE 'd53_emp_%';

-- ========== 5) GROUP BY / HAVING（按部门统计）==========
-- Q12 各部门人数
SELECT dept_id, COUNT(*) AS headcount
FROM employee
WHERE name LIKE 'd53_emp_%'
GROUP BY dept_id
ORDER BY dept_id;

-- Q13 各部门平均工资（保留两位）
SELECT
  dept_id,
  COUNT(*) AS headcount,
  ROUND(AVG(base_salary), 2) AS avg_salary
FROM employee
WHERE name LIKE 'd53_emp_%'
GROUP BY dept_id
ORDER BY dept_id;

-- Q14 各部门底薪总和
SELECT dept_id, SUM(base_salary) AS total_pay
FROM employee
WHERE name LIKE 'd53_emp_%'
GROUP BY dept_id;

-- Q15 HAVING：只显示“人数 >= 5”的部门
SELECT dept_id, COUNT(*) AS headcount
FROM employee
WHERE name LIKE 'd53_emp_%'
GROUP BY dept_id
HAVING COUNT(*) >= 5
ORDER BY dept_id;

-- Q16 HAVING：平均工资超过某阈值（示例 6000）
SELECT dept_id, ROUND(AVG(base_salary), 2) AS avg_salary
FROM employee
WHERE name LIKE 'd53_emp_%'
GROUP BY dept_id
HAVING AVG(base_salary) > 6000
ORDER BY avg_salary DESC;
