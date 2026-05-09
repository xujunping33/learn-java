# 第 29 周学习笔记（W29）— 分布式入门（Day 197 起）

对应计划：[W29_PLAN.md](W29_PLAN.md)。主线工程：`boot-social-demo/`。

本周默认落地两件事：
- **Sa-Token**：Session → Token（更适合前后端分离/多实例）
- **RabbitMQ**：评论/点赞 → 通知作者（异步解耦）

---

## Day 197：概念备忘（2 页以内）

### 1）为什么单体会遇到瓶颈（结合本项目）

- **吞吐**：评论/点赞是高频写；如果同步做“写业务 + 写通知 + 额外查询”，接口响应时间上升。
- **耦合**：通知逻辑（给作者发通知）和核心写接口强绑定，改通知策略会影响主链路。
- **发布风险**：通知模块出 bug，会拖垮发帖/评论/点赞主链路（不该被影响）。
- **数据库压力**：通知如果同步落库，会把额外写压到同一个事务/同一个 DB 上。

### 2）异步（MQ）的收益与代价

- **收益**
  - **解耦**：主链路只关心业务写成功；通知作为“副作用”异步处理。
  - **削峰**：短时间大量点赞/评论，消息堆积在 MQ，消费者按能力消费。
  - **可演进**：未来可换成更多消费者（短信/站内信/邮件），发布互不影响。

- **代价**
  - **最终一致性**：写成功后，通知可能会延迟几秒出现。
  - **消息丢失/重复**：必须设计“至少一次”下的幂等（dedup）。
  - **排查更难**：要能追踪一条写请求对应的消息与消费结果（日志/trace）。

### 3）鉴权（Token）的收益与代价

- **收益**
  - **无状态/易扩展**：服务多实例/多机器，不依赖粘性会话。
  - **前后端分离友好**：客户端保存 token，用 header 带上即可。

- **代价**
  - **注销与过期**：需要 token 失效机制（登出、过期时间）。
  - **泄漏风险**：token 被窃取相当于被登录（需 HTTPS、最小权限、过期策略）。
  - **刷新策略**（可选）：短 token + refresh token，会增加复杂度（本周先不做）。

---

## Day 197：最小流程图（文字）

以“发表评论 → 通知作者”为例：

1. 客户端 `POST /api/posts/{id}/comments`（带 token）
2. 服务端校验登录 → 写 `comments` 表（DB）
3. **publish MQ**：发送 `CommentCreated` 事件（包含 postId/commentId/actorId/ownerId 等）
4. consumer 收到消息：
   - **幂等检查**（dedup_key UNIQUE 或先查）
   - 写入 `notifications` 表（DB）
5. 作者客户端未来可以 `GET /api/notifications` 拉取（Day201 后再做）

---

## Day 197：事件结构设计（字段）

原则：
- 事件要能**自解释**（谁对谁做了什么）
- 能支撑幂等（必须有稳定的 dedup_key 来源）
- 不把“可变数据”塞太多（payload 可放少量快照）

### 1）CommentCreated

- `eventId`: UUID（用于排查/日志关联）
- `eventType`: `"CommentCreated"`
- `occurredAt`: ISO 时间串
- `postId`: long
- `commentId`: long（**推荐**：作为幂等主键来源）
- `actorId`: long（评论者）
- `ownerId`: long（帖子作者）
- `payload`（可选快照）：
  - `commentPreview`: string（前 50～100 字）
  - `postTitle`: string（可选）

### 2）PostLiked

（点赞幂等的特点：同一 actor 对同一 post 多次 like 只算一次）

- `eventId`: UUID
- `eventType`: `"PostLiked"`
- `occurredAt`: ISO 时间串
- `postId`: long
- `actorId`: long（点赞者）
- `ownerId`: long（帖子作者）
- `payload`（可选快照）：
  - `postTitle`: string（可选）

> 取消点赞 `PostUnliked`（可选），本周若做通知可以同理。

---

## Day 197：通知表最小字段 + dedup_key 方案

目标：consumer **重复消费**同一事件时，**不会插入两条通知**。

### notifications（最小）

