# oa-demo（W19 · OA 请假）

## 技术方案（已定）

- **方案 A**：**Maven Web**（**Servlet + JDBC 或 MyBatis**），与当前 `servlet-demo` 学习路径衔接。  
- **Spring Boot**：按个人节奏**后置**接入，本周不引入。

## Day127

- **需求、状态机、权限矩阵**：见 **`docs/requirements.md`**。

## 数据库导入顺序

1. 建库（若尚无）：`CREATE DATABASE IF NOT EXISTS oa_demo ... utf8mb4`  
2. **建表**：`sql/oa_schema.sql`  
3. **种子**：`sql/oa_seed.sql`（**Day129**，可重复执行）

示例（Ubuntu 上 **`root`** 常为 **`auth_socket`**，遇 **1698** 时用 `sudo mysql`）：

```bash
sudo mysql -e "CREATE DATABASE IF NOT EXISTS oa_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql oa_demo < sql/oa_schema.sql
sudo mysql oa_demo < sql/oa_seed.sql
```

## 默认演示账号（Day129）

| 用户名 | 初始密码 | 角色     | 说明           |
|--------|----------|----------|----------------|
| `emp`  | `emp123` | 员工     | 研发部，上级为演示经理 |
| `mgr`  | `mgr123` | 部门经理 | 研发部部门经理 |
| `admin`| `admin123` | 系统管理员 | 管理部 |

密码存库为 **MD5(明文 + salt)**（小写十六进制），算法见 **`sql/oa_seed.sql`** 文件头注释。

## Day130：运行 `oa-demo`（Maven `war`）

- **JDK 21**、**Tomcat 10+**（Jakarta Servlet 6.0）。
- 配置 **`src/main/resources/db.properties`**（可参考 **`db.properties.example`**），保证能连上已导入 **`oa_schema` + `oa_seed`** 的 **`oa_demo`** 库。  
  **若登录接口返回 `50001` 且提示 Access denied**：多为本机 **`root` 使用 `auth_socket`**，JDBC 不能当普通账号用；请按 **`db.properties.example`** 里注释为 **`oa_demo`** 库建 **`oa_demo`@`localhost`** 等带密码用户，再把 **`db.properties`** 改成该用户。
- **Day140（可选）**：复制 **`redis.properties.example`** → **`redis.properties`**，**`redis.enabled=true`** 时为 **`GET /api/leaves/me`** 启用 Redis 旁路缓存（默认关闭，无 Redis 也能跑）。见 **`day140/README.md`**。

### 本仓库自带 Tomcat（与 `servlet-demo` 相同）

解压路径：**`learn/java/tools/apache-tomcat-10.1.54/`**（见 **`day113/README.md`**、**`W17-notes.md`**）。在 **`learn/java` 仓库根目录**执行：

```bash
export CATALINA_HOME="$PWD/tools/apache-tomcat-10.1.54"
cd oa-demo && mvn package -DskipTests && cd ..
rm -rf "$CATALINA_HOME/webapps/oa-demo" "$CATALINA_HOME/webapps/oa-demo.war"
cp oa-demo/target/oa-demo.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" start
```

应用根 URL：**`http://127.0.0.1:8080/oa-demo/`**（等 **`webapps/oa-demo/`** 解压完成后再 curl）。停止：**`"$CATALINA_HOME/bin/catalina.sh" stop`**。

若本机没有 **`tools/apache-tomcat-10.1.54`**，请按 **`day113/README.md`** 下载解压到该路径，或改用系统自带的 **Tomcat 10** 并把 **`oa-demo.war`** 拷到其 **`webapps/`**。

### 接口与统一 JSON

