-- Day53：DML（INSERT / UPDATE / DELETE）
-- 执行前请先：USE learn_java;
-- 若重复练习，可先删掉本日演示数据（按命名前缀），再从头执行。

USE learn_java;

-- 1) 清理本日演示数据（可重复执行脚本）
DELETE FROM student WHERE name LIKE 'd53_stu_%';
DELETE FROM employee WHERE name LIKE 'd53_emp_%';

-- 2) INSERT：student 20 条（多行插入 + UNIQUE name 不重复）
INSERT INTO student (name, score, age) VALUES
  ('d53_stu_01', 92, 18),
  ('d53_stu_02', 78, 17),
  ('d53_stu_03', 55, 19),
  ('d53_stu_04', 88, 18),
  ('d53_stu_05', 61, 20),
  ('d53_stu_06', 45, 17),
  ('d53_stu_07', 73, 19),
  ('d53_stu_08', 95, 18),
  ('d53_stu_09', 58, 20),
  ('d53_stu_10', 82, 17),
  ('d53_stu_11', 67, 21),
  ('d53_stu_12', 49, 18),
  ('d53_stu_13', 90, 19),
  ('d53_stu_14', 76, 17),
  ('d53_stu_15', 84, 20),
  ('d53_stu_16', 59, 18),
  ('d53_stu_17', 71, 19),
  ('d53_stu_18', 86, 17),
  ('d53_stu_19', 63, 20),
  ('d53_stu_20', 79, 18);

-- 3) INSERT：employee 20 条
INSERT INTO employee (name, base_salary, dept_id) VALUES
  ('d53_emp_01', 5200.00, 1),
  ('d53_emp_02', 6100.50, 2),
  ('d53_emp_03', 4800.00, 1),
  ('d53_emp_04', 7300.00, 3),
  ('d53_emp_05', 5500.00, 2),
  ('d53_emp_06', 8900.00, 3),
  ('d53_emp_07', 4700.00, 1),
  ('d53_emp_08', 6200.00, 2),
  ('d53_emp_09', 5100.00, 1),
  ('d53_emp_10', 7000.00, 3),
  ('d53_emp_11', 5800.00, 2),
  ('d53_emp_12', 4500.00, 1),
  ('d53_emp_13', 7600.00, 3),
  ('d53_emp_14', 5300.00, 2),
  ('d53_emp_15', 6400.00, 2),
  ('d53_emp_16', 8200.00, 3),
  ('d53_emp_17', 4900.00, 1),
  ('d53_emp_18', 6700.00, 2),
  ('d53_emp_19', 5900.00, 1),
  ('d53_emp_20', 9100.00, 3);

-- 4) 验收入库行数
SELECT COUNT(*) AS student_cnt FROM student WHERE name LIKE 'd53_stu_%';
SELECT COUNT(*) AS employee_cnt FROM employee WHERE name LIKE 'd53_emp_%';

-- ========== UPDATE（务必带 WHERE；习惯：先想条件，再写 SET）==========

-- 改成绩：按姓名定位一行
UPDATE student SET score = 95, age = 19 WHERE name = 'd53_stu_01';

-- 改部门：把某人调到部门 3
UPDATE employee SET dept_id = 3 WHERE name = 'd53_emp_05';

-- 批量加薪：部门 1 全员底薪 +300
UPDATE employee SET base_salary = base_salary + 300.00 WHERE dept_id = 1;

-- 批量按比例调薪：部门 2 底薪 * 1.03（示例）
UPDATE employee SET base_salary = ROUND(base_salary * 1.03, 2) WHERE dept_id = 2;

-- ========== DELETE（务必带 WHERE）==========

-- 按主键删除：用变量先取出 id，再删（避免误删多行）
SET @sid = (SELECT id FROM student WHERE name = 'd53_stu_20' LIMIT 1);
DELETE FROM student WHERE id = @sid;

-- 按条件删除：不及格（score < 60）
DELETE FROM student WHERE name LIKE 'd53_stu_%' AND score < 60;

-- 查看本日演示数据剩余情况
SELECT id, name, score, age FROM student WHERE name LIKE 'd53_stu_%' ORDER BY id;
SELECT id, name, base_salary, dept_id FROM employee WHERE name LIKE 'd53_emp_%' ORDER BY id;
