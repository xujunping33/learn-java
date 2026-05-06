# boot-social-demo

Spring Boot 3（JDK 21）小型社交 API 示例：注册/登录（Session）、帖子、评论、点赞（幂等）。

## 前置条件

- JDK 21、Maven 3.9+
- MySQL 中已有库表（与 `application-dev.yml` 里库名一致，默认 `ssm_social`），包含 `users`、`posts`、`comments`、`post_likes` 等（可与 W24 `ssm-social-demo` 同源 schema）。

## 配置说明

| Profile | 用途 | 说明 |
|--------|------|------|
| **dev**（默认） | 本地开发 | `application.yml` 激活 `dev`；连接 MySQL；端口 **8081**（见 `application-dev.yml`）。 |
| **test** | `mvn test` | `application-test.yml` 关闭数据源与 MyBatis-Plus，只保留 Web 等轻量 Bean（如 `GET /api/ping`）。 |

**Day 176**：已接入 **MyBatis-Plus**（`mybatis-plus-spring-boot3-starter` + **`mybatis-plus-jsqlparser`**（分页/SQL 解析所需，3.5.9 起单独依赖），版本见 `pom.xml` 的 `mybatis-plus.version`），`dev` profile 下启用 **`PaginationInnerInterceptor`**（MySQL）。`UserMapper` 继承 `BaseMapper<User>`；按用户名查询走 **`LambdaQueryWrapper`**，与 XML 里的 `insertUser` / `findById` 共存。

**Day 177**：`GET /api/posts` 使用 MP **`Page`** 分页（自定义 XML `pagePosts`，由分页插件追加 `LIMIT`）；`data` 为 **`PageResult`**（字段 **`items` / `total` / `page` / `size`**）。可选：`keyword`（标题模糊）、`userId`（作者 id，`>=1`）。`page`、`size` 非法时返回 **400** + `VALIDATION_FAILED`。

**Day 178**：接入 **springdoc-openapi**（`springdoc-openapi-starter-webmvc-ui`，版本见 `springdoc.version`）。本地启动后用浏览器打开 Swagger UI：**<http://127.0.0.1:8081/swagger-ui/index.html>**（旧路径 **`/swagger-ui.html`** 多数版本会跳转）。原始 OpenAPI JSON：**<http://127.0.0.1:8081/v3/api-docs>**。  
Session 怎么测：**先**在 Swagger 里执行 **`POST /api/auth/register`** 或 **`POST /api/auth/login`**，同源 Try it out 会使用浏览器会话，服务端 **`Set-Cookie`**（cookie 名默认 **`JSESSIONID`**，见 **`app.session.cookie-name` / `server.servlet.session.cookie.name`**）会在后续请求中自动带上，即可直接调 **`POST /api/posts`** 等。**或者**：用开发者工具或 `curl -c`/`curl -b` 复制该 cookie **值**，在 Swagger **Authorize** 里按文档中的 cookie 名填写。锁标记接口同上。

**Day 179**：**Spring Boot Actuator**（`spring-boot-starter-actuator`）。**仅**通过 HTTP 暴露 **`health`**、**`info`**（未使用 `expose: '*'`）。根配置里 **`management.endpoint.health.show-details: never`**；**dev** 覆盖为 **`always`**，便于本地看 **`db`** 等组件是否 **UP**。自定义 **`BootSocialInfoContributor`** 写入 **`service`**；若已执行 Maven **`build-info`**，**`build`** 中含 **`artifact` / `version` / `time`**。运维探活：**`GET /actuator/health`**、**`GET /actuator/info`**（同上主机端口，默认 **8081**）。可按教材把 **management.server.port** 另起端口与安全策略放到后续「上线」章节。

**Day 180**：**`AppProperties`**（`@ConfigurationProperties(prefix = "app")`）：**`api.default-page-size`**（`GET /api/posts` 未传 **`size`** 时的默认条数，`1～100`，默认 `20`，可用环境变量 **`APP_API_DEFAULT_PAGE_SIZE`** 覆盖）；**`cors.origins`**（列表，非空时注册 **`/api/**`** CORS，`allowCredentials=true`，须写明前端 origin，不可用 `*`）；**`session.cookie-name`**（默认 `JSESSIONID`，与 **`server.servlet.session.cookie.name: ${app.session.cookie-name}`** 联动）。本地敏感配置可放 **`application-local.yml`**（已加入仓库根 **`.gitignore`**；**`.env`** 也已忽略），勿提交密码。数据库仍推荐 **`DB_PASSWORD`** 等环境变量（与现有 `spring-boot:run` 用法一致）。

