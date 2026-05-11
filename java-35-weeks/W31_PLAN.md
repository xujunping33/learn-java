# 第 31 周学习计划：微服务入门之「数据与存储」（MinIO / MongoDB 二选一深挖）

> 对应总纲：**第31周（微服务入门：数据库集群/MyCat/任务调度/Minio/MongoDB）**  
> 目标：理解 **关系库以外** 的常见存储形态，并 **选 1 个方向在 `boot-social-demo` 里落地**，而不是把清单全跑通。  
> 时间：约 **2 小时/天**；本周 **Day 211 ~ Day 217**。  
> **说明**：**MyCat / 分库分表 / 数据库集群** 本周以 **概念 + 面试能讲** 为主（Day 211）；真动手集群成本高，放到你后续工作或专题再深挖。

---

## 本周必须二选一（Day 211 当晚定下来）

- **路径 A（默认推荐）**：**MinIO** —— 帖子配图 / 用户头像等 **对象存储上传 + 读取**（S3 兼容 API）。
- **路径 B**：**MongoDB** —— 为社交项目加一个 **文档型模块**（例如：`audit_logs`、`user_activity`、或「feed 快照」只读集合）。

> 两条都做很容易超时；**只选一条做到「能演示、能讲取舍」** 就是本周成功。

---

## 本周总目标

- **能说清**：关系库 vs 对象存储 vs 文档库分别在解决什么问题、trade-off 是什么。
- **能落地**：在 `boot-social-demo` 上有一条 **真实业务链路** 用上传的路径（A）或文档查询（B）。
- **容器可复现**：`compose.yaml` 增加 MinIO 或 MongoDB；`.env.example` 补变量；README 一条命令能起。
- **安全最小集**：对象存储 **不把 accessKey 泄给浏览器**（上传可走服务端或 presigned URL，二选一写进文档）。

---

## Day 211（周一）— 概念：存储分层 + 何时不用 MySQL 存文件/MyCat 干什么

**学什么**

- **MySQL**：事务、强一致、结构化查询；不适合堆大 blob、不适合海量小文件元数据无模式演进。
- **对象存储（MinIO/S3）**：大文件、CDN 友好、按 key 组织；**不是**用来替代你业务主库的。
- **文档库（MongoDB）**：schema 灵活、嵌套文档、某些聚合/日志场景方便；**不是**“比 MySQL 更快”的通解。
- **MyCat / 分库分表（了解）**：水平扩展、跨库事务变难、运维复杂度上升；本周只记关键词与典型问题。

**做什么**

1. `W31-notes.md`：用 1 页写 **路径 A 或 B** 的取舍（3 条优点 + 3 条缺点）。
2. 定下本周需求一句话（示例）：
   - A：`POST /api/posts/{id}/cover` 上传封面，详情里返回可访问 URL。
   - B：`POST /api/audit` 写审计；`GET /api/audit?userId=` 查询最近 N 条。

**验收**

- 你能用 2 分钟讲清楚：为什么不用 MySQL `BLOB` 存图片/大文件。

---

## 路径 A：MinIO（Day 212 ~ Day 216）

### Day 212（周二）— MinIO：容器、bucket、权限模型（dev vs prod）

**做什么**

1. `compose.yaml` 加 `minio`（console + api 端口写进 README）。
2. 手动或启动脚本创建 bucket（例如 `boot-social`）。
3. 明确 dev 策略：**私有 bucket + 服务端读** 或 **presigned GET**（二选一）。

**验收**

- 浏览器/MinIO Console 能看到 bucket；本机 `curl` 能证明 API 端口可达。

### Day 213（周三）— 接入：S3 SDK（MinIO 兼容）+ Spring 配置

**学什么**

- AWS SDK for Java v2 **`S3Client`** / **`S3AsyncClient`**（MinIO endpoint 指向本地）。
- `application-docker.yml`：`endpoint`、`region`、`accessKey`、`secretKey`、`pathStyleAccess`（MinIO 常开 path-style）。

**做什么**

1. `pom.xml` 增加 AWS SDK v2 S3 依赖（或你们已采用的 BOM）。
2. `@ConfigurationProperties`：`MinioProperties`。
3. 写一个 `StorageService`：`putObject`、`getObject` 或 `presignedGetObject`（按你选择）。

**验收**

- 单元/集成测试或 dev-only controller 能 `put` 一个测试文件并在 MinIO 里看到对象。

### Day 214（周四）— 业务：上传接口 + 元数据进 MySQL

**做什么**

