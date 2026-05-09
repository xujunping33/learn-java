## 第19周学习计划：OA请假系统实战（需求→库表→后端骨架→RBAC→核心流程）

对应原路径：第19–20周《MyBatis实现OA系统项目实战》中的 **第19周（先把业务与数据跑通）**。  
学习时长：每天约 2 小时。

本周核心目标
- 完成 OA 的 **需求拆解**：角色、用例、状态机（请假单）
- 完成 **数据库设计**：用户/角色/权限、部门员工、请假单、审批记录（审计）
- 搭一个可迭代的后端工程骨架（建议新建 `oa-demo/`，不要继续堆在 `servlet-demo` 里）
- 实现 **RBAC**：基于角色的访问控制（最小可用）
- 实现 **请假核心流程**：提交 → 审批通过/驳回 → 状态变更（最小闭环）
- 密码存储：**MD5 + salt（盐）**（按课程要求；你知道生产更推荐 BCrypt，但本周先按课纲完成）

本周交付物（必须完成）
- `oa-demo/README.md`：如何启动、默认账号、主要 URL
- `sql/oa_schema.sql`：建表脚本（可重复执行/有注释）
- `sql/oa_seed.sql`：初始化角色/权限/管理员账号
- 后端代码（任选其一，但本周必须定下来并保持统一）
  - A方案（更贴近你当前路径）：`oa-demo` = Maven Web（Servlet + JDBC 或 + MyBatis）
  - B方案（更贴近企业现状）：`oa-demo` = Spring Boot + MyBatis（如果你愿意提前引入 Spring）
- `W19-notes.md`

目录建议
- `day127` ~ `day133`

每天固定节奏（2小时）
- 20min：复盘（重跑昨天 SQL + 接口用 curl/浏览器验证）
- 70min：写 SQL/写代码（必须可运行）
- 20min：总结入 `W19-notes.md`
- 10min：口述复盘（用业务语言讲流程，不用堆术语）

---

## Day 127（需求分析：角色、用例、状态机）

学习要点
- 角色：员工、部门经理、HR、系统管理员（先选最小集合：员工 + 经理 + 管理员）
- 用例：登录、提交请假、查看自己的单、审批、列表筛选
- 状态：`DRAFT/SUBMITTED/APPROVED/REJECTED/CANCELLED`（先实现 SUBMITTED/APPROVED/REJECTED）

任务卡（70min）
- 写 `oa-demo/docs/requirements.md`（很短也行）
  - 画出请假状态流转（文字版即可）
  - 列出每个角色能做什么（权限矩阵雏形）

验收标准
- 你能用 60 秒讲清楚：请假单从创建到结束经历哪些状态

---

## Day 128（数据库设计：RBAC + 请假单 + 审批记录）

学习要点
- RBAC：`users`、`roles`、`permissions`、`user_roles`、`role_permissions`
- 业务表：`departments`、`employees`、`leave_requests`、`leave_actions`（审批记录/审计）
- 外键与索引：按查询场景加索引（不要贪多）

任务卡（70min）
- 输出 `sql/oa_schema.sql`
- 至少包含字段
  - `leave_requests`：申请人、类型、起止时间、原因、状态、当前处理人（可先简化）
  - `leave_actions`：谁、在什么时间、做了什么、备注

验收标准
- 你能解释：为什么要有 `leave_actions`（审计/追溯）

---

## Day 129（初始化数据：角色权限 + 管理员 + 演示员工）

学习要点
- 初始化脚本要幂等（至少不重复插入关键角色）
- 密码不要明文落库（MD5+salt）

任务卡（70min）
- 输出 `sql/oa_seed.sql`
- 准备 3 个测试账号（员工/经理/管理员），写进 README

验收标准
- 重新导入 seed 后系统还能启动（不依赖手工改库）

---

## Day 130（工程骨架：统一 JSON、异常、日志）

学习要点
- 统一返回结构：`{code,message,data}`
- 统一异常处理（Servlet 可用 Filter/基类 Servlet；Spring 可用 `@ControllerAdvice`）
- 日志：关键业务点打印（谁审批了什么）

任务卡（70min）
- 创建 `oa-demo` 工程骨架
- 实现 `GET /api/health` 返回 JSON
- 实现登录接口雏形：`POST /api/login`（先返回成功/失败即可）

验收标准
- 你能用 curl 或 fetch 调通 health 与 login（不要求前端页面）

---

## Day 131（RBAC落地：鉴权过滤器/拦截点 + 动态数据范围先简化）

学习要点
- “接口级权限”：访问 `/api/admin/*` 必须管理员
- “数据范围”先简化：员工只能看自己的请假单；经理能看本部门（先可做假数据/全量后再收紧）

任务卡（70min）
- 实现 `AuthFilter`（或等价机制）
- 实现 `GET /api/leaves/me`：只返回当前登录用户的请假单

验收标准
- 用两个账号分别访问，数据隔离正确（最小版本）

---

## Day 132（核心流程：提交请假 + 审批通过/驳回）

学习要点
- 事务：提交与写审计记录同事务
- 并发：同一请假单不能重复审批（乐观锁可先不做，但要写 TODO）

任务卡（70min）
- `POST /api/leaves`：提交请假（状态 SUBMITTED）
- `POST /api/leaves/{id}/approve`：经理审批通过
- `POST /api/leaves/{id}/reject`：驳回并写原因

验收标准
- 状态变更 + `leave_actions` 记录齐全
- 非法状态流转要返回明确错误（例如重复审批）

---

## Day 133（周整合：端到端演示 + SQL复盘）

整合任务（必须）
- 从 0 导入 `oa_schema.sql` + `oa_seed.sql`
- 启动 `oa-demo`，用 curl/Postman 跑通最小流程：
  - 员工登录 → 提交请假
  - 经理登录 → 审批通过/驳回
  - 员工登录 → 查看历史与状态

验收标准（完成即过关）
- 你能演示完整闭环（不要求页面好看）
- 你能口述：RBAC 在哪里生效、业务状态在哪里变更、审计记录写在哪里
