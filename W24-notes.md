# 第 24 周笔记（SSM 社交网站实战：Spring + Spring MVC + MyBatis）

本周目标：做一个可部署 Tomcat 的 **WAR** 项目（不使用 Spring Boot），走完最小社交流程：

- 注册 / 登录 / 退出（Session）
- 发帖 / 列表 / 详情（含作者）
- 评论（新增 + 列表 + 详情聚合可选）
- 点赞（防重复）+ likeCount
- 工程化：统一错误体、smoke 脚本、部署脚本

---

## Day 162：脚手架 + `/api/ping`

### 学习要点

- **WAR + Tomcat**：应用以 `ssm-social-demo.war` 形式部署，context path 默认 `/ssm-social-demo`。
- **Servlet 3+ 启动入口**：`AbstractAnnotationConfigDispatcherServletInitializer` 代替 `web.xml`。

### 交付

- `ssm-social-demo/`（`packaging: war`）
- `GET /api/ping` → `{"ok":true}`

---

## Day 163：DataSource + MyBatis + 第一个 Mapper

### 学习要点

- **DataSource（HikariCP）**：Spring 管理连接池，MyBatis 使用它获取连接。
- **MyBatis-Spring**：`SqlSessionFactoryBean` + `MapperScannerConfigurer` 扫描 mapper 接口。
- **XML Mapper**：用 `resultMap` 把表字段映射到 Java 字段（支持 `mapUnderscoreToCamelCase`）。

### 交付

- `UserMapper`：`insertUser / findByUsername / findById`
- `UserService`：最小通路（后续扩展为 register/login）
- `smoke-users.sh`

---

## Day 164：Session 登录态（register/login/logout/me）

### 学习要点

- **HttpSession**：登录成功后 `session.setAttribute("uid", id)`，登出 `session.invalidate()`。
- **统一错误体**：`ApiErrorBody(code, message)`；异常集中处理（`@RestControllerAdvice`）。
- **密码策略（demo 版）**：salt + hash（SHA-256）封装在 `Passwords` 工具类中。

### 接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`（204）
- `GET /api/me`（未登录 401）

### Smoke

- `smoke-auth.sh`

---

## Day 165：发帖 / 列表 / 详情（含作者信息）

### 学习要点

- **分层**：Controller 处理入参/鉴权/返回；Service 编排与事务；Mapper 只写 SQL。
- **关联查询**：post + author（`JOIN users` 拿 `authorUsername`）。
- **最简分页**：`limit/offset`。

### 接口

- `POST /api/posts`（需登录，201）
- `GET /api/posts?limit=&offset=`（公开）
- `GET /api/posts/{id}`（公开）

### Smoke

- `smoke-posts.sh`

---

## Day 166：评论（新增 + 列表）+ 详情聚合（可选）

### 学习要点

- 评论表 `comments` 关联 `posts/users`，列表按时间稳定排序。
- 详情页聚合：`GET /api/posts/{id}?includeComments=true` 返回 post + comments。

### 接口

- `POST /api/posts/{id}/comments`（需登录，201）
- `GET /api/posts/{id}/comments`（公开）
- `GET /api/posts/{id}?includeComments=true`（公开，含 `comments` 数组）

### Smoke

- `smoke-comments.sh`

---

## Day 167：点赞（防重复）+ likeCount

### 学习要点

- **防重复点赞**：`post_likes(post_id,user_id)` 唯一约束 + `INSERT IGNORE`。
- **likeCount**：列表/详情通过聚合查询返回 `likeCount`。

### 接口

- `POST /api/posts/{id}/like`（需登录，204）
- `DELETE /api/posts/{id}/like`（需登录，204，可选但已实现）
- `GET /api/posts`、`GET /api/posts/{id}` 返回 `likeCount`

### Smoke

- `smoke-like.sh`

---

## Day 168：拦截器鉴权 + 最小缓存 + README

### 学习要点

- **Spring MVC Interceptor**：把“写操作需登录”的逻辑从 Controller 里抽出来统一做。
- **注意点**：
  - **`OPTIONS`** 预检要放行
  - **`GET`** 读接口应公开（只拦写操作）
  - 拦截器 `return false` 时不会进入 `@ControllerAdvice`，因此需要自己写 401 JSON（与统一错误体对齐）
- **最小缓存**：`PostService#getPostDetail` 加 30 秒内存缓存；点赞后需 invalidation，避免 likeCount 不更新。

### 交付

- `AuthInterceptor`（Session 鉴权，保护发帖/评论/点赞等写接口）
- `PostService` 详情缓存（30s）+ 点赞后缓存失效
- `ssm-social-demo/README.md`：从 0 跑起、部署、smoke、curl 示例

---

## 一键复现（推荐）

仓库根目录：

```bash
./deploy/deploy-ssm-social-demo.sh
```

跑 smoke（按天）：

```bash
SSM_BASE=http://localhost:8080/ssm-social-demo bash ssm-social-demo/smoke-auth.sh
SSM_BASE=http://localhost:8080/ssm-social-demo bash ssm-social-demo/smoke-posts.sh
SSM_BASE=http://localhost:8080/ssm-social-demo bash ssm-social-demo/smoke-comments.sh
SSM_BASE=http://localhost:8080/ssm-social-demo bash ssm-social-demo/smoke-like.sh
```