1. 扩展表结构（最小）：
   - `posts.cover_object_key` 或 `users.avatar_object_key`（varchar）
2. `POST /api/.../upload`：`multipart/form-data`，限制 **大小/类型**（先 jpg/png）。
3. Service：上传 MinIO → 返回 `objectKey` → 更新业务表。

**验收**

- 上传后 DB 里能查到 key；重复上传覆盖或版本化策略写一句文档。

### Day 215（周五）— 读取：下载、直链、或 presigned URL

**做什么**

1. 选一种对外读法并写死文档：
   - `GET /api/files/{key}` 服务端流式转发（简单但占带宽）
   - `GET /api/files/{key}/url` 返回 **短期 presigned URL**（更贴近生产）
2. 详情接口：把封面 URL 带入 `PostResponse`（或单独字段 `coverUrl`）。

**验收**

- 前端/ curl 能打开图片；未授权访问行为符合你的安全设定。

### Day 216（周六）— 加固：清理、任务调度（最小）、错误处理

**学什么**

- `@Scheduled` 或 Quartz（本周 **@Scheduled 足够**）：定时清理孤儿对象（可选）。
- 上传失败：删除半成品对象或标记任务重试。

**做什么**

1. 上传失败时日志包含 **traceId/objectKey**（结构化字段）。
2. （可选）每天删 `orphan_objects` 表或按 key 规则清理（可先不做全量，只写设计）。

**验收**

- 你能演示一次失败重试/人工处理路径（哪怕只是文档 + log）。

---

## 路径 B：MongoDB（Day 212 ~ Day 216）

### Day 212（周二）— MongoDB：容器、数据库、集合设计

**做什么**

1. `compose.yaml` 加 `mongo`。
2. 定义集合 `audit_logs` 文档结构：`{ _id, userId, action, target, detail, ts }`。

### Day 213（周三）— Spring Data MongoDB：Repository

**做什么**

1. `spring-boot-starter-data-mongodb`。
2. `AuditLogRepository`：`findTop50ByUserIdOrderByTsDesc`。

### Day 214（周四）— 写入：AOP 或显式 service 记审计

**做什么**

1. 在关键写操作后写审计（发帖/删帖/登录失败等你选 2～3 个）。
2. 索引：`userId + ts`（写在 README）。

### Day 215（周五）— 查询：分页与聚合（选一个）

**做什么**

1. 简单分页：`Pageable`。
2. 或一条聚合：`action` 分布统计。

### Day 216（周六）— TTL 索引（可选）与容量意识

**做什么**

1. （可选）`ttl` 索引自动过期旧日志。
2. 文档写清：Mongo 与 Redis 的区别（别把 Mongo 当缓存）。

---

## Day 217（周日）— 收口：smoke + README + 与 W32 衔接

**做什么**

1. `smoke-w31-storage.sh`（A：上传→读 URL→详情带封面；B：写审计→查列表）。
2. README：`compose` 新增服务、环境变量、端口、安全注意。
3. `W31-notes.md`：**复盘 3 个问题**
   - 为什么选 A/B
   - 生产还差什么（HTTPS、权限、病毒扫描、CDN……各一句）
   - 如果流量 ×10，你会先优化哪里

**验收**

- 新人按 README 能复现；你能用 demo 讲「存储选型」。

---

## 本周交付清单（必须）

| 交付物 | 说明 |
|--------|------|
| 二选一落地 | MinIO **或** MongoDB（另一条只做笔记也行） |
| `compose.yaml` | 新增相应服务 |
| 业务接口 + 持久化 | MySQL 元数据（A）或 Mongo 文档（B） |
| 文档 + smoke | 可复现 |
| `W31-notes.md` | 取舍与复盘 |

---

## 与 W32 衔接（预览）

**W32** 将进入 **Nacos + Feign + Gateway + Sentinel**：对象存储/文档库会继续作为「拆分服务后的外部依赖」，本周的 **key/URL 设计**不要写死到 controller 里，尽量收口在 `StorageService` 或 `AuditService`，拆服务时更省事。

---

## 现在开始：Day 211

1. 打开总纲复习第 31 周那一行目标。  
2. 选定 **A 或 B**，用一句话写下本周要交付的用户可见功能。  
3. 新建 `W31-notes.md`，写完「3 优点 3 缺点」。

把 **你选的 A/B** 和 **那一句话需求** 发我，我再按你当前 `boot-social-demo` 的表结构，帮你定 **最小 DDL 字段名** 和 **接口路径**（避免和现有 Post/User 冲突）。