所有接口响应形如 **`{ "code": number, "message": string, "data": any }`**：业务成功 **`code === 0`**；失败时 **`code`** 为非零业务码，HTTP 状态码与语义一致（如 **401** 登录失败）。

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/oa-demo/api/health` | 健康检查，不访问数据库 |
| `POST` | `/oa-demo/api/login` | 登录，写 **Session**（`userId` / `roleCodes` 等） |
| `GET` | `/oa-demo/api/leaves/me` | **Day131 / Day136**：本人列表；可选 **`?status=`**、**`?leaveType=`**（白名单枚举）；**Day140**：可选 **Redis** 旁路缓存（**`redis.enabled=true`**） |
| `GET` | `/oa-demo/api/leaves/pending` | **Day135 / Day136**：本部门列表；默认 **`SUBMITTED`**；可选 **`?status=`**、**`?leaveType=`** |
| `POST` | `/oa-demo/api/leaves` | **Day132**：提交请假（须 **`leave:submit`**） |
| `POST` | `/oa-demo/api/leaves/{id}/approve` | **Day132**：经理通过（须 **`leave:approve`** 且为 **`current_assignee_user_id`**） |
| `POST` | `/oa-demo/api/leaves/{id}/reject` | **Day132**：经理驳回，JSON **`{"remark":"..."}`** |
| `GET` | `/oa-demo/api/admin/ping` | **Day131**：管理端占位（仅 **ADMIN** 角色） |

### curl 示例（默认 **8080**、上下文 **`/oa-demo`**）

```bash
BASE=http://127.0.0.1:8080/oa-demo
curl -sS "$BASE/api/health"
curl -sS -c /tmp/oa-cookies.txt -X POST "$BASE/api/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"emp","password":"emp123"}'
curl -sS -b /tmp/oa-cookies.txt "$BASE/api/leaves/me"
# Day132 / Day133：提交 → 换 mgr 登录 → 通过（LEAVE_ID 取提交响应的 data.id）
SUBMIT_JSON=$(curl -sS -b /tmp/oa-cookies.txt -X POST "$BASE/api/leaves" \
  -H "Content-Type: application/json" \
  -d '{"leaveType":"ANNUAL","startAt":"2026-06-01T09:00:00","endAt":"2026-06-03T18:00:00","reason":"年假"}')
