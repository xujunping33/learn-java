# boot-social-demo

Spring Boot 3（JDK 21）小型社交 API 示例：注册/登录（**Sa-Token** Bearer Token）、帖子、评论、点赞（幂等），以及评论/点赞的**异步通知**（RabbitMQ）。

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
Token 怎么测：先在 Swagger 里执行 **`POST /api/auth/register`** 或 **`POST /api/auth/login`**，响应中会返回 token（`tokenPrefix` + `tokenValue`）。然后点击右上角 **Authorize**，填入：`Bearer <tokenValue>`（或按响应里的 prefix）。

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
| Swagger | **`/swagger-ui/index.html`**，Bearer Token 方案 **`bearerToken`** |
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
| `APP_SESSION_COOKIE_NAME` | 旧 Session cookie 名（W29 起已切到 Sa-Token，通常无需） |

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

MinIO（W31：对象存储）：
- S3 API：`http://127.0.0.1:${MINIO_API_PORT:-9000}`
- Console：`http://127.0.0.1:${MINIO_CONSOLE_PORT:-9001}`（账号见 `.env.example`：`MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD`）
- bucket：启动时由 `minio-init` 自动创建 `MINIO_BUCKET`（默认 `boot-social`）

**W31 Day213**：应用侧已接入 **AWS SDK for Java v2**（`S3Client` + `S3Presigner`，path-style，兼容 MinIO）。Docker profile 下 `app.minio.enabled=true`，compose 会把 `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` 等传给 `app`。验证上传 + **presigned GET**（需 MinIO 已起且 bucket 存在）：

```bash
curl -sS -X POST http://127.0.0.1:8081/api/dev/storage/smoke
```

**W31 Day215（读取策略）**：
- 业务侧 **不直连暴露 MinIO 密钥**；对外读取使用 **presigned GET**（短期有效 URL）
- Docker 内部访问 MinIO 用 `MINIO_ENDPOINT=http://minio:9000`；但返回给浏览器/宿主机的 URL 必须是可访问地址，因此单独配置 `MINIO_PUBLIC_ENDPOINT`（默认 `http://127.0.0.1:9000`）
- presigned URL 中包含 `&`，终端 `curl` 时务必加引号：`curl -sS "<url>"`

一键冒烟（上传封面 → 详情返回 coverUrl → curl presigned GET → DB 断言）：