**Day 181**（JDK 21）：对外 **请求/响应 DTO** 已为 **`record`** + **`@Schema`**；**`PostController`** 内用 **`switch` + `null` / `Long` pattern**（`longOrZero`）替代多处 **`Long` 空值三元表达式**，JSON 字段不变。业务异常仍由 **`BizException` + `GlobalExceptionHandler`** 集中处理，未再叠一层按 `code` 映射（计划允许「已集中处理可略」）。

**Day 182**：**Testcontainers** MySQL（`mysql:8.0`，见 **`SocialFlowIT`**）。表结构脚本 **`src/test/resources/testcontainers-schema.sql`**。**`mvn surefire`** 已排除 **`**/*IT.java`**；薄集成放在 **Failsafe**：**`mvn verify`** 额外汇跑 **`register → POST post → GET list`**。类上使用 **`@Testcontainers(disabledWithoutDocker = true)`**：本机 **无 Docker** 时 **`SocialFlowIT`** 在 **`mvn verify`** 中会被 **skipped**（报告里 `skipped=1`，**不导致失败**）；终端里 Testcontainers 可能打印一条「找不到 Docker」的提示，可忽略。日常 **`mvn test`** 仍可全绿。若 CI 禁止访问 Docker Hub，可自行换镜像仓库或改用 **路线 B**：手工起 MySQL + 导入仓库根 **`sql/ssm_social_schema.sql`**，再给 **`dev`**（或复制一份 profile）配置 **`SPRING_DATASOURCE_*`**，用 **`@SpringBootTest`** 写一条同类 HTTP 集成（本仓库默认走路线 **A**）。

**Day 184**：接入 **Spring Data Redis**（`spring-boot-starter-data-redis`，默认 Lettuce）。配置分层：`dev` 默认连 **`127.0.0.1:6379`**（可用 `REDIS_HOST` / `REDIS_PORT` 覆盖），新增 `docker` profile 默认连服务名 **`redis:6379`**（见 `application-docker.yml`）。提供可开关冒烟：启动时加 `-Dapp.redis-smoke=true` 或配置 `app.redis-smoke=true`，会执行一次 `set/get` 并写日志 `redis smoke ok`。

**Day 185**：帖子详情缓存（Cache-Aside）。`GET /api/posts/{id}` 会先查 Redis（key：`post:detail:{id}`），miss 再查 MySQL 聚合 `PostDetailResponse` 并回填缓存；TTL 来自配置 **`app.api.post-detail-cache-ttl-seconds`**（默认 120s）。写路径做失效（evict）：评论与点赞变更后删除该 key，保证下一次读回源刷新。

---

### W26 新增能力一览（收口）

| 能力 | 说明 |
|------|------|
| MyBatis-Plus | Boot3 starter + jsqlparser，`UserMapper`/`PostMapper`，分页插件 |
| 分页列表 | `PageResult`、`keyword`/`userId` |
| Swagger | **`/swagger-ui/index.html`**，Session Cookie 方案 **`sessionCookie`** |
| Actuator | 仅 **`health`** / **`info`**，`BootSocialInfoContributor`，dev 展开 db |
| 配置绑定 | **`AppProperties`**，默认分页 size / CORS / session cookie 名 |
| JDK 21 | **`record` DTO**，`PostController.longOrZero` **switch pattern** |
| 集成测试 | **`SocialFlowIT`** + Failsafe；或见上「无 Docker → 路线 B」 |

---

环境变量（覆盖 `application-dev.yml` 默认值）：

| 变量 | 含义 |
|------|------|
| `DB_URL` | JDBC URL |
| `DB_USER` | 数据库用户 |
| `DB_PASSWORD` | 数据库密码 |
| `APP_API_DEFAULT_PAGE_SIZE` | 分页默认 **`size`**（`1～100`，见 Day 180） |
| `APP_SESSION_COOKIE_NAME` | Session cookie 名（与 **`app.session.cookie-name`** 对应） |

示例：

