-- =============================================================================
-- W20 Day136：动态 SQL 模板（MySQL 8+ / oa_demo）
-- =============================================================================
-- 本文件为「可讲清」的查询骨架：条件用占位符或注释标出；应用层须 **PreparedStatement**
-- 绑定变量，**禁止**把用户输入直接拼进 SQL 字符串。
-- 数据范围要点：**员工**强制 **`applicant_user_id = 当前用户`**；**经理**强制 **`dept_id = 本人部门`**
--（否则能扫到全公司单据，属于越权）。
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 1) 本人请假列表 + 可选状态 / 类型（与 GET /api/leaves/me 一致）
-- ---------------------------------------------------------------------------
-- 固定范围：WHERE applicant_user_id = ?
-- 动态：AND status = ?（仅当筛选「已提交/已通过」等白名单值时拼接）
--        AND leave_type = ?（ANNUAL / SICK / OTHER）
-- 为什么「本人」必须带 applicant_user_id：RBAC 只解决接口能不能进库；行级隔离靠 WHERE。
/*
SELECT id, applicant_user_id, dept_id, leave_type, start_at, end_at, reason, status,
       current_assignee_user_id, version, created_at, updated_at
FROM leave_requests
WHERE applicant_user_id = ?
  /* AND status = ? */
  /* AND leave_type = ? */
ORDER BY created_at DESC;
*/

-- ---------------------------------------------------------------------------
-- 2) 经理：本部门请假单 + 默认「待审」+ 可选状态 / 类型（与 GET /api/leaves/pending 一致）
-- ---------------------------------------------------------------------------
-- 固定范围：dept_id 必须等于 (SELECT dept_id FROM employees WHERE user_id = 经理?)
--           默认再限制 status = 'SUBMITTED'；若传参则改为绑定 ?。
-- 为什么必须带 dept：经理只能看本部门数据；去掉 dept 条件即「全表」，与数据范围设计冲突。
/*
SELECT lr.id, lr.applicant_user_id, lr.dept_id, lr.leave_type, lr.start_at, lr.end_at, lr.reason, lr.status,
       lr.current_assignee_user_id, lr.version, lr.created_at, lr.updated_at
FROM leave_requests lr
WHERE lr.dept_id = (SELECT e.dept_id FROM employees e WHERE e.user_id = ? LIMIT 1)
  AND lr.status = 'SUBMITTED'   -- 或改为 AND lr.status = ? 由参数决定
  /* AND lr.leave_type = ? */
ORDER BY lr.created_at DESC;
*/

-- ---------------------------------------------------------------------------
-- 3) 动态时间窗：某部门在时间范围内的请假（用于报表 / 导出雏形）
-- ---------------------------------------------------------------------------
-- 动态：start_at / end_at 上下界是否拼接由业务决定；占位用 ?。
/*
SELECT id, applicant_user_id, status, start_at, end_at, reason
FROM leave_requests
WHERE dept_id = ?
  AND start_at >= ?
  AND end_at <= ?
ORDER BY start_at;
*/

-- ---------------------------------------------------------------------------
-- 4) RBAC：判断某 user 是否拥有某 permission.code（与 Java Permissions 一致）
-- ---------------------------------------------------------------------------
-- EXISTS 子查询避免重复行；适合登录后缓存或网关鉴权扩展。
/*
SELECT EXISTS (
  SELECT 1
  FROM user_roles ur
  JOIN role_permissions rp ON rp.role_id = ur.role_id
  JOIN permissions p ON p.id = rp.permission_id
  WHERE ur.user_id = ? AND p.code = ?
) AS has_perm;
*/

-- ---------------------------------------------------------------------------
-- 5) 审计流水：按请假单 + 可选动作类型筛选（排障 / 合规）
-- ---------------------------------------------------------------------------
/*
SELECT id, leave_request_id, actor_user_id, action, remark, created_at
FROM leave_actions
WHERE leave_request_id = ?
  /* AND action = ? */   -- 如 APPROVE / REJECT / SUBMIT
ORDER BY created_at ASC;
*/

-- ---------------------------------------------------------------------------
-- 6) 分页列表（LIMIT / OFFSET 由应用层绑定整数，禁止字符串拼接用户输入）
-- ---------------------------------------------------------------------------
/*
SELECT id, applicant_user_id, status, created_at
FROM leave_requests
WHERE applicant_user_id = ?
ORDER BY created_at DESC
LIMIT ? OFFSET ?;
*/
