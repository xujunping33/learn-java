# 第 19 周笔记（OA 请假 · 需求 → 库表 → Servlet 骨架 → RBAC → 流程）

## Day 127：需求分析（方案 A）

### 技术选型（已定）

- **方案 A**：**`oa-demo`** = **Maven Web（Servlet + JDBC / MyBatis）**，与 **`servlet-demo`** 能力连续；**Spring Boot 本轮不接**，后续单开迭代即可。

### 交付

- **`oa-demo/docs/requirements.md`**：角色（**员工 / 部门经理 / 系统管理员** 最小集）、用例表、请假状态 **文字状态机**（首版 **`SUBMITTED` → `APPROVED` / `REJECTED`**，**`DRAFT` / `CANCELLED`** 预留）、接口级权限矩阵雏形。
- **`oa-demo/README.md`**：写明方案 A 与文档入口。

### 口述验收（约 60s）

1. 员工提交 → 单进入 **已提交**。  
2. 经理审批 → **通过**则结束；**驳回**则结束并留痕（后续表 **`leave_actions`**）。  
3. 管理员管人、角色、部门，不抢业务审批主路径。

## Day 128：数据库设计（RBAC + 请假 + 审计）

### 脚本

- **`sql/oa_schema.sql`**（MySQL 8+）：开发环境可重复执行 — 先 **`SET FOREIGN_KEY_CHECKS=0`** 再 **`DROP TABLE IF EXISTS`**（逆依赖顺序），再 **`CREATE`**。
- **RBAC**：**`users`**、**`roles`**、**`permissions`**、**`user_roles`**、**`role_permissions`**。
- **组织**：**`departments`**、**`employees`**（**`user_id`**、**`dept_id`**、可选 **`manager_employee_id`**）。
- **业务**：**`leave_requests`** — 申请人 **`applicant_user_id`**、**`dept_id`**、**`leave_type`**、**`start_at`/`end_at`**、**`reason`**、**`status`**、**`current_assignee_user_id`**（当前处理人）、**`version`**（乐观锁占位）；**`leave_actions`** — **`leave_request_id`**、**`actor_user_id`**、**`action`**、**`remark`**、**`created_at`**。
- **索引**：按「本人列表 **`(applicant_user_id, status)`**」「本部门待审 **`(dept_id, status)`**」「审计按单时间线 **`(leave_request_id, created_at)`**」建辅助索引。

### 口述验收：为什么要有 **`leave_actions`**？

- **主表 `leave_requests`** 只保留**当前状态**与少量冗余字段；**每一次**提交/通过/驳回/取消都应留下**不可抵赖**的流水：**谁、何时、做了什么、备注**。便于排障、合规审计、恢复「当时发生了什么」，而不是只看最终状态。

## Day 129：种子数据（角色权限 + 演示账号）

### 脚本

- **`sql/oa_seed.sql`**：**`departments`**（研发部 `RD`、管理部 `HQ`）、**`roles`** / **`permissions`** / **`role_permissions`**（员工提交与查本人；经理待审/通过/驳回；管理员含 **`admin:access`** 及全部业务权限）、演示用户 **`emp` / `mgr` / `admin`**（**MD5(密码明文+salt)** 落库）、**`user_roles`**、**`employees`**（`emp` 的 **`manager_employee_id`** 指向 `mgr` 的员工行）。
- **幂等**：部门/角色/权限/用户按唯一键 **UPSERT**；演示账号的 **`user_roles`**、**`employees`** 先 **DELETE** 再 **INSERT**；**`role_permissions`** 使用 **`INSERT IGNORE`**。

### 默认口令

- 见 **`oa-demo/README.md`** 表格（`emp123` / `mgr123` / `admin123`）。

### 口述验收

- 重复执行 **`oa_seed.sql`** 后，角色不重复、三个账号仍可登录校验（与 Day130 登录实现对接时验证 hash 算法一致即可）。

## Day 130：工程骨架（统一 JSON、异常、日志）

### 交付

