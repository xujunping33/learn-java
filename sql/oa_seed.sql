-- =============================================================================
-- W19 Day129：OA 演示种子数据（MySQL 8+）
-- =============================================================================
-- 依赖：已执行 **`sql/oa_schema.sql`** 建好库表后再导入本文件。
--   sudo mysql oa_demo < sql/oa_seed.sql
--   或：mysql -u root -p oa_demo < sql/oa_seed.sql
--
-- 密码算法（与 Java 校验一致）：**`password_hash = LOWER( HEX( MD5( CONCAT(密码明文, salt) ) ) )`**
--   UTF-8 明文与 salt 拼接后做 MD5，结果为小写十六进制字符串（32 位）。
--   生产环境更推荐 BCrypt；本周按课纲使用 MD5+salt。
--
-- 幂等策略：角色/权限/部门按 **唯一键** `INSERT ... ON DUPLICATE KEY UPDATE`；
-- 演示用户按 **`username`** 更新 hash/salt；**`user_roles`** / **`employees`**
-- 对演示账号先 **DELETE** 再 **INSERT**，避免重复绑定；**`role_permissions`**
-- 用 **`INSERT IGNORE`**（主键 `(role_id, permission_id)` 不重复则跳过）。
-- =============================================================================

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 部门（code 唯一）
-- ---------------------------------------------------------------------------
INSERT INTO departments (name, code) VALUES
  ('研发部', 'RD'),
  ('管理部', 'HQ')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ---------------------------------------------------------------------------
-- 角色
-- ---------------------------------------------------------------------------
INSERT INTO roles (code, name) VALUES
  ('EMPLOYEE', '员工'),
  ('MANAGER', '部门经理'),
  ('ADMIN', '系统管理员')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ---------------------------------------------------------------------------
-- 权限点（与 requirements 矩阵方向一致）
-- ---------------------------------------------------------------------------
INSERT INTO permissions (code, description) VALUES
  ('leave:submit', '提交请假'),
  ('leave:view_own', '查看本人请假'),
  ('leave:pending_dept', '查看本部门待审请假'),
  ('leave:approve', '审批通过'),
  ('leave:reject', '审批驳回'),
  ('admin:access', '管理端 /api/admin/**')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ---------------------------------------------------------------------------
-- 角色-权限（可重复执行：已存在则 IGNORE）
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'leave:submit' WHERE r.code = 'EMPLOYEE';
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'leave:view_own' WHERE r.code = 'EMPLOYEE';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'leave:pending_dept' WHERE r.code = 'MANAGER';
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'leave:approve' WHERE r.code = 'MANAGER';
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p ON p.code = 'leave:reject' WHERE r.code = 'MANAGER';

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN';

-- ---------------------------------------------------------------------------
-- 演示用户（MD5(明文+salt)，见文件头）
--   emp   / emp123
--   mgr   / mgr123
--   admin / admin123
-- ---------------------------------------------------------------------------
INSERT INTO users (username, password_hash, salt, display_name, status) VALUES
  ('emp', '4e81f4d601f02f5815e208d57146eb6a', 'oa_salt_emp_01', '演示员工', 1),
  ('mgr', '43264916d66fd287ae4f9823ebddfe99', 'oa_salt_mgr_01', '演示经理', 1),
  ('admin', 'be1f1776f96ae915877eeff42a570e98', 'oa_salt_adm_01', '演示管理员', 1)
AS seed
ON DUPLICATE KEY UPDATE
  display_name = seed.display_name,
  password_hash = seed.password_hash,
  salt = seed.salt,
  status = seed.status;

-- 绑定角色：先清演示账号的旧绑定，再插入（幂等）
DELETE ur FROM user_roles ur
  INNER JOIN users u ON u.id = ur.user_id
  WHERE u.username IN ('emp', 'mgr', 'admin');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'EMPLOYEE' WHERE u.username = 'emp';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'MANAGER' WHERE u.username = 'mgr';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ADMIN' WHERE u.username = 'admin';

-- ---------------------------------------------------------------------------
-- 员工行（部门 + 上下级）：先删演示用户对应员工再插，避免重复
-- ---------------------------------------------------------------------------
DELETE e FROM employees e
  INNER JOIN users u ON u.id = e.user_id
  WHERE u.username IN ('emp', 'mgr', 'admin');

INSERT INTO employees (user_id, dept_id, job_title, manager_employee_id)
SELECT u.id, d.id, '部门经理', NULL
FROM users u
JOIN departments d ON d.code = 'RD'
WHERE u.username = 'mgr';

INSERT INTO employees (user_id, dept_id, job_title, manager_employee_id)
SELECT u.id, d.id, '系统管理员', NULL
FROM users u
JOIN departments d ON d.code = 'HQ'
WHERE u.username = 'admin';

INSERT INTO employees (user_id, dept_id, job_title, manager_employee_id)
SELECT u.id, d.id, '软件工程师', mgr.id
FROM users u
JOIN departments d ON d.code = 'RD'
JOIN users u_mgr ON u_mgr.username = 'mgr'
JOIN employees mgr ON mgr.user_id = u_mgr.id
WHERE u.username = 'emp';
