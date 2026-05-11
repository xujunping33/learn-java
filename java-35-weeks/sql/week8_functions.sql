-- Week8 Functions（Day55：数值 / 日期时间 / 字符串）
-- 说明：本脚本以“练习函数”为目标，主要用 SELECT 展示结果。
-- 建议先执行 day53.sql（插入 d53_* 演示数据），再跑本文件更直观。

USE learn_java;

-- ========== 数值函数：ROUND / CEIL / FLOOR ==========
-- F1：round(保留 2 位)
SELECT 1.23456 AS x, ROUND(1.23456, 2) AS round_2;

-- F2：ceil / floor
SELECT 1.01 AS x, CEIL(1.01) AS ceil_x, FLOOR(1.01) AS floor_x;

-- F3：负数的 ceil / floor（观察差异）
SELECT -1.01 AS x, CEIL(-1.01) AS ceil_x, FLOOR(-1.01) AS floor_x;

-- F4：把部门 2 的工资按 3% 上调后展示（不写回，只做计算展示）
SELECT
  id,
  name,
  base_salary,
  ROUND(base_salary * 1.03, 2) AS salary_after_raise
FROM employee
WHERE name LIKE 'd53_emp_%' AND dept_id = 2
ORDER BY id;

-- ========== 日期时间函数：NOW / DATE_FORMAT / DATEDIFF ==========
-- F5：当前时间
SELECT NOW() AS now_time;

-- F6：格式化当前时间（常见报表展示）
SELECT DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s') AS now_fmt;

-- F7：学生数据创建日期（只看日期部分）
SELECT
  id,
  name,
  created_at,
  DATE_FORMAT(created_at, '%Y-%m-%d') AS created_date
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 10;

-- F8：距离今天多少天（created_at → today）
SELECT
  id,
  name,
  created_at,
  DATEDIFF(NOW(), created_at) AS days_since_created
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id;

-- F9：演示 DATEDIFF 的“两个具体日期”
SELECT DATEDIFF('2026-12-31', '2026-01-01') AS days_between;

-- ========== 字符串函数：CONCAT / LENGTH / SUBSTRING ==========
-- F10：拼接字符串（生成可读标签）
SELECT CONCAT('student#', id, ':', name) AS label
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 10;

-- F11：字符长度（UTF8MB4 下中英文长度差异可自行插入数据观察）
SELECT
  name,
  LENGTH(name) AS bytes_len
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 5;

-- F12：截取子串：取前 7 个字符（d53_stu 前缀）
SELECT
  name,
  SUBSTRING(name, 1, 7) AS prefix_7
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 10;

-- F13：截取子串：取最后 2 个字符（01/02/...）
SELECT
  name,
  SUBSTRING(name, LENGTH(name) - 1, 2) AS suffix_2
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY id
LIMIT 10;

-- F14：组合：把员工名变成“部门-姓名”的展示字段
SELECT
  dept_id,
  name,
  CONCAT('dept=', dept_id, ', name=', name) AS show_text
FROM employee
WHERE name LIKE 'd53_emp_%'
ORDER BY dept_id, id
LIMIT 10;

-- F15：用 SUBSTRING 把 'd53_emp_10' → '10'（用于演示字符串解析）
SELECT
  name,
  SUBSTRING(name, 9, 2) AS emp_no_2chars
FROM employee
WHERE name LIKE 'd53_emp_1%'
ORDER BY id;

-- F16：综合练习：把学生“编号后两位”转成数值比较（只做展示，不写回）
-- 说明：SUBSTRING 得到的是字符串；这里用 CAST 转成无符号数用于排序演示
SELECT
  id,
  name,
  CAST(SUBSTRING(name, LENGTH(name) - 1, 2) AS UNSIGNED) AS stu_no
FROM student
WHERE name LIKE 'd53_stu_%'
ORDER BY stu_no ASC;

