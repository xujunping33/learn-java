# 第 27 周学习笔记（W27）— Redis + Docker + Compose + Nginx + Git（基于 `boot-social-demo`）

> 对应计划：[W27_PLAN.md](W27_PLAN.md)。本周目标是把「**缓存 + 容器化 + 反代 + 协作提交**」跑成可复现链路。  \n+> 工程主线：`boot-social-demo/`。

---

## 0. 前置检查（最容易卡的点）

### 0.1 Docker 权限（`docker.sock`）

现象：
- `permission denied while trying to connect to the docker API at unix:///var/run/docker.sock`

原因：
- 当前用户不在 `docker` 组，或已加入但 **会话未刷新**。

处理：
- `sudo usermod -aG docker "$USER"`
- 让权限生效：`newgrp docker` 或 **注销/重新登录**
- 验证：`docker run --rm hello-world`

### 0.2 本机已有 MySQL / Redis 端口占用

现象：
- compose 里 `redis:6379` 映射到宿主机时报 `address already in use`
- compose 里 `mysql:3306` 映射到宿主机时报 `address already in use`

处理策略（更贴近生产）：
- **不映射** `mysql` / `redis` 到宿主机：只在 compose 内网给 `app` 用
- 仅把 `app`（8081）和 `nginx`（80）映射到宿主机

---

## 1. Day 183：Redis 7 基础（CLI）

关键点：
- `SET/GET`：缓存基本读写
- `EXPIRE` / `TTL`：缓存自动淘汰
- `HSET/HGET`：对象属性类数据
- `INCR`：计数器（命中率/限流）

概念速记：
- **RDB**：定时快照（恢复快，可能丢两次快照间数据）
- **AOF**：追加日志（数据更新更细，文件更大、恢复可能更慢）

---

## 2. Day 184：Spring Data Redis 接入

### 2.1 依赖与连接配置

- 依赖：`spring-boot-starter-data-redis`（默认 Lettuce）
- `dev`：默认连 `127.0.0.1:6379`（可用 `REDIS_HOST/REDIS_PORT` 覆盖）
- `docker`：默认连 `redis:6379`（见 `application-docker.yml`，用于 compose）

### 2.2 冒烟验证（Runner）

- `RedisSmokeRunner`（`dev`/`docker`）：通过 `app.redis-smoke=true` 执行一次 `set/get` 并打印 `redis smoke ok`
- **注意**：用 `mvn spring-boot:run` 时若要把参数传给应用，推荐用：
  - `-Dspring-boot.run.arguments=--app.redis-smoke=true`

### 2.3 测试 profile 隔离

`test` profile 不依赖 Redis：在 `application-test.yml` 排除
- `RedisAutoConfiguration`
- `RedisRepositoriesAutoConfiguration`

---

## 3. Day 185：热点接口缓存 + 失效（像真项目）

### 3.1 缓存对象与 key 设计

选择缓存接口：**`GET /api/posts/{id}`**（详情聚合：post + comments + likeCount）。  \n+采用 **Cache-Aside**：
- 先查缓存，miss 再查 DB 并回填
- 缓存内容：整包 `PostDetailResponse` JSON（最简单可落地）

key：
- `post:detail:{id}`

TTL：
- `app.api.post-detail-cache-ttl-seconds`（默认 120s；可调）

### 3.2 失效策略（evict）

写路径会影响详情聚合数据，必须 evict：
- `POST /api/posts/{id}/comments`（评论变化）
- `POST /api/posts/{id}/like`、`DELETE /api/posts/{id}/like`（点赞变化）
- （未来若有编辑/删帖，也应 evict）

### 3.3 验收方法（比看 curl 输出更可靠）

看 Redis：
- `redis-cli EXISTS post:detail:19`
- `redis-cli TTL post:detail:19`

验证 evict：
- 读一次详情让 key 出现（EXISTS=1）
- 点赞后 key 应消失（EXISTS=0）
- 再读详情 key 重新出现（EXISTS=1）

---

## 4. Day 186–187：Dockerfile + Compose 一键起（MySQL + Redis + App）

### 4.1 Dockerfile（multi-stage）

- stage1：Maven 构建 jar（`-DskipTests`）
- stage2：JRE21 运行 jar
- 默认 profile：`SPRING_PROFILES_ACTIVE=docker`

### 4.2 Compose（可复现环境）

文件：
- `boot-social-demo/compose.yaml`
- `boot-social-demo/README-W27-docker.md`

服务：
- `mysql`（挂载 `../sql/ssm_social_schema.sql` 作为 init 脚本；volume 持久化）
- `redis`
- `app`（依赖 mysql/redis 健康检查）

常用命令：

```bash
cd boot-social-demo
docker compose up -d --build
docker compose logs -f app
docker compose ps
docker compose down
docker compose down -v   # 清库重来（慎用）
```

### 4.3 一个关键坑：profile 导致 API “消失”

现象：
- `POST /api/auth/register` 变成 500，日志里是 `No static resource api/auth/register`

原因：
- 容器里跑 `docker` profile，但 Controller/Service/Interceptor 等仅标 `@Profile("dev")`，导致 **Bean 没加载**，路由不存在。

处理：
- 将这些 Bean profile 扩展为 `@Profile({"dev","docker"})`

验收：
- `BASE=http://127.0.0.1:8081 ./smoke-boot-social.sh` → `ALL OK`
- `/actuator/health` → `UP`

---

## 5. Day 188：Nginx 反代（proxy_pass）

目标：
- 对外只暴露 `80`，内部转发到 `app:8081`
- 为后续 HTTPS、限流、多实例负载均衡打基础

实现：
- `boot-social-demo/deploy/nginx/boot-social.conf`
- `compose.yaml` 增加 `nginx` 服务：`80:80`，挂载配置

验收：

```bash
curl -sS http://127.0.0.1/api/ping
```

Header（关键转发头）：
- `Host`
- `X-Forwarded-For`
- `X-Forwarded-Proto`

---

## 6. Day 189：Git 协作流程（feature → PR → main）

本周在仓库中演练了：
- 从 `main` 拉分支：`feature/w27-redis-docker-nginx`
- 拆分原子提交（示例：feat vs docs 分开）
- 推送到 `origin` 并通过 GitHub 链接创建 PR

注意点：
- 仓库里存在大量学习目录/笔记文件，PR 建议只包含本周主线工程增量，避免“巨型 PR”。

---

## 7. 本周交付清单对照

- Redis 缓存 + 失效：✅（详情缓存 + 点赞/评论 evict）
- Dockerfile：✅
- docker compose：✅（mysql + redis + app；健康检查；可一键起）
- Nginx 反代：✅（compose 第 4 服务）
- Git：✅（feature 分支 + push + PR 链接）
- 文档：✅（`README.md` + `README-W27-docker.md`）

