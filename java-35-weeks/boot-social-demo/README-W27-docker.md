# W27 / W28 Docker 小抄（boot-social-demo）

> W27 Day 187：compose 一键起。  
> **W28 Day 192**：`.env` 收口、**`deploy/mysql/init.sql`** 初始化、**mysql / redis / app** 健康检查、**nginx 等 app healthy** 再启动。

## 0. 前置

- 已安装 Docker（并能执行 `docker run --rm hello-world`）。

## 1. 环境变量（推荐）

在 `boot-social-demo/` 下：

```bash
cp .env.example .env
# 按需编辑 .env（勿提交 .env；模板见 .env.example）
```

主要变量：`MYSQL_DATABASE`、`DB_USER`、`DB_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`APP_PORT`、`NGINX_HTTP_PORT`、`REDIS_*`。若改 **库名**，须同时改 **`deploy/mysql/init.sql`** 里的 **`CREATE DATABASE` / `USE`** 与 **`DB_URL`**（或让 `DB_URL` 留空，使用 compose 里按 `MYSQL_DATABASE` 拼好的默认 URL）。

## 2. 一键启动

```bash
cd boot-social-demo
docker compose up -d --build
```

`docker compose ps` 中 **app** 在 **`healthy`** 后，**nginx** 才会起来（避免反代打到未就绪进程）。

看日志：

```bash
docker compose logs -f app
```

## 2.1 RabbitMQ（W29 Day200）

Compose 里增加了 `rabbitmq:3-management`（带管理后台）。默认端口：

- AMQP：`5672`（应用连接用）
- 管理后台：`15672`（浏览器）

访问管理后台（默认账号密码来自 `.env` / `.env.example`）：

```bash
open http://127.0.0.1:15672
# Linux 无 open 时直接用浏览器访问
```

## 2.2 MQ 冒烟（dev）

仅 `dev` profile 提供：

```bash
curl -sS -X POST http://127.0.0.1:8081/api/dev/mq-test \
  -H 'Content-Type: application/json' \
  -d '{"text":"hello mq"}'
```

消费端会打印日志：`mq_test_consume ...`；也可在 RabbitMQ UI 观察 queue。

验收（若 `.env` 里 `APP_PORT` 非 8081，请改端口）：

```bash
curl -sS "http://127.0.0.1:${APP_PORT:-8081}/api/ping"
curl -sS "http://127.0.0.1:${APP_PORT:-8081}/actuator/health"
```

通过 Nginx（默认宿主机 **80**，可用 **`NGINX_HTTP_PORT`** 改；**Day193**：**`/health`** → 后端 **`/actuator/health`**）：

```bash
curl -sS http://127.0.0.1/health
curl -sS http://127.0.0.1/api/ping
```

冒烟（应用端口与 `BASE` 一致）：

```bash
BASE="http://127.0.0.1:${APP_PORT:-8081}" ./smoke-boot-social.sh
```

## 3. 停止 / 重置

**停止**（保留 MySQL 数据卷，库表与数据仍在）：

```bash
docker compose down
```

**重置环境**（删卷 → 下次 `up` 会重新跑 **`init.sql`** 建库表，**数据清空**）：

```bash
docker compose down -v
docker compose up -d --build
```

## 4. 常用排查

### 4.1 改了 Nginx 配置却不生效？

`deploy/nginx/boot-social.conf` 是 **bind mount**；**nginx 只在启动（或 reload）时读配置**。容器若一直在跑，`docker compose up -d --build` 可能 **只重建 app、未重建 nginx**，你会看到 **`/health`** 仍打到后端 **`GET /health`**，应用没有该路由时会走 **`GlobalExceptionHandler`** → JSON 形如 **`INTERNAL_ERROR` / unexpected server error**（而 **`/api/ping`** 仍正常）。

请任选其一：

```bash
docker compose restart nginx
# 或强制重建 nginx
docker compose up -d --force-recreate nginx
```

### 4.2 常规命令

- 状态与健康：

```bash
docker compose ps
docker inspect --format='{{.State.Health.Status}}' "$(docker compose ps -q app)"
```

- 进 MySQL（推荐 compose 子命令，不依赖容器名后缀）：

```bash
docker compose exec mysql mysql -u"${DB_USER:-admin}" -p"${DB_PASSWORD:-AdminPass_123!}" "${MYSQL_DATABASE:-ssm_social}"
```

- 进 Redis：

```bash
docker compose exec redis redis-cli ping
```

## 5. 文件说明

| 路径 | 说明 |
|------|------|
| `compose.yaml` | 服务定义、健康检查、`depends_on` |
| `.env.example` | 可提交的配置模板 |
| `.env` | 本地私密覆盖（勿提交） |
| `deploy/mysql/init.sql` | 首次初始化库表（仅新数据卷执行一次） |
| `deploy/nginx/boot-social.conf` | **`upstream`**、`/` 反代、`/health`→`/actuator/health`、日志格式 |

与本工程同处 **`java-35-weeks/`** 下的 **`sql/ssm_social_schema.sql`** 可作参考；Compose **Day192** 起以 **`deploy/mysql/init.sql`** 为准，避免依赖上级目录路径。
