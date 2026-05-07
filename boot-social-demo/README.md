# boot-social-demo

Spring Boot 3（JDK 21）小型社交 API 示例：注册/登录（Session）、帖子、评论、点赞（幂等）。

**Day 194 CI（GitHub Actions）**：仓库根 **`.github/workflows/ci.yml`**；在 **`boot-social-demo/`** 下执行 **`mvn -B -ntp verify`**（**Surefire** 单元测 + **Failsafe** `*IT`，Testcontainers 依赖 runner 自带 Docker）。仅当变更路径包含 **`boot-social-demo/**`** 或该 workflow 时触发；也可在 Actions 里 **Run workflow**。

在 GitHub 打开 **Actions** 选对应 run 即看通过/失败；徽章（可选）：把下面 `OWNER/REPO` 换成你的仓库后写入本文件或总 README：

```markdown
![CI](https://github.com/OWNER/REPO/actions/workflows/ci.yml/badge.svg)
```

**W28 baseline（准备进入 W29）**：
- **CI**：Actions 绿（`mvn verify`）
- **Compose**：`docker compose up -d --build` 可起
- **健康检查**：`curl http://127.0.0.1/health` 返回 Actuator JSON
- **冒烟**：`BASE=http://127.0.0.1:<APP_PORT> ./smoke-boot-social.sh` 通过

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