```bash
export DB_PASSWORD='your_password'
cd boot-social-demo
mvn -q spring-boot:run
```

服务默认：<http://127.0.0.1:8081>

## 启动方式

```bash
cd boot-social-demo
mvn -q spring-boot:run
```

单元 / 切片测试（使用 **`test`** profile，`PingTest` **无 MySQL**，**无 Docker**）：

```bash
mvn -q test
```

集成测试（**Failsafe**，需 **Docker**，首次会拉 **`mysql:8.0`** 镜像）：

```bash
cd boot-social-demo && mvn -q verify
```

## Docker（Day 186）

构建镜像（在 `boot-social-demo/` 目录下）：

```bash
docker build -t bootsocial:dev .
```

直接运行容器（需要你自己准备 MySQL / Redis，并用环境变量注入地址）：

```bash
docker run --rm -p 8081:8081 \
  -e DB_URL='jdbc:mysql://host.docker.internal:3306/ssm_social?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8' \
  -e DB_USER='admin' \
  -e DB_PASSWORD='your_password' \
  -e REDIS_HOST='host.docker.internal' \
  -e REDIS_PORT='6379' \
  bootsocial:dev
```

说明：
- 默认 `SPRING_PROFILES_ACTIVE=docker`（见 `Dockerfile` / `application-docker.yml`），后续 Day 187 会用 compose 把 `mysql` / `redis` / `app` 一键拉起。

## Docker Compose（Day 187）

一键启动（需要 Docker；会创建 MySQL 数据卷）：

```bash
cd boot-social-demo
docker compose up -d --build
```

看日志：

```bash
docker compose logs -f app
```

验收：

```bash
curl -sS http://127.0.0.1:8081/api/ping
curl -sS http://127.0.0.1:8081/actuator/health
```

更多操作见：`README-W27-docker.md`。

## Nginx（Day 188）

在 `compose.yaml` 里包含了 `nginx`（监听宿主机 **80**，反代到 compose 内网的 `app:8081`）。启动 compose 后可通过 Nginx 访问：

```bash
curl -sS http://127.0.0.1/api/ping
```

## 鉴权说明（Day 175）

- **Session**：登录或注册成功后写入 `SessionKeys.UID`（用户 id）。
- **`AuthInterceptor`**（仅 **dev**）：对帖子相关**写**接口校验 Session；**GET**（列表、详情、`/comments` 列表）匿名可访问。
- 受保护的写路径示例：`POST /api/posts`、`POST /api/posts/{id}/comments`、`POST|DELETE /api/posts/{id}/like`。

## curl 主流程

先赋予脚本执行权限：

```bash
chmod +x smoke-boot-social.sh
```

默认访问 `http://127.0.0.1:8081`，可通过环境变量 `BASE` 修改：

```bash
BASE=http://127.0.0.1:8081 ./smoke-boot-social.sh
```

脚本顺序：**注册** → **发帖** → **列表** → **详情** → **评论** → **点赞** → **再看详情**（含 `comments` 与 `likeCount`）。

更完整的点赞幂等校验可使用 `smoke-day174.sh`。

## API 速览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ping` | 业务侧连通探测（JSON `ApiResult`） |
| GET | `/actuator/health` | Actuator 健康（总体 + dev 下可展开组件含数据源） |
| GET | `/actuator/info` | Actuator 信息（自定义 `service` + 可选 Maven `build`） |
| POST | `/api/auth/register` | 注册并入 Session |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/me` | 当前用户 |
| POST | `/api/posts` | 发帖（需登录） |
| GET | `/api/posts` | 帖子分页列表（`page`/`size`；可选 `keyword`、`userId`；`data` 为 `PageResult`） |
| GET | `/api/posts/{id}` | 帖子详情（含评论列表、`likeCount`） |
| GET | `/api/posts/{id}/comments` | 评论列表 |
| POST | `/api/posts/{id}/comments` | 发表评论（需登录） |
| POST | `/api/posts/{id}/like` | 点赞（需登录，幂等） |
| DELETE | `/api/posts/{id}/like` | 取消点赞（需登录） |

成功体为 `ApiResult<T>`：`{"ok":true,"data":...}`；错误体为 `ApiError`：`code` / `message` / `details`（由 `GlobalExceptionHandler` 统一输出）。