- `id` bigint PK
- `user_id` bigint（通知接收者：ownerId）
- `type` varchar（`COMMENT_CREATED` / `POST_LIKED`）
- `ref_id` bigint（commentId 或 postId）
- `payload` text/json（可选）
- `created_at` datetime
- `dedup_key` varchar UNIQUE（**幂等关键**）

### dedup_key 设计

- **CommentCreated**：`comment:{commentId}`
  - 理由：commentId 在 DB 唯一且稳定，重复投递同一评论事件不会重复插入
- **PostLiked**：`like:{postId}:{actorId}`
  - 理由：同一人对同一帖的 like 在业务上幂等；重复消息不会重复通知

---

## Day 197：我能 3 分钟讲清的版本（验收口径）

- **为什么用 MQ**：通知是副作用，异步能解耦 + 削峰；即使通知服务挂了也不该影响评论/点赞主链路。
- **为什么不直接同步写通知表**：会增加写接口延迟和失败面；通知逻辑变化会反复改主链路；高并发下 DB 压力更大。
- **怎么保证不重复通知**：consumer 落库时用 `dedup_key UNIQUE` 做幂等，重复消费插入会冲突，从而不产生第二条通知。

---

## Day 200：RabbitMQ 最小落地（MQ 冒烟）

- Compose：新增 `rabbitmq:3-management`（AMQP 5672、UI 15672）
- Boot：新增 `spring-boot-starter-amqp`
- dev 冒烟接口：`POST /api/dev/mq-test` 发消息到：
  - exchange: `bootsocial.test.direct`
  - routingKey: `test`
  - queue: `bootsocial.test.queue`
- consumer：`@RabbitListener` 打印 `mq_test_consume ...`

验收：

```bash
cd boot-social-demo
docker compose up -d --build
curl -sS -X POST http://127.0.0.1:8081/api/dev/mq-test \
  -H 'Content-Type: application/json' \
  -d '{"text":"hello mq"}'
docker compose logs -n 50 app | grep mq_test_consume || true
```

---

## Day 201：评论/点赞 → MQ → 通知落库（幂等）

落地目标：把“通知作者”从主链路拆出去：评论/点赞接口只管业务成功，通知异步落库。

- 表：`notifications(id, user_id, type, ref_id, payload, created_at, dedup_key UNIQUE)`
- 事件：
  - `CommentCreated(eventId, occurredAt, postId, commentId, actorId, ownerId, commentPreview)`
  - `PostLiked(eventId, occurredAt, postId, actorId, ownerId)`
- 幂等（至少一次投递下避免重复通知）：
  - `comment:{commentId}`
  - `like:{postId}:{actorId}`

验收口径：
- 同一消息重复消费不会插两条（靠 `dedup_key UNIQUE` + `INSERT IGNORE`）。

---

## Day 202：失败与补偿（固定次数重试 + DLQ + replay）

失败场景（示例）：consumer 抛异常、DB 短暂超时、网络抖动等。

最小可行策略：
- **有限重试**：例如最多 3 次（避免无限重试风暴）
- **DLQ**：超过重试次数仍失败 → 进入死信队列，等待人工/脚本补偿
- **replay**：把 DLQ 消息重新投回原 exchange/routingKey

本项目最小演示：
- notify queue：`bootsocial.notify.queue`
- DLX：`bootsocial.notify.dlx`
- DLQ：`bootsocial.notify.dlq`
- dev：
  - `GET/POST /api/dev/mq/failure?enabled=true|false`：模拟失败开关
  - `GET /api/dev/dlq/peek`：查看一条 DLQ（排障）
  - `POST /api/dev/dlq/replay?limit=10`：重放

能讲清的两句：
- **为什么要 DLQ**：隔离失败消息，避免阻塞正常消费，并给补偿提供入口。
- **为什么不能无限重试**：会产生重试风暴，把 MQ/DB/CPU 打满，导致雪崩。

---

## Day 203：收口（文档 + smoke 脚本）

- README 补齐：Token 鉴权、MQ 一键启动、评论/点赞触发通知、DLQ/replay 说明
- 新增脚本：`boot-social-demo/smoke-w29-token-mq.sh`
  - 一键跑通：注册→发帖→评论/点赞→通知落库（包含 DLQ + replay）