**Day 182**：**Testcontainers** MySQL（`mysql:8.0`，见 **`SocialFlowIT`**）。表结构脚本 **`src/test/resources/testcontainers-schema.sql`**。**`mvn surefire`** 已排除 **`**/*IT.java`**；薄集成放在 **Failsafe**：**`mvn verify`** 额外汇跑 **`register → POST post → GET list`**。类上使用 **`@Testcontainers(disabledWithoutDocker = true)`**：本机 **无 Docker** 或 **无法连上 Docker 套接字** 时 **`SocialFlowIT`** 会 **skipped**（**不导致失败**）。**W28 说明**：较新的 **Docker Engine** 会提高 **API 版本下限**；若日志出现 **`client version … is too old`**，项目已在 **`pom.xml`** 用 **`testcontainers.version`（如 1.21.4+）** 覆盖 Boot 默认的 **1.21.0**，以便本机 **`mvn verify`** 能真正跑通集成测。仍失败时可试环境变量 **`DOCKER_API_VERSION=1.44`**（见 [Testcontainers 与 Docker 29 讨论](https://github.com/testcontainers/testcontainers-java/issues/11210)）。日常 **`mvn test`** 仍可全绿。若 CI 禁止访问 Docker Hub，可自行换镜像或改用 **路线 B**：手工起 MySQL + 导入 **`sql/ssm_social_schema.sql`** 等（本仓库默认路线 **A**）。

**Day 184**：接入 **Spring Data Redis**（`spring-boot-starter-data-redis`，默认 Lettuce）。配置分层：`dev` 默认连 **`127.0.0.1:6379`**（可用 `REDIS_HOST` / `REDIS_PORT` 覆盖），新增 `docker` profile 默认连服务名 **`redis:6379`**（见 `application-docker.yml`）。提供可开关冒烟：启动时加 `-Dapp.redis-smoke=true` 或配置 `app.redis-smoke=true`，会执行一次 `set/get` 并写日志 `redis smoke ok`。

**Day 185**：帖子详情缓存（Cache-Aside）。`GET /api/posts/{id}` 会先查 Redis（key：`post:detail:{id}`），miss 再查 MySQL 聚合 `PostDetailResponse` 并回填缓存；TTL 来自配置 **`app.api.post-detail-cache-ttl-seconds`**（默认 120s）。写路径做失效（evict）：评论与点赞变更后删除该 key，保证下一次读回源刷新。

**Day 191**（穿透保护 + 失效面文档）：仍为同一 key：**`post:detail:{postId}`**；正文为 JSON。**不存在**的帖子在首次查 MySQL miss 后会写入占位符 **`__NULL__`**（负缓存），TTL：**`app.api.post-detail-absent-cache-ttl-seconds`**（默认 **30** 秒）；占位期间再次请求直接 **404**，不再访问 DB。占位与正文都会被写路径 **`evict`** 清掉。**影响面（写 → 失效 key）**：

| 写操作 | 接口（摘要） | 失效 Redis |
|--------|---------------|------------|
| 发帖 | `POST /api/posts` | `post:detail:{id}`（新建的 id） |
| 评论 | `POST /api/posts/{id}/comments` | `post:detail:{id}` |
| 点赞 / 取消 | `POST` / `DELETE /api/posts/{id}/like` | `post:detail:{id}` |
| 列表 / 评论区 | `GET /api/posts`、`GET /api/posts/{id}/comments` | 未缓存该维度（不涉及 evict） |

**验收**：`smoke-boot-social.sh` 开头对「幽灵 id」请求两次 **`GET`**（均 **404** + **`NOT_FOUND`**）。可选：**`redis-cli`** 能访问与 Boot 相同的 Redis 时脚本会校验 **`REDIS_CLI_HOST`** / **`REDIS_CLI_PORT`**（默认 **`127.0.0.1:6379`**）下的值为 **`__NULL__`** 且 **`TTL`>0**。若 **Compose** 未把 Redis 映射到宿主机端口，则用 **`docker compose exec redis redis-cli GET …`**。

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
| `APP_API_POST_DETAIL_ABSENT_CACHE_TTL_SECONDS` | 不存在帖子负缓存占位 TTL（秒，见 Day 191） |
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

## Docker Compose（Day 187 + Day 192）

一键启动（需要 Docker；会创建 MySQL 数据卷）。

**Day 192**：复制 **`boot-social-demo/.env.example`** 为 **`.env`** 收口口令与端口（`APP_PORT`、`NGINX_HTTP_PORT`、`MYSQL_DATABASE` 等）；**`deploy/mysql/init.sql`** 在**首次建卷**时初始化库表（不再挂载仓库根 **`sql/`**）；**mysql / redis / app** 带 **healthcheck**，**nginx** 在 **app 为 healthy** 后启动。**镜像内安装 `curl`** 供 app 探活。**重置**：`docker compose down -v` 后再 `up`（会重新执行 init）。

```bash
cd boot-social-demo
cp .env.example .env   # 可选
docker compose up -d --build
```

看日志：

```bash
docker compose logs -f app
```

验收（默认 **8081**；若改过 `APP_PORT` 请换端口）：

```bash
curl -sS http://127.0.0.1:8081/api/ping
curl -sS http://127.0.0.1:8081/actuator/health
```

更多操作见：`README-W27-docker.md`。

## Nginx（Day 188 + Day 193）

在 `compose.yaml` 里包含 `nginx`（宿主机端口默认 **80**，可由 **`.env`** 的 **`NGINX_HTTP_PORT`** 修改）。`deploy/nginx/boot-social.conf` 定义 **`upstream bootsocial_app`**（`app:8081`）、**`/`** 反代到应用，以及 **`/health` → 后端 `/actuator/health`**（统一探活 JSON）。反代头包含 **`Host`、`X-Real-IP`、`X-Forwarded-*`**；访问日志使用紧凑 **`log_format bootsocial_compact`**（含 **`upstream_status`、`request_time`**）。启动 compose 后：

```bash
curl -sS http://127.0.0.1/health
curl -sS http://127.0.0.1/api/ping
```

若 Nginx 监听非 80，将 `127.0.0.1` 换成 **`127.0.0.1:<NGINX_HTTP_PORT>`**。

若 **`/health`** 返回 **`INTERNAL_ERROR`** 而 **`/api/ping`** 正常：多半是改过 **`boot-social.conf`** 后 nginx 进程未载入新配置，执行 **`docker compose restart nginx`**（见 **`README-W27-docker.md`**）。

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
