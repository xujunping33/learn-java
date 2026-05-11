-- Day52：DDL（修改表结构 + 索引入门 + EXPLAIN）

USE learn_java;

-- 1) ALTER TABLE：给 student 增加 age（任选其一：age 或 class_name）
ALTER TABLE student
  ADD COLUMN age TINYINT NOT NULL DEFAULT 0 AFTER score;

-- 2) 建索引（示例：student(name) 已是 UNIQUE，自带索引；这里再给 score 建普通索引）
CREATE INDEX idx_student_score ON student(score);

-- employee(dept_id) 在 Day51 已建 idx_employee_dept_id；这里演示再建 name 索引
CREATE INDEX idx_employee_name ON employee(name);

-- 3) EXPLAIN（至少 5 条）：观察 key/rows 等字段
EXPLAIN SELECT * FROM student WHERE id = 1;
EXPLAIN SELECT * FROM student WHERE name = 'Alice';
EXPLAIN SELECT * FROM student WHERE score >= 60 ORDER BY score DESC LIMIT 0, 10;
EXPLAIN SELECT * FROM employee WHERE dept_id = 1;
EXPLAIN SELECT * FROM employee WHERE name LIKE 'A%';

-- 可选：查看索引
SHOW INDEX FROM student;
SHOW INDEX FROM employee;

