-- Week9 Schema Upgrade（Day57 起步：把 dept_id 规范化）
--
-- 目标：
-- - 新增 department 表
-- - 给 employee.dept_id 清洗成“真实部门 id”
--
-- 注意：
-- - 本脚本不会 DROP employee/student（避免误清空 Week8 数据）
-- - Day58 才会加外键约束；Day57 先把“数据结构”铺好 + 数据清洗

USE learn_java;

-- 1) 新增 department 表
CREATE TABLE IF NOT EXISTS department (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_department_name (name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- 2) 插入基础部门数据（幂等：重复执行不会重复插入）
INSERT INTO department (name) VALUES
  ('研发'),
  ('测试'),
  ('产品'),
  ('人事')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 3) 确保存在一个“未分配/默认部门”（用于把 dept_id=0 的脏数据落地）
INSERT INTO department (name) VALUES ('未分配')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4) 把 employee.dept_id=0 或 NULL 清洗为“未分配”
UPDATE employee
SET dept_id = (SELECT id FROM department WHERE name = '未分配')
WHERE dept_id IS NULL OR dept_id = 0;

-- 5)（可选清洗）把 employee.dept_id 指向不存在的部门也修正为“未分配”
-- 说明：目前还没加外键，历史脏数据可能存在。
UPDATE employee e
LEFT JOIN department d ON e.dept_id = d.id
SET e.dept_id = (SELECT id FROM department WHERE name = '未分配')
WHERE d.id IS NULL;

-- 6) 验收：看 department 列表 & employee 按部门统计（应不再出现 dept_id=0）
SELECT * FROM department ORDER BY id;

SELECT dept_id, COUNT(*) AS headcount
FROM employee
GROUP BY dept_id
ORDER BY dept_id;

-- ============================================================
-- Day58：外键与约束（让错误数据进不来）
-- ============================================================
-- 说明：
-- - MySQL 的 ADD CONSTRAINT 不支持 IF NOT EXISTS，这里用 information_schema 做幂等判断
-- - ON UPDATE CASCADE：如果部门主键（极少发生）被改动，员工表跟随更新
-- - ON DELETE RESTRICT：仍有员工引用该部门时，不允许删除部门

SET @existing_fk_name := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = DATABASE()
    AND table_name = 'employee'
    AND column_name = 'dept_id'
    AND referenced_table_name = 'department'
  LIMIT 1
);

SET @add_fk_sql := IF(
  @existing_fk_name IS NULL,
  'ALTER TABLE employee ADD CONSTRAINT fk_employee_dept_id FOREIGN KEY (dept_id) REFERENCES department(id) ON UPDATE CASCADE ON DELETE RESTRICT',
  'SELECT ''fk_employee_dept_id already exists'' AS msg'
);

PREPARE stmt_add_fk FROM @add_fk_sql;
EXECUTE stmt_add_fk;
DEALLOCATE PREPARE stmt_add_fk;


