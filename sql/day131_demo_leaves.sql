-- =============================================================================
-- W19 Day131（可选）：为演示账号各插一条请假单，便于 curl 看到非空列表
-- =============================================================================
-- 依赖：已执行 oa_schema + oa_seed。在 oa_demo 库执行：
--   mysql -u ... oa_demo < sql/day131_demo_leaves.sql
-- 可重复执行：先删再插（仅演示事由前缀）
-- =============================================================================

SET NAMES utf8mb4;

DELETE FROM leave_actions
WHERE leave_request_id IN (SELECT id FROM leave_requests WHERE reason LIKE 'Day131 演示%');

DELETE FROM leave_requests WHERE reason LIKE 'Day131 演示%';

INSERT INTO leave_requests (
  applicant_user_id, dept_id, leave_type, start_at, end_at, reason, status,
  current_assignee_user_id, version
)
SELECT u.id, e.dept_id, 'ANNUAL', '2026-05-10 09:00:00.000', '2026-05-11 18:00:00.000',
       'Day131 演示-员工', 'SUBMITTED', um.id, 0
FROM users u
JOIN employees e ON e.user_id = u.id
LEFT JOIN employees mgr ON mgr.id = e.manager_employee_id
LEFT JOIN users um ON um.id = mgr.user_id
WHERE u.username = 'emp'
LIMIT 1;

INSERT INTO leave_requests (
  applicant_user_id, dept_id, leave_type, start_at, end_at, reason, status,
  current_assignee_user_id, version
)
SELECT u.id, e.dept_id, 'SICK', '2026-05-12 08:00:00.000', '2026-05-12 12:00:00.000',
       'Day131 演示-经理', 'SUBMITTED', NULL, 0
FROM users u
JOIN employees e ON e.user_id = u.id
WHERE u.username = 'mgr'
LIMIT 1;