```bash
cd boot-social-demo
chmod +x smoke-w31-storage.sh
COVER_FILE=./temp/download.jpeg BASE=http://127.0.0.1:8081 ./smoke-w31-storage.sh
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

## 鉴权说明（Day 198：Sa-Token）

- 登录/注册成功后，接口会返回 `tokenPrefix` + `tokenValue`。客户端请求时通过 Header 传入：
  - `Authorization: Bearer <tokenValue>`
- 受保护的写路径示例：`POST /api/posts`、`POST /api/posts/{id}/comments`、`POST|DELETE /api/posts/{id}/like`。
- Token 策略（见 `application.yml`）：
  - `sa-token.timeout: 7200`：token 过期时间 **2h**
  - `sa-token.active-timeout: 1800`：不活跃超时 **30min**（超过则需重新登录）
  - `POST /api/auth/logout`：服务端立即使当前 token 失效（之后再用会 401）
- 同账号多端登录策略（见 `application.yml` → `app.auth.multi-login-policy`）：
  - `ALLOW`：允许同账号多端同时登录（多 token 并存）
  - `REPLACE`：互踢（新登录顶掉旧 token；旧 token 再用会 401，错误码通常映射为 `AUTH_EXPIRED`）
  - `DENY`：拒绝重复登录（已登录再次登录会 409：`ALREADY_LOGGED_IN`）
- 401/403 错误码契约（错误体 `ApiError`）：
  - `AUTH_REQUIRED`（401）：未登录 / 未携带 token
  - `AUTH_EXPIRED`（401）：token 过期 / 被踢出 / 被顶号
  - `AUTH_INVALID_TOKEN`（401）：token 非法
  - `AUTH_INVALID_CREDENTIALS`（401）：用户名或密码错误
  - `FORBIDDEN`（403）：无权限
  - `ALREADY_LOGGED_IN`（409）：`DENY` 策略下重复登录
  - `RATE_LIMITED`（429）：评论/点赞触发 Redis 限流（W30 Day208；窗口时长见 `app.rate-limit.window-seconds`）

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

W29 MQ（评论/点赞 → 通知落库 + DLQ 演示）可使用：`smoke-w29-token-mq.sh`（见下）。

## API 速览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ping` | 业务侧连通探测（JSON `ApiResult`） |
| GET | `/actuator/health` | Actuator 健康（总体 + dev 下可展开组件含数据源） |
| GET | `/actuator/info` | Actuator 信息（自定义 `service` + 可选 Maven `build`） |
| POST | `/api/auth/register` | 注册并返回 token |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/me` | 当前用户 |
| POST | `/api/posts` | 发帖（需登录） |
| GET | `/api/posts` | 帖子分页列表（`page`/`size`；可选 `keyword`、`userId`；`data` 为 `PageResult`） |
| GET | `/api/posts/{id}` | 帖子详情（含评论列表、`likeCount`、`coverUrl`（presigned GET）） |
| GET | `/api/posts/{id}/comments` | 评论列表 |
| POST | `/api/posts/{id}/comments` | 发表评论（需登录） |
| POST | `/api/posts/{id}/cover` | 上传帖子封面（multipart，jpg/png；仅作者；写入 `posts.cover_object_key`） |
| POST | `/api/posts/{id}/like` | 点赞（需登录，幂等） |
| DELETE | `/api/posts/{id}/like` | 取消点赞（需登录） |

成功体为 `ApiResult<T>`：`{"ok":true,"data":...}`；错误体为 `ApiError`：`code` / `message` / `details`（由 `GlobalExceptionHandler` 统一输出）。

## 异步通知（W29 Day201 + Day202）

写路径（评论/点赞）成功后会 publish 事件，consumer 异步落库到 `notifications`（幂等：`dedup_key UNIQUE`）。

- **Exchange / Queue**
  - notify exchange: `bootsocial.notify.direct`
  - routingKey: `comment.created` / `post.liked`
  - notify queue: `bootsocial.notify.queue`
- **W30 Day206：Outbox（可靠发布）**
  - 写路径（评论/点赞）事务内不再直接发 MQ，而是写入 `outbox_events`
  - 后台定时任务 `OutboxPublisher` 轮询 `outbox_events(status=PENDING)`，发送成功标记为 `SENT`
  - 若 RabbitMQ 暂不可用：业务仍可写入（outbox 堆积），恢复后会自动补发（最终一致）
  - 重试：5s / 30s / 2m / 10m / 1h（封顶），错误写入 `last_error`
- **W30 Day207：消费幂等 + 失败通道（DLQ）**
  - 幂等：`notifications.dedup_key UNIQUE`，消费侧 `INSERT IGNORE`；重复投递见日志 `notify_duplicate …` 或 `inserted=false`
  - 规则（代码收口 `DedupKeys`）：`COMMENT_CREATED` → `comment:{commentId}`；`POST_LIKED` → `like:{postId}:{actorId}`
  - 消费抛异常 → Rabbit 重试（最多 3 次）→ 仍失败则进 **DLQ** `bootsocial.notify.dlq`，可用 `POST /api/dev/dlq/replay` 重放回 notify 队列；再次消费时若已落库则幂等跳过（不重复插行）
  - **实现说明**：`MqConsumers` 对 notify 队列为 **单一监听入口**，参数为原始 **`Message.body`** + `ObjectMapper.readTree`，再按 **`amqp_receivedRoutingKey`** 分流（路由键缺失时根据 JSON 是否含 **`commentPreview`** 区分评论/点赞）。避免 `@Payload JsonNode` 与 Jackson TypeId 推断冲突（易致从不消费），亦避免 DLQ 回放丢失类型头。
- **W30 Day208：Redis 写接口限流（固定窗口）**
  - 作用接口：`POST /api/posts/{id}/like`、`POST /api/posts/{id}/comments`（取消点赞 `DELETE …/like` 不限流）
  - 默认规则：**同一用户 + 同一帖子**，**10 秒**内最多 **3 次**（可配）
  - Key：`bootsocial:rate:like:{postId}:{userId}` / `bootsocial:rate:comment:{postId}:{userId}`；实现为 Redis `INCR`，首次计数时对 key `EXPIRE window-seconds`
  - 超限：**HTTP 429**，错误码 **`RATE_LIMITED`**，`message`：`too many requests for this post (retry after window)`
  - Redis 不可用：**放行**请求（与帖子详情缓存降级一致），仅打 warn，避免本地无 Redis 时写接口全挂
  - 配置：`application.yml` → `app.rate-limit`（`enabled`、`window-seconds`、`max-requests`、`apply-to-like`、`apply-to-comment`）；可用环境变量覆盖，例如 **`APP_RATE_LIMIT_MAX_REQUESTS`**、**`APP_RATE_LIMIT_WINDOW_SECONDS`**
- **W30 Day209：可观测（链路日志 + info 计数）**
  - 日志前缀：`notify_chain_src`（写库后发事件）、`notify_chain_outbox`（落 outbox）、`notify_chain_publish`（发往 MQ）、`notify_chain_consume`（消费落通知）；字段包含 **`eventId`、`dedupKey`、`postId`**，评论另有 **`commentId`、`actorId`、`ownerId`**
  - 排障：`grep notify_chain_ app日志`，可按 **`eventId`** 串联整条链路
  - **`GET /actuator/info`** → `notifyPipeline.notifyConsumeSuccessTotal` / `notifyConsumeFailureTotal`（通知队列消费者成功 / 抛异常次数；失败含重试前的每次异常）
- **DLQ（最小演示）**
  - DLX: `bootsocial.notify.dlx`
  - DLQ queue: `bootsocial.notify.dlq`
  - dev 重放：`POST /api/dev/dlq/replay?limit=10`
  - dev 查看一条 DLQ（排障）：`GET /api/dev/dlq/peek`
  - dev 模拟消费失败开关：`GET/POST /api/dev/mq/failure?enabled=true|false`（**默认 false**；需要 FAIL_CONSUME 演示时再显式 `enabled=true`）

一键冒烟（包含 DLQ + replay + 落库验证）：

```bash
cd boot-social-demo
chmod +x smoke-w29-token-mq.sh
./smoke-w29-token-mq.sh
```

**W30 Day210 收口**：全流程可靠性冒烟（happy path + 幂等不变量 + `FAIL_CONSUME` → DLQ → replay）：

```bash
cd boot-social-demo
chmod +x smoke-w30-reliability.sh
BASE=http://127.0.0.1 ./smoke-w30-reliability.sh    # nginx；直连应用则用 BASE=http://127.0.0.1:8081
```

要求：`jq`、`curl`；Compose **MySQL** 与脚本同级 **`compose.yaml`** 且 **`mysql` 容器可 exec**。密码默认 `PW=p123456`，可用环境变量覆盖。

### W30 收口速查（Outbox / DLQ / 限流）

| 主题 | 要点 |
|------|------|
| **Outbox 表** | `outbox_events`：`exchange_name`、`routing_key`、`payload_type`、`payload_json`、`status`（`PENDING`/`SENDING`/`SENT`）、`retry_count`、`next_retry_at`、`last_error`、`sent_at`（DDL：`deploy/mysql/init.sql`） |
| **发布重试** | `OutboxPublisher`：失败后 `next_retry_at` 间隔约 **5s → 30s → 2m → 10m → 1h** |
| **DLQ** | 队列 `bootsocial.notify.dlq`；查看 `GET /api/dev/dlq/peek`；重放 `POST /api/dev/dlq/replay?limit=N` |
| **消费幂等** | `notifications.dedup_key UNIQUE` + `INSERT IGNORE`；键规则见 `DedupKeys` |
| **Redis 限流** | `app.rate-limit`：`POST …/comments`、`POST …/like`，默认 **10s 内 ≤3 次** → **429 `RATE_LIMITED`** |

Outbox 验证（Docker Compose）：

1. 启动全套：

```bash
cd boot-social-demo
docker compose up -d --build
```

2. 临时停掉 RabbitMQ（模拟 publish 失败）：

```bash
docker compose stop rabbitmq
```

3. 继续发评论/点赞（业务应成功），然后查看 `outbox_events` 有积压（`PENDING`）：

```bash
docker compose exec mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" \
  -e "select id,status,retry_count,routing_key,created_at from outbox_events order by id desc limit 5;"
```

4. 恢复 RabbitMQ，等待 2~5 秒后再次查看，应该逐步变成 `SENT`：

```bash
docker compose start rabbitmq
sleep 3
docker compose exec mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" \
  -e "select id,status,retry_count,routing_key,sent_at,last_error from outbox_events order by id desc limit 5;"
```
