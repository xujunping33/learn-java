# Day131：AuthFilter + GET /api/leaves/me

## 交付

- **`learn.java.oa.filter.AuthFilter`**：白名单 **`/api/health`**、**`/api/login`**；**`/api/admin/*`** 需 **`ADMIN`** 角色。
- **`learn.java.oa.auth.SessionKeys`**：**Session** 属性约定。
- **`LoginServlet`**：登录后写入 **`userId`**、**`roleCodes`**（来自 **`user_roles` + `roles`**）。
- **`LeavesMeServlet`**：**`GET /api/leaves/me`**，仅 **`applicant_user_id = 当前用户`**。
- **`AdminPingServlet`**：**`GET /api/admin/ping`**（占位）。
- 可选：**`sql/day131_demo_leaves.sql`**。

## 验收

- 见 **`oa-demo/README.md`**（**`-c` / `-b` Cookie** 与 **`emp` / `mgr` / `admin`** 对照）。
