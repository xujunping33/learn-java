-- =============================================================================
-- W19 Day128：OA 请假系统 · 建表脚本（MySQL 8+，InnoDB，utf8mb4）
-- =============================================================================
-- 用法（示例）：
--   若本机 root 为 auth_socket（Ubuntu 常见 ERROR 1698），用：
--     sudo mysql -e "CREATE DATABASE IF NOT EXISTS oa_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
--     sudo mysql oa_demo < sql/oa_schema.sql
--   若 root 已设密码登录：
--     mysql -u root -p oa_demo < sql/oa_schema.sql
--
-- 可重复执行：先按外键依赖逆序 DROP 再 CREATE（开发环境；生产勿盲跑 DROP）。
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS leave_actions;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS departments;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- 部门
-- ---------------------------------------------------------------------------
CREATE TABLE departments (
  id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  name          VARCHAR(100)    NOT NULL COMMENT '部门名称',
  code          VARCHAR(50)     NULL COMMENT '部门编码（可选）',
  created_at    DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_departments_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门';

-- ---------------------------------------------------------------------------
-- 用户（登录主体；密码存 hash+salt，见 Day129 seed）
-- ---------------------------------------------------------------------------
CREATE TABLE users (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  username        VARCHAR(64)     NOT NULL COMMENT '登录名',
  password_hash   VARCHAR(128)    NOT NULL COMMENT 'MD5(密码+salt) 等',
  salt            VARCHAR(64)     NOT NULL COMMENT '盐',
  display_name    VARCHAR(100)    NOT NULL COMMENT '展示姓名',
  status          TINYINT         NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
  created_at      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  KEY idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- ---------------------------------------------------------------------------
-- 角色 / 权限（RBAC）
-- ---------------------------------------------------------------------------
CREATE TABLE roles (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  code        VARCHAR(50)     NOT NULL COMMENT '角色编码：EMPLOYEE/MANAGER/ADMIN',
  name        VARCHAR(100)    NOT NULL COMMENT '角色名称',
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色';

CREATE TABLE permissions (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  code        VARCHAR(100)    NOT NULL COMMENT '权限点编码，如 leave:submit',
  description VARCHAR(200)    NULL COMMENT '说明',
  PRIMARY KEY (id),
  UNIQUE KEY uk_permissions_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限';

CREATE TABLE user_roles (
  user_id BIGINT UNSIGNED NOT NULL COMMENT '用户',
  role_id BIGINT UNSIGNED NOT NULL COMMENT '角色',
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色';

CREATE TABLE role_permissions (
  role_id       BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限';

-- ---------------------------------------------------------------------------
-- 员工（挂部门；经理审批「本部门」时可按 dept_id 过滤）
-- ---------------------------------------------------------------------------
CREATE TABLE employees (
  id                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id              BIGINT UNSIGNED NOT NULL COMMENT '对应 users.id',
  dept_id              BIGINT UNSIGNED NOT NULL COMMENT '部门',
  job_title            VARCHAR(100)    NULL COMMENT '岗位',
  manager_employee_id  BIGINT UNSIGNED NULL COMMENT '直属上级员工 id（可选）',
  created_at           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_employees_user (user_id),
  KEY idx_employees_dept (dept_id),
  KEY idx_employees_manager (manager_employee_id),
  CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_employees_dept FOREIGN KEY (dept_id) REFERENCES departments (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_employees_mgr FOREIGN KEY (manager_employee_id) REFERENCES employees (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工';

-- ---------------------------------------------------------------------------
-- 请假单（业务主表）
-- applicant_user_id：申请人（与 users 对齐，查询简单；亦可与 employees 联查部门）
-- current_assignee_user_id：当前待谁处理（首版可填经理 user_id，占位）
-- ---------------------------------------------------------------------------
CREATE TABLE leave_requests (
  id                         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  applicant_user_id          BIGINT UNSIGNED NOT NULL COMMENT '申请人 users.id',
  dept_id                    BIGINT UNSIGNED NOT NULL COMMENT '所属部门（冗余，便于列表/待审）',
  leave_type                 VARCHAR(32)     NOT NULL COMMENT '类型：ANNUAL/SICK/OTHER',
  start_at                   DATETIME(3)     NOT NULL COMMENT '开始时间',
  end_at                     DATETIME(3)     NOT NULL COMMENT '结束时间',
  reason                     VARCHAR(500)    NOT NULL COMMENT '事由',
  status                     VARCHAR(32)     NOT NULL COMMENT 'DRAFT/SUBMITTED/APPROVED/REJECTED/CANCELLED',
  current_assignee_user_id BIGINT UNSIGNED NULL COMMENT '当前处理人 users.id（可空）',
  version                    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT '乐观锁预留（Day132）',
  created_at                 DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  updated_at                 DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  KEY idx_leave_applicant_status (applicant_user_id, status),
  KEY idx_leave_dept_status (dept_id, status),
  KEY idx_leave_assignee (current_assignee_user_id, status),
  CONSTRAINT fk_leave_applicant FOREIGN KEY (applicant_user_id) REFERENCES users (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_leave_dept FOREIGN KEY (dept_id) REFERENCES departments (id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_leave_assignee FOREIGN KEY (current_assignee_user_id) REFERENCES users (id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假单';

-- ---------------------------------------------------------------------------
-- 审批 / 审计流水：谁在何时做了什么、备注（状态每次变更应落一条）
-- ---------------------------------------------------------------------------
CREATE TABLE leave_actions (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  leave_request_id BIGINT UNSIGNED NOT NULL COMMENT '请假单',
  actor_user_id    BIGINT UNSIGNED NOT NULL COMMENT '操作人 users.id',
  action           VARCHAR(32)     NOT NULL COMMENT 'SUBMIT/APPROVE/REJECT/CANCEL/COMMENT 等',
  remark           VARCHAR(500)    NULL COMMENT '备注/驳回原因',
  created_at       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_actions_leave_time (leave_request_id, created_at),
  KEY idx_actions_actor (actor_user_id, created_at),
  CONSTRAINT fk_actions_leave FOREIGN KEY (leave_request_id) REFERENCES leave_requests (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_actions_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='请假审批与审计流水';