- **`oa-demo/`**：Maven **`war`**（JDK 21、Tomcat 10+）；**`WEB-INF/web.xml`** 注册过滤器与 **`HealthServlet`** / **`LoginServlet`**。
- **统一 JSON**：**`{ code, message, data }`**；成功 **`code=0`**；**`ApiException`** 携带 HTTP 状态与业务 **`code`**（如登录失败 **401** / **40100**）。
- **日志**：**`RequestLoggingFilter`** 打 method+URI；**`LoginServlet`** 在成功/失败（不含密码）打 **INFO**。
- **`POST /api/login`**：读 **`db.properties`**，校验 **`users`** 表密码（**`learn.java.oa.security.Passwords`** 与 seed 算法一致）。

### 口述验收

- 用 **curl** 调 **`GET .../api/health`** 与 **`POST .../api/login`**；说明异常如何从 Servlet 冒泡到 **`BaseJsonServlet#service`** 再变成统一 JSON。

## Day 131：鉴权 + 本人请假列表

### 交付

- **`AuthFilter`**（在 **`EncodingFilter` → `RequestLoggingFilter`** 之后）：**`/api/health`**、**`/api/login`** 白名单；其余 **`/api/*`** 须 **Session** 含 **`userId`**；**`/api/admin/*`** 须 Session 中 **`roleCodes`** 含 **`ADMIN`**。
- **`LoginServlet`**：校验成功后 **`invalidate`** 旧 Session、建新 Session，写入 **`SessionKeys`**（**`userId` / `username` / `displayName` / `roleCodes`**）；响应 **`data.roles`** 便于自测。
- **`GET /api/leaves/me`**：**`LeavesMeServlet`** 按 **`applicant_user_id = 当前 userId`** 查 **`leave_requests`**，统一 JSON **`data.items`**。
- **`GET /api/admin/ping`**：**`AdminPingServlet`**，验证管理员接口拦截。
- 可选 SQL：**`sql/day131_demo_leaves.sql`**（为 **`emp` / `mgr`** 各插一条演示请假，可重复执行）。

### 口述验收

- 同一 **`curl -b cookies`**：先 **`emp`** 登录看 **`/api/leaves/me`**，再 **`mgr`** 登录看 **`/api/leaves/me`**，列表互不串数据；**`emp`** 调 **`/api/admin/ping`** 得 **403**。

## Day 132：提交请假 + 审批（事务 + 审计）

### 交付

- **`POST /api/leaves`**（**`LeaveSubmitServlet`**）：校验 **`leave:submit`**；从 **`employees` + 直属上级** 解析 **`dept_id`**、**`current_assignee_user_id`**（经理 **`users.id`**）；插入 **`leave_requests`**（**`SUBMITTED`**）与 **`leave_actions`**（**`SUBMIT`**）**同一连接 `commit`/`rollback`**。
- **`POST /api/leaves/{id}/approve|reject`**（**`LeaveDecisionServlet`**）：**`SELECT ... FOR UPDATE`**；校验 **`leave:approve` / `leave:reject`**、状态 **`SUBMITTED`**、操作人即 **`current_assignee_user_id`**；**`UPDATE ... AND version = ?`** 防重复审批；写 **`APPROVE` / `REJECT`** 流水；类注释 **TODO** 并发与前端 **`version`** 传递。
- **`Permissions`**、**`HttpSessions.requireUserId`**；**`Db.getConnection()`** 改为 **`throws SQLException`** 便于 Servlet 编译与错误归类。

### 口述验收

- **`emp`** 提交 → **`mgr`** 通过或驳回 → **`leave_actions`** 与主表状态一致；对已 **`APPROVED`** 再点通过 → **409** 明确提示。

## Day 133：周整合（端到端 + SQL 复盘）

### 交付

- **`day133/README.md`**：从零导入顺序、**SQL 表职责复盘**、口述要点（RBAC / 状态 / 审计）。  
- **`day133/smoke-curl.sh`**：一键 **curl** 串联（**`health` → 登录 → 提交 → 经理通过 → 员工列表**），依赖本机 **`python3`** 解析 **`data.id`**。  
- **`oa-demo/README.md`**：**Day133** 小节 + **curl** 示例改为用 **`SUBMIT_JSON`** / **`python3`** 取 **`LEAVE_ID`**，并说明 **`day131_demo_leaves`** 与「新提交单」审批的差别。

### 口述验收

- 能按清单从 **0 导入 SQL** 到 **Tomcat + curl** 跑通闭环；能口述 **RBAC 拦截点**、**状态落库位置**、**`leave_actions` 与事务边界**。