echo "$SUBMIT_JSON"
LEAVE_ID=$(echo "$SUBMIT_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
curl -sS -c /tmp/oa-mgr.txt -X POST "$BASE/api/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"mgr","password":"mgr123"}'
curl -sS -b /tmp/oa-mgr.txt -X POST "$BASE/api/leaves/${LEAVE_ID}/approve"
curl -sS -b /tmp/oa-cookies.txt "$BASE/api/leaves/me"
# 驳回示例（需另一张 SUBMITTED 且你是 current_assignee 的单）：
# curl -sS -b /tmp/oa-mgr.txt -X POST "$BASE/api/leaves/${LEAVE_ID}/reject" \
#   -H "Content-Type: application/json" -d '{"remark":"材料不全"}'
```

- 未登录访问受保护接口：**HTTP 401**，**`code`** 约 **40101**，提示先登录。  
- **`emp`** 访问 **`/api/admin/ping`**：**HTTP 403**，**`code`** 约 **40301**；换 **`admin` / `admin123`** 登录后再访问应 **200**。  
- 非 **`SUBMITTED`** 或 **`current_assignee_user_id`** 不符时审批 → **HTTP 409**，**`code`** 约 **40901**；同一单重复通过也会 **409**。  
- 可选演示数据：**`sql/day131_demo_leaves.sql`**（员工条已带直属经理为 **`current_assignee_user_id`**）；**审批联调仍建议用上面 `POST /api/leaves` 新单**，避免误用列表里历史 **`id`**。

## Day133：端到端演示 + SQL 复盘

### 从零跑一轮（检查清单）

1. **MySQL**：建库 **`oa_demo`** → **`sql/oa_schema.sql`** → **`sql/oa_seed.sql`**（见上文「数据库导入顺序」）。  
2. **`oa-demo`**：**`db.properties`** → **`cd oa-demo && mvn package -DskipTests`** → 部署 **`target/oa-demo.war`** → 启动 Tomcat（见「本仓库自带 Tomcat」）。  
3. **curl**：用上一节 **「curl 示例」** 跑完 **health → emp 登录 →（可选 leaves/me）→ POST leaves → mgr 登录 → approve → emp leaves/me**；或仓库根目录执行 **`./day133/smoke-curl.sh`**（见 **`day133/README.md`**）。

### 口述（验收口径）

- **RBAC**：**`AuthFilter`**（登录态与 **`/api/admin/*`**）；**`Permissions`**（**`leave:submit`** / **`leave:approve`** 等）。  
- **状态**：**`leave_requests.status`** 在 **`LeaveSubmitServlet` / `LeaveDecisionServlet`** 的 **`UPDATE`** 中变更。  
- **审计**：**`leave_actions`** 与上述变更**同事务**写入（**`SUBMIT` / `APPROVE` / `REJECT`**）。

---

## W20 · Day134：Vue3 登录页（Session Cookie）

静态页路径（部署后，上下文一般为 **`/oa-demo`**）：

| 页面 | URL 示例 |
|------|----------|
| 登录 | **`http://127.0.0.1:8080/oa-demo/web/login.html`** |
| 工作台 | **`http://127.0.0.1:8080/oa-demo/web/app.html`**（由登录成功跳转） |

要点：**`fetch(..., { credentials: 'include' })`**；Chrome DevTools → **Network** 查看 **`JSESSIONID`**。勿用 **`file://`** 打开 HTML，否则无法与 **`/oa-demo/api/*`** 同源带 Cookie。

---

## W20 · Day135：多页面工作台 + 权限入口（UI）

| 页面 | 路径 | 说明 |
|------|------|------|
| 工作台 | **`/oa-demo/web/app.html`** | 按 **`roles`** 显示「我的请假 / 待审 / 管理自检」链接 |
| 我的请假 | **`/oa-demo/web/leaves.html`** | **`EMPLOYEE`**：提交表单 + **`GET /api/leaves/me`** 列表 |
| 待审 | **`/oa-demo/web/pending.html`** | **`MANAGER` / `ADMIN`**：**`GET /api/leaves/pending`**，通过 / 驳回按钮 |
| 管理自检 | **`/oa-demo/web/admin-check.html`** | **`ADMIN`**：**`GET /api/admin/ping`** |

角色与 **`oa_seed.sql`** 一致：**`emp`** 走请假页；**`mgr`** 走待审；**`admin`** 走管理自检（待审也可点，数据范围为其所在部门）。

---

## W20 · Day136：动态 SQL 与数据范围

- **SQL 模板**：**`sql/oa_dynamic_queries.sql`**（注释块 + 与 Java 对齐的 **`WHERE`** 思路）。  
- **经理为何必须带 `dept` 条件**：**`LeavesPendingServlet`** 中 **`lr.dept_id = (SELECT dept_id FROM employees WHERE user_id=? …)`** 将结果限制在**当前用户所在部门**；否则可扫到全公司请假单，属越权。  
- **接口**：**`GET /api/leaves/me?status=SUBMITTED&leaveType=ANNUAL`**、**`GET /api/leaves/pending?status=APPROVED`**（非法枚举 → **HTTP 400**，**`code`** **40004**）。

---

## W20 · Day137：Linux 笔记（部署排障）

- 见仓库 **`deploy/linux-notes.md`**：**`ss` / `catalina.out` / `chmod`** 等与 **`tools/apache-tomcat-10.1.54`** 对齐的练习说明。

---

## W20 · Day138：vim + tar + 一键部署脚本

- **`deploy/deploy-oa.sh`**：仓库根执行，停 Tomcat → 构建（**`SKIP_BUILD=1`** 可跳过）→ 仅清理 **`webapps/oa-demo*`** → 拷 **`oa-demo.war`** → 启动。  
- **`deploy/backup-oa-tomcat-logs.sh`**：将 **`$CATALINA_HOME/logs`** 打成 **`deploy/backups/*.tar.gz`**。  
- 细节与 **vim / tar** 速查：**`deploy/linux-notes.md`** 第 8～10 节；**`day138/README.md`**。

---

## W20 · Day139：Redis + Jedis（String / Hash / JSON 字符串）

- **`redis/redis-notes.md`**：**Ubuntu** 安装、**`redis-cli`** 常用命令、与 **`HashMap`** 对比。  
- **`redis/jedis-demo`**：**`JedisPooled`** + **`SET`/`GET`**、**`HSET`/`HGET`** + **Gson** 存 JSON；**`day139/README.md`**。

---

## W20 · Day140：`GET /api/leaves/me` 旁路缓存（Redis）

- **`redis.enabled=true`** 时：**`catalina.out`** 中 **`leaves/me cache HIT` / `MISS`**；**`redis-cli KEYS oa:v1:leaves:me:*`**。  
- **提交 / 审批** 后 **`LeavesMeRedisCache.invalidateUser(申请人 userId)`**（**`SCAN`+`DEL`**）。详见 **`day140/README.md`**。
