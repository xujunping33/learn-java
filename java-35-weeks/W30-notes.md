# W30 Notes（Day 204 ~ Day 210）

> 本周主题：分布式入门-2（boot-social-demo：Token + MQ + Redis 可靠性工程化）

---

## Day 204 — 复盘 W29：可靠性清单（每项 3 行）

### 1) 哪些地方可能重复？（at-least-once / 重投 / 重试）

- **可能重复的入口**：
- **重复会造成什么坏结果**：
- **我准备怎么兜住（幂等点/唯一约束/去重 key）**：

### 2) 哪些地方可能丢失？（发布前崩溃 / 网络问题 / ACK）

- **可能丢失的入口**：
- **丢失会造成什么坏结果**：
- **我准备怎么兜住（Outbox/重放/补偿）**：

### 3) 哪些地方需要幂等？（尤其写库、写通知、点赞/关注等）

- **必须幂等的业务点**：
- **幂等 key（dedup_key / idempotency-key）怎么设计**：
- **幂等落点（UNIQUE/幂等表/Redis）**：

### 4) 失败如何补偿？（DLQ / 失败表 / 重放脚本）

- **失败会发生在哪里**：
- **失败后如何“可追踪”**：
- **我准备怎么重放/补偿**：

---

## Day 204 — 本周选择（今天必须定下）

### A. 本周优先补齐的“可靠性 2 个坑”

- **坑 1**：
  - why：
  - scope（影响哪些接口/链路）：
- **坑 2**：
  - why：
  - scope：

### B. Day 208：Redis 三选一

- **我选**：A 限流 / B 幂等 token / C 分布式锁
- **落地到哪个接口/业务点**：
- **规则（示例）**：

---

## Day 205 ~ Day 210 记录区（后面每天补）

### Day 205

- done：
  - 统一 401/403 错误码（`AUTH_REQUIRED` / `AUTH_EXPIRED` / `AUTH_INVALID_TOKEN` / `FORBIDDEN`）
  - `/api/auth/login` 登录失败返回 `AUTH_INVALID_CREDENTIALS`
  - README + Swagger 文档补齐 token 策略（timeout/active-timeout/logout）
- blockers：
- notes：

### Day 206

- done：
  - 加表 `outbox_events`（`deploy/mysql/init.sql` + `testcontainers-schema.sql`）
  - 写路径（评论/点赞）事务内写 outbox，不直接发 MQ
  - `OutboxPublisher(@Scheduled)` 轮询投递 MQ：成功标记 SENT；失败按 5s/30s/2m/10m/1h 重试
- blockers：
- notes：

### Day 207

- done：
  - 幂等键收口 `DedupKeys`（`comment:{id}` / `like:{postId}:{actorId}`）
  - `MqConsumers` 重复投递打日志 `notify_duplicate`
  - DLQ + replay 沿用 `DevDlqController`，README 写明闭环
- blockers：
- notes：

### Day 208

- done：
  - 选项 A：`WriteActionRateLimiter`（Redis INCR + 首次 EXPIRE），`POST …/like` 与 `POST …/comments`
  - 默认每帖每用户 10s 内 ≤3 次；超限 429 + `RATE_LIMITED`；Redis 宕机放行
  - `app.rate-limit.*` + README 说明
- blockers：
- notes：

### Day 209

- done：
  - 链路日志：`notify_chain_src` / `outbox` / `publish` / `consume`，字段含 eventId、dedupKey、postId、actorId 等
  - `NotifyConsumeMetrics` + `/actuator/info` → `notifyPipeline`
- blockers：
- notes：

### Day 210

- done：
  - `smoke-w30-reliability.sh`（happy path + 空闲 replay 幂等不变量 + FAIL_CONSUME→DLQ→replay）
  - README「W30 收口速查」表 + Day210 冒烟说明
  - 本节「演示稿」
- blockers：
- notes：

---

## Day 210 — 演示稿（约 10 分钟：改进前后对比）

**开场（1 分钟）**：同一单体服务里，引入 MQ 后会出现「写库成功但消息没发出去」「重复投递」「消费失败」三类典型问题；本周用 Outbox、幂等、DLQ、限流把链路做成「可解释、可补救」。

**改进前（口述对照旧代码/心智模型）**：评论成功后直接 `rabbitTemplate.send`，MQ 抖动则通知可能丢；消费不重试或无限重试都不好控。

**改进后 — 现场演示顺序建议**

1. **Compose 全套**，执行 `./smoke-w30-reliability.sh`（或分步 curl）：指出日志里同一条 **`eventId`** 依次出现 `notify_chain_src` → `outbox` → `publish` → `consume`。
2. **`/actuator/info`** 里 **`notifyPipeline`**：成功/失败消费计数（可与 `FAIL_CONSUME` 脚本对照）。
3. **故意失败**：评论含 `FAIL_CONSUME` → 重试后进 DLQ → `replay` 后通知落库；强调 **`dedup_key` 唯一**，重复 replay 不会重复插行。
4. **Outbox（可选加演）**：`docker compose stop rabbitmq` 后发评论 → MySQL `outbox_events` 堆积 → `start rabbitmq` 后变 `SENT`。
5. **限流（可选 1 分钟）**：同一帖连点 4 次点赞，第 4 次 **429 `RATE_LIMITED`**，`sleep 10` 后恢复。

**收尾**：可靠性不是「零故障」，而是 **丢不了（Outbox）、重复可控（幂等）、失败看得见并能重放（DLQ + 日志）**。
