# Day133：周整合（端到端演示 + SQL 复盘）

## 整合任务（对照 `W19_PLAN.md`）

1. **从零导入库**（开发机）：**`oa_schema.sql`** → **`oa_seed.sql`**；可选 **`day131_demo_leaves.sql`**（演示列表；**审批联调请优先用 `POST /api/leaves` 新单**，以便带上正确的 **`current_assignee_user_id`**）。  
2. 配置 **`oa-demo/src/main/resources/db.properties`**，**`mvn package`**，部署 **`oa-demo.war`**，启动 **Tomcat 10+**。  
3. 用 **curl** 或 Postman 跑通：**员工登录 → 提交请假 → 经理登录 → 通过或驳回 → 员工再查列表**。

一键自检（需 **`python3`**，且服务已启动）：

```bash
chmod +x day133/smoke-curl.sh   # 只需一次
./day133/smoke-curl.sh
# 或：BASE=http://127.0.0.1:8080/oa-demo ./day133/smoke-curl.sh
```

## SQL 复盘（最小记忆点）

| 表 | 作用 |
|----|------|
| **`users`** / **`roles`** / **`permissions`** | 登录主体与 RBAC 定义 |
| **`user_roles`** / **`role_permissions`** | 用户绑角色、角色绑权限点（如 **`leave:submit`**） |
| **`departments`** / **`employees`** | 部门与员工；**`manager_employee_id`** 用于提交时解析 **`current_assignee_user_id`** |
| **`leave_requests`** | 业务主表：当前 **`status`**、**`version`**、待处理人 **`current_assignee_user_id`** |
| **`leave_actions`** | 审计流水：每次 **提交 / 通过 / 驳回** 对应 **`SUBMIT` / `APPROVE` / `REJECT`** |

## 口述验收（约 60s）

1. **RBAC 在哪生效**：**`AuthFilter`** 拦 **`/api/*`**（白名单除外）；**`/api/admin/*`** 要 **`ADMIN`**；业务里 **`Permissions`** 校验 **`leave:submit`** / **`leave:approve`** 等。  
2. **业务状态在哪变**：**`LeaveSubmitServlet`** 插入 **`SUBMITTED`**；**`LeaveDecisionServlet`** **`UPDATE leave_requests`** 为 **`APPROVED` / `REJECTED`**，并用 **`version`** 防重复审批。  
3. **审计写在哪**：与主表变更**同事务**写入 **`leave_actions`**（**`actor_user_id`**、**`action`**、**`remark`**）。

更细的接口与 curl 片段见 **`oa-demo/README.md`** → **「Day133」** 小节。
