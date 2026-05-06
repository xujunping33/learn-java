# 第 27 周学习计划：Redis 7 + Docker + Nginx 反代 + Git 协作（基于 `boot-social-demo`）

> 对应总纲：**第27–28周（Redis7 / Nginx / Docker / Git&GitLab + 项目重构升级）** 中的 **第27周**：先把「**能缓存、能容器跑、能反代、能协作提交**」这条链路跑通。  
> 时间：约 **2 小时/天**；本周 **Day 183 ~ Day 189**。  
> 工程主线：继续在 **`boot-social-demo/`** 上增量；**W28** 做「重构升级 + 更深一点的运维/协作」收尾（本文件末尾有衔接说明）。

---

## 本周总目标

- **Redis 7**：能用 CLI 理解 **String / Hash / ZSet** 等常见用途；在 Boot 里用 **`spring-boot-starter-data-redis`**（默认 **Lettuce**）连上 Redis。
- **缓存落地**：给你社交里 **1 个读多写少的热点接口** 加缓存（首选 **`GET /api/posts/{id}` 详情**，或分页列表的最后一页防抖版本二选一）；并设计 **写入后的失效（evict）** 策略。
- **Docker**：会为 Boot 应用写 **`Dockerfile`（multi-stage 可选）**；会用 **`docker compose`** 把 **MySQL + Redis + App** 在本地一键拉起（配置外置：`env`/`.env`，密码不进镜像）。
- **Nginx**：能写最小 **`proxy_pass`** 反向代理到你的 Spring Boot（对外 80，对内 8081）；理解 **Host / X-Forwarded-* / WebSocket（了解即可）**。
- **Git 协作**：按「小团队流程」跑一次：**从 `main` 拉 feature 分支 → 提交原子 commit → MR/PR → 合并回 main**（平台可用 GitHub / GitLab 任一）。

---

## 前置检查（开课当天 10 分钟内完成）

- 本机已装：**Docker Engine + Docker Compose Plugin**、`git`。
- **`boot-social-demo`** 在无 Docker 时能 `mvn -q package`（或你已习惯的构建方式）。
- MySQL：库与表结构与当前代码一致（沿用 W25/W26）。

---

## Day 183（周一）— Redis 7：容器启动 + CLI + 与你的业务对上号

**学什么**

- Redis 典型场景：**缓存**、**限流计数**、**会话外置**（本周先做缓存）。
- 持久化与部署概念：RDB/AOF（先知道名字即可，别这周钻太深）。

**做什么**

1. `docker run` 拉起 **Redis 7**（或 compose 里单独一个 `redis` 服务），本机 `redis-cli ping` 返回 `PONG`。
2. CLI 练习：`SET/GET`、`EXPIRE`、`HSET/HGET`、`INCR`（各 5 分钟）。
3. 在笔记里写清楚：**你准备缓存哪个 URL**、**key 设计**（例如 `post:detail:{id}`）、**TTL 建议**（例如 60s～300s，先能讲理由）。

**验收**

- 能口述：为什么列表分页比详情更难缓存、坑在哪里（排序变化、过滤条件组合爆炸）。

---

## Day 184（周二）— Spring Data Redis：连接、序列化、配置分层

**学什么**

- `RedisTemplate` vs `StringRedisTemplate`；与 **JSON 序列化**（Jackson）组合的常见写法。
- `spring.data.redis.*` 配置：`host/port/password/database`；**dev** 用本地，**docker** 用服务名 `redis`。

**做什么**

1. `pom.xml` 增加 `spring-boot-starter-data-redis`。
2. `application-dev.yml` / `application-docker.yml`（新建 profile）分开写 Redis 地址。
3. 写一个最小 `RedisSmoke`：`@SpringBootTest` 或 `CommandLineRunner`（二选一）验证 `set/get` 成功（注意：测试里可用 Testcontainers Redis 或 `@Disabled` + 本地 Redis，二选一写进 README）。

**验收**

- 应用启动后日志无 Redis 连接报错；能从一个临时接口或 runner 读到写入的值。

---

## Day 185（周三）— 热点接口缓存 + 失效策略（必须“像真项目”）

**学什么**

- **Cache-Aside**：读先查缓存，miss 再查库并回填。
- **一致性**：发帖/改帖/删帖/评论/点赞后，哪些 key 要删？（至少 **详情** 与 **相关列表** 要想清楚）
- **Spring Cache 抽象（可选）**：`@Cacheable` / `@CacheEvict` vs 手写 `RedisTemplate`（本周选一种，别混用直到你理解边界）。

**做什么**

1. 实现 **帖子详情** 缓存（若你详情接口会聚合评论/点赞，明确缓存的是「整包 JSON」还是分层缓存；第一周建议 **整包** 最简单）。
2. 对 **写路径**（`POST/DELETE` 发帖、评论、点赞）加 **evict**：保证下一次读不会长期脏读。
3. 加一个简单的 **命中率日志**（可选）：miss/hit 计数，用 `Slf4j` 输出到 debug。

**验收**

