-- Day58：外键与约束（让“错误数据”进不来）
--
-- 一键执行：
-- - 复用 day57 的结构升级 + 数据清洗
-- - 添加外键约束 employee(dept_id) -> department(id)
--
-- 注意：
-- - 下面“验证失败”的 SQL 默认是注释状态；你需要一条条取消注释单独执行来观察报错信息

SOURCE /home/xjp/xjp/code/learn/java/sql/week9_schema_upgrade.sql;

-- ======================
-- 验证：5 条会失败的 SQL
-- ======================
-- 0) 先确认一个“不存在的部门 id”
-- SELECT MAX(id) + 999 AS non_exist_dept_id FROM department;

-- 1) 插入员工时使用不存在的 dept_id（应失败）
-- INSERT INTO employee (name, base_salary, dept_id) VALUES ('bad_fk_1', 5000.00, 999999);

-- 2) 更新员工 dept_id 为不存在的值（应失败）
-- UPDATE employee SET dept_id = 999999 WHERE id = 1;

-- 3) 删除一个被引用的 department（应失败：RESTRICT）
-- -- 先找一个“确实有人引用”的部门
-- SELECT dept_id, COUNT(*) FROM employee GROUP BY dept_id HAVING COUNT(*) > 0 ORDER BY dept_id LIMIT 1;
-- -- 假设查出来 dept_id=1，再执行：
-- DELETE FROM department WHERE id = 1;

-- 4) 把 department.id 改掉（如果该部门被引用，CASCADE 会联动；你可以观察结果）
-- -- 强烈建议在练习前先备份，或在测试库里做
-- UPDATE department SET id = id + 1000 WHERE name = '测试';

-- 5) 如果你把 employee.dept_id 改为 NULL（列是 NOT NULL），应失败
-- UPDATE employee SET dept_id = NULL WHERE id = 1;

