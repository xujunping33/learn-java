# 第 28 周学习笔记（W28）— 收口与升级（Day 190 起）

对应计划：[W28_PLAN.md](W28_PLAN.md)。主线工程：`boot-social-demo/`。

目标：把 W27 的「能跑」打磨成「可解释、可维护、可协作、可复现」。

---

## Day 190：复盘（5 个风险点）

1. **缓存穿透/击穿**：`post:detail:{id}` 只缓存命中结果时，不存在 id 会反复打 DB；热点 miss 可能存在击穿并发回源。
2. **写路径失效覆盖面不透明**：点赞/评论已 evict，但缺少「写操作 → 失效 key」表，后续加接口易漏。
3. **Compose 与环境变量**：配置分散在本机默认值与 `compose.yaml`，缺统一 `.env`；与本机 Redis/MySQL 端口冲突需注意。
4. **健康检查入口不统一**：~~Nginx 缺独立 `/health`~~ **Day193** 已：`/health` → `/actuator/health`。
5. **CI 缺失**：~~无 push 自动化~~ **Day194** 已加 **`.github/workflows/ci.yml`**（`boot-social-demo` 路径触发）。

---

## 本周必修（2 项）

- **必修 1 — 缓存加固（含 Day 191）**：影响面文档 + **负缓存** `__NULL__`（TTL 默认 30s）+ evict 与读路径一致性。
- **必修 2 — compose / Nginx（Day 192–193）**：`.env` 收口、compose healthcheck、`/health` 反代 actuator。

其余（CI、小重构）按计划在后续天数推进。

---

## Day 191：穿透保护（负缓存）+ evict 面

实现要点：

- Key 仍为 **`post:detail:{id}`**；**命中正文** JSON，`miss` 回源 DB；**确认为不存在** 时 **`SET` `__NULL__`**，TTL **`app.api.post-detail-absent-cache-ttl-seconds`**（默认 30）。
- **`peek` 三态**：`MISS` / `ABSENT` / `Hit(body)`，`GET /api/posts/{id}` 在 `ABSENT` 直接 404 **不再访问 DB**。
- 原有写路径：`POST` 发帖 / 评论、`POST|DELETE` 点赞已对同一 key **`evict`**。

验收：`mvn test`； **`smoke-boot-social.sh`**（可选 `redis-cli` 断言 `GET`/`TTL`）；README 有影响面表。

---

## Day 192：Compose 打磨

- **`.env.example`**：MYSQL/Redis/宿主端口、`DB_*`；本地复制为 `.env`（根 `.gitignore` 忽略 `.env`）。
- **`deploy/mysql/init.sql`**：挂载到 **`docker-entrypoint-initdb.d`**（仅空卷跑一次）；与 **`MYSQL_DATABASE` / JDBC** 默认库名对齐说明写在文件头。
- **健康检查**：原有 mysql、redis；**app** 用容器内 **`curl /api/ping`**（`Dockerfile` 安装 curl）；**start_period** 90s。**nginx**：`depends_on` **app 为 `service_healthy`**。
- **宿主端口**：`APP_PORT`、`NGINX_HTTP_PORT`。**`DB_URL`** 默认串里嵌 **`${MYSQL_DATABASE}`**（Compose 展开）。
- 文档：**`README-W27-docker.md`**（一键启动、**`down -v` 重置**、排查）。

---

## Day 193：Nginx 打磨

- **`upstream bootsocial_app`**：`server app:8081` + **`keepalive`**；`proxy_http_version 1.1` + **`Connection ""`** 配合连接复用。
- **`location = /health`** → **`proxy_pass .../actuator/health`**（对外 JSON 与 Actuator 一致）。
- **`/`** → 原样反代（`/api/**`、Swagger、`/actuator` 等）。
- **Headers**：`Host`、`X-Real-IP`、`X-Forwarded-For`、`X-Forwarded-Proto`、`X-Forwarded-Host`、`X-Forwarded-Port`。
- **访问日志**：`log_format bootsocial_compact`（`upstream_status`、`upstream_addr`、`request_time` 等）。

验收：`curl http://127.0.0.1/health`（或 **`NGINX_HTTP_PORT`**）；`curl …/api/ping`。

---

## Day 194：CI（GitHub Actions）

- **`.github/workflows/ci.yml`**：`ubuntu-latest` + **Temurin 21**；**`setup-java` Maven cache**（`cache-dependency-path: boot-social-demo/pom.xml`）；**`defaults.run.working-directory: boot-social-demo`**；**`mvn -B -ntp verify`**。
- **触发**：`push` / `pull_request` 且路径含 **`boot-social-demo/**`**（或改 workflow）；**`workflow_dispatch`** 可手工跑。
- GitHub 托管 runner 带 **Docker**，Testcontainers 可拉 **`mysql:8.0`**；若遇 Hub 限流可以后换镜像/registry。
- **本机**：有 Docker 仍可能因 **Docker API 版本** 被拒（旧 TC 客户端）；**`pom.xml`** 覆盖 **`testcontainers.version`**（≥**1.21.4**）后 **`SocialFlowIT`** 应真实执行而非 skip。

---

## Day 190：全链路验证（备忘）

```bash
cd boot-social-demo
mvn -q test
docker compose up -d --build
curl -sS http://127.0.0.1:8081/api/ping
curl -sS http://127.0.0.1:8081/actuator/health
curl -sS http://127.0.0.1/api/ping
```
