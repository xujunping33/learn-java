# Day135：多页面工作台 + 待审列表

## 交付

- 后端：**`GET /api/leaves/pending`**（**`LeavesPendingServlet`**），**`web.xml`** 中映射在 **`/api/leaves/*`** 之前，避免被通配抢路径。
- 前端：**`web/leaves.html`**（员工提交 + 本人列表）、**`web/pending.html`**（待审 + 通过/驳回）、**`web/admin-check.html`**（管理员 **`/api/admin/ping`**）；**`app.html`** 导航与 **`static/theme.css`** 表格样式。

## 验收

- 浏览器分别用 **`emp` / `mgr` / `admin`** 登录，从工作台进入对应页，能完成：**提交 → 待审 → 审批**（管理员以 **`admin-check`** 为主路径即可）。
