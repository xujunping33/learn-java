# 第 20 周笔记（OA 收尾 + Linux/部署 + Redis）

## Day 134：Vue3 登录页（对接 Session Cookie）

### 学习要点

- 浏览器 **`fetch`** 默认不跨域带 Cookie；同应用下需 **`credentials: 'include'`** 才会带上/接收 **`JSESSIONID`**。
- Servlet **Session** 与 **`Set-Cookie: JSESSIONID`** 对应；后续受 **`AuthFilter`** 保护的 **`/api/*`** 依赖该 Cookie。

### 交付

- **`oa-demo/src/main/webapp/web/login.html`**、**`web/app.html`**、**`web/static/theme.css`**；根 **`index.html`** 增加入口链接。
- 登录成功将 **`data`** 写入 **`sessionStorage.oa_profile`**（仅展示用，不含密码），并跳转 **`app.html`**。

### 口述验收（约 60s）

1. 打开 **Network**，指出登录响应的 **`Set-Cookie`** 与后续请求的 **`Cookie`**。  
2. 说明为何必须用 **`http://…/oa-demo/...`** 访问而不能 **`file://`**（同源与 Cookie 作用域）。

## Day 135：最小工作台（多页面 + UI 级权限入口）

### 交付

- **`GET /api/leaves/pending`**（**`LeavesPendingServlet`**）：**`leave:pending_dept`**；**`WHERE status='SUBMITTED' AND dept_id = (当前用户 employees.dept_id)`**。
- **`LeaveJsons`**：请假行 JSON 与 **`LeavesMeServlet`** 共用。
- 静态页：**`web/leaves.html`**、**`web/pending.html`**、**`web/admin-check.html`**；**`app.html`** 导航按 **`sessionStorage.oa_profile.roles`** 显示/隐藏链接（与后端 RBAC 互补，前端仅 UI）。

### 口述验收

- **`emp` / `mgr` / `admin`** 各有一条可点主路径；经理在 **`pending.html`** 能列表并审批（须为 **`current_assignee_user_id`** 时接口才通过，与 Day132 一致）。

## Day 136：动态 SQL 与数据范围（模板 + 接口筛选）

### 学习要点

- **动态 SQL**：条件是否出现在 **`WHERE`** 由业务决定；**值**一律 **`PreparedStatement`** 绑定，枚举类条件用**白名单**（防注入）。
- **数据范围**：**`GET /api/leaves/me`** 固定 **`applicant_user_id = 当前用户`**（本人）；**`GET /api/leaves/pending`** 固定 **`dept_id = 本人部门`**（经理/具备待审权限者）。**经理若不带 `dept` 约束**（或等价地查全表），即可看到其它部门单据，属于**越权**，故子查询 **`employees.dept_id`** 是必要条件。

### 交付

- **`sql/oa_dynamic_queries.sql`**：≥6 条模板（本人列表、本部门列表、时间窗、RBAC EXISTS、审计筛选、分页），注释说明占位符与 **`dept` / `applicant`** 范围。
- **`LeaveQueryParams`**：解析 **`status`**、**`leaveType`** 查询参数（非法非空 → **400**）。
- **`LeavesMeServlet` / `LeavesPendingServlet`**：按参数动态拼接 **`AND`**（**`pending`** 无 **`status`** 时默认 **`SUBMITTED`**）。

### 口述验收（约 90s）

- 用 **「经理看本部门」** 说明：**`WHERE dept_id = (SELECT … WHERE user_id=?)`** 把结果限制在**与经理同一 `employees.dept_id`** 的行上；再叠 **`status` / `leave_type`** 只是筛选，不改变部门边界。

## Day 137：Linux-1（目录结构 + 权限 + 进程与端口）

### 学习要点

- **`/var/log`**、**`/etc`**、**`/opt`** 各干什么；**`ls -la`** 看权限位；**`chmod` / `chown`** 解决「进不去 / 跑不起来」里的权限类问题。  
- **`ss -tlnp`** / **`ps`** 看端口与进程；**`journalctl`** 在 **systemd** 管理的服务上查启动与报错。  
- 本课 **Tomcat**：**`$CATALINA_HOME/logs/catalina.out`** 是排障主战场。

### 交付

- **`deploy/linux-notes.md`**：个人笔记 + 与本仓库 **`tools/apache-tomcat-10.1.54`** 对齐的练习步骤。

### 口述验收

- **应用起不来**时最先三件事：**端口是否监听**、**日志里最后一条异常**、**权限与属主/脚本可执行位**。

## Day 138：vim + tar + 部署脚本（bash）

### 学习要点

- **vim**：**`i` / `Esc` / `:wq` / `:q!` / `/` + `n` `N`** 够用即可。  
- **tar**：**`-czf`** 打包、**`-xzf`** 解压；备份 **`logs/`** 不删源。  
- **bash**：**`set -euo pipefail`**；部署只删 **`webapps/oa-demo*`**，不误删整个 Tomcat。

### 交付

- **`deploy/deploy-oa.sh`**、**`deploy/backup-oa-tomcat-logs.sh`**；**`deploy/linux-notes.md`** 第 8～10 节；**`day138/README.md`**。

### 口述验收

- 说明 **`deploy-oa.sh`** 做了哪几步；为何 **`rm`** 只针对 **`oa-demo`**；**`tar`** 备份与解压各用什么选项。

## Day 139：Redis 安装配置 + 常用命令 + Jedis

### 学习要点

- **String / Hash**：**`SET`/`GET`**、**`HSET`/`HGET`**（CLI 先敲熟）。  
- **Jedis**：**`JedisPooled`** 作连接池入口；**try-with-resources** 释放连接；复杂结构可先 **JSON 字符串** 存 **String 键**。  
- **与 `HashMap`**：跨进程共享、可选持久化、独立服务与网络访问。

### 交付

- **`redis/redis-notes.md`**；**`redis/jedis-demo`**（**`mvn exec:java`**）；**`day139/README.md`**。

### 口述验收

- **Redis** 与本机 **`HashMap`**：**谁可见、是否落盘、是否单独进程**。

## Day 140：Redis 应用场景（旁路缓存 + 写后删）

### 学习要点

- **Cache-aside**：读先 Redis，miss 再 DB，再 **`SETEX`**；TTL 容忍短时间旧数据。  
- **一致性（朴素）**：**提交 / 审批** 后 **`SCAN` + `DEL`** 清掉该申请人 **`leaves/me*`** 键，避免长期脏读。

### 交付

- **`LeavesMeRedisCache`** + **`LeavesMeServlet`** 缓存；**`LeaveSubmitServlet` / `LeaveDecisionServlet`** 失效；**`oa-demo`** 的 **`redis.properties.example`**；**`day140/README.md`**。

### 口述验收

- **`catalina.out`** 或 **`redis-cli`** 如何看出 **HIT** vs **MISS**；为何审批后要删**申请人**的键而不是审批人。
