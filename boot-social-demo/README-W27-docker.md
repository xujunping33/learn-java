# W27 Docker 小抄（boot-social-demo）

> 对应：`W27_PLAN.md` Day 187（compose 一键起）。

## 0. 前置

- 已安装 Docker（并能执行 `docker run --rm hello-world`）。

## 1. 一键启动（MySQL + Redis + App）

在 `boot-social-demo/` 目录：

```bash
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

## 1.1 （可选）通过 Nginx 访问（Day 188）

compose 里包含 `nginx` 服务后，可用 80 端口访问同一套 API：

```bash
curl -sS http://127.0.0.1/api/ping
```

## 2. 停止 / 清理

停止（保留 mysql 数据卷）：

```bash
docker compose down
```

停止并删除数据卷（会清库，慎用）：

```bash
docker compose down -v
```

## 3. 常用排查

- 查看容器状态：

```bash
docker compose ps
```

- 进入 MySQL（容器内）：

```bash
docker exec -it bootsocial-mysql mysql -uadmin -p
```

密码默认来自 `compose.yaml`：`${DB_PASSWORD:-AdminPass_123!}`。

## 4. 配置（可选）

可通过环境变量覆盖（也可放 `.env` 文件）：

- `DB_URL` / `DB_USER` / `DB_PASSWORD`
- `REDIS_HOST` / `REDIS_PORT`