- 同一 `id` 连续读两次：**第二次明显不再打数据库**（用 SQL log 验证）。
- 执行一次会让内容变化的写操作后：再读详情应 **reload**（或通过 TTL 兜底说明策略）。

---

## Day 186（周四）— Docker 镜像：`Dockerfile` 构建可运行 Jar

**学什么**

- 容器镜像分层：JDK/jre、eclipse-temurin 基础镜像选型。
- 构建上下文：别把 `target/` 里巨大无关文件塞进镜像。

**做什么**

1. 在 `boot-social-demo/` 增加 `Dockerfile`：
   - 推荐 **multi-stage**：stage1 `mvn package`，stage2 只拷贝 `jar` + `jvm` 启动命令。
   - 或：**本机构建 jar + Dockerfile 只用 `COPY` jar**（更简单，适合你先把链路跑通）。
2. `docker build -t bootsocial:dev .` 成功。
3. 容器启动参数：用环境变量注入 `SPRING_DATASOURCE_*`、`SPRING_DATA_REDIS_*`、`SERVER_PORT` 等。

**验收**

- `docker run ...` 能启动并访问 `/api/ping`（或 health）。

---

## Day 187（周五）— Docker Compose：MySQL + Redis + App 一键起

**学什么**

- Compose 网络：服务名即 DNS（`jdbc:mysql://mysql:3306/...`）。
- 数据卷：`mysql` 数据持久化；开发期 schema 初始化（`init.sql` 挂载或 migration 工具，二选一）。

**做什么**

1. 增加 `docker-compose.yml`（或 `compose.yaml`）：`mysql`、`redis`、`app` 三服务。
2. App 依赖健康检查（可选但推荐）：`depends_on + condition: service_healthy`。
3. `README-W27-docker.md`（或并入主 README）：**一条命令启动**、`docker compose logs -f app`、如何重建。

**验收**

- 新克隆仓库的人（理论上的你）照着文档能在 **15 分钟内** 从 0 到主流程可调通。

---

## Day 188（周六）— Nginx 反代：`proxy_pass` + 常见 Header

**学什么**

- 为什么前置 Nginx：**TLS 终止（了解）**、静态资源、反代多台后端（了解）、统一入口。
- `proxy_set_header Host $host`、`X-Forwarded-For`、`X-Forwarded-Proto`。

**做什么**

1. 写一个最小 `nginx.conf`（可以放 `deploy/nginx/boot-social.conf`）：
   - `listen 80`
   - `location /` → `proxy_pass http://host.docker.internal:8081`（或 compose 网络上 `proxy_pass http://app:8081`，选一种并保持文档一致）
2. （可选）把 Nginx 也放进 compose 作为第 4 个服务。
3. 用 `curl -v` 验证经过 Nginx 访问 `/api/ping` 与直连一致。

**验收**

- 能解释：为什么 Session/Cookie 路径在反代场景下可能踩坑（SameSite/Secure 先了解即可）。

---

## Day 189（周日）— Git 多分支协作：feature → MR/PR → main

**学什么**

- 分支模型（最小可用）：`main` 保护分支 + `feature/*`。
- Code review 清单：接口兼容性、配置秘密、日志、测试、回滚方式。

**做什么**

1. 从 `main` 拉 `feature/w27-redis-docker-nginx`（名字自拟）。
2. 本周改动分 **3～5 个原子 commit**（Redis / Docker / compose / nginx / docs 分开）。
3. 在 GitHub/GitLab 上开 **MR/PR**：描述里写清 **如何启动 compose**、**缓存了哪个接口**、**如何验证失效**。
4. 自己给自己做一次 review（写 3 条 comment），再合并（模拟团队流程）。

**验收**

- `main` 上能合并出一个“可复现的 W27 交付”；PR 描述别人能看懂。

---

## 本周交付清单（必须）

| 交付物 | 说明 |
|--------|------|
| Redis 缓存 + 失效 | 至少详情或你选定的热点读接口 |
| `Dockerfile` | 可构建可运行 |
| `docker-compose.yml` | mysql + redis + app（+ nginx 可选） |
| Nginx 反代最小配置 | 文档写清访问入口 |
| Git：一次真实 MR/PR 流程 | 分支、描述、合并 |
| 文档 | `README` 增补 W27 章节或小抄 `README-W27.md` |

---

## 与 W28 衔接（预览）

**W28** 建议把注意力放在：**项目重构升级**（包结构/领域边界/重复代码收敛）、**安全默认值**（Redis 密码、Nginx 限流了解、敏感配置）、**CI（GitHub Actions/GitLab CI）跑 `mvn verify`**、以及把 **W27 的 compose** 打磨成更接近生产的形态（资源限制、日志卷、备份策略文档）。

---

## 现在开始：Day 183

1. 用 Docker 跑起 **Redis 7**，`redis-cli ping` 成功。  
2. 在笔记里确定：**缓存 key、TTL、失效点**（写 5 行就够）。  
3. 明天开始改 `boot-social-demo` 接 `spring-boot-starter-data-redis`。

做完把：**Redis 启动方式** + 你选的 **缓存接口 URL** + **key/TTL** 文本发我，我再帮你检查一次「失效是否在写路径全覆盖」。
