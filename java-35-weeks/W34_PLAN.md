# 第 34 周学习计划：双系统项目实战-2（异步化 / 一致性意识 / 报表深化 / 上线收口）

> 对应总纲：**第33–34周（双系统项目实战）** 中的 **第34周**。  
> 承接 **`dual-system-ms`**：在 W33「下单→支付→确认→报表」跑通的基础上，把链路做成更像真实业务的形态：**异步事件、更好的报表口径、可观测与交付**。  
> 时间：约 **2 小时/天**；本周 **Day 232 ~ Day 238**。  
>
> **边界**：分布式事务（Seata）可作为 **概念日（Day 236）**；动手默认优先 **Outbox + MQ** 或 **最终一致性**，避免一周卡在重量级组件上。

---

## 本周总目标

- **异步解耦**：支付成功不再只靠同步 Feign（保留也行），增加 **MQ 事件**：`PaymentSucceeded` → order-service 消费 → 订单置 `PAID`（与同步路径二选一做主路径，另一路径降级说明）。
- **幂等与可靠性**：消费端幂等 +（可选）失败重试/DLQ；与 W30 Outbox 思路对齐。
- **报表口径**：明确「按下单日 vs 按支付成功日」聚合差异；报表接口文档写清楚维度。
- **双系统入口**：Gateway 区分「用户端路由」与「报表/运营端路由」（路径前缀或单独 filters）；最小鉴权（API Key / 固定 token，够用即可）。
- **可观测**：请求链路 **trace/correlation id**（日志或 MDC）；关键步骤结构化日志。
- **交付**：`compose` 一键起全套；`smoke-w34-dual-system.sh`；`README` + `W34-notes.md`。

---

## 前置（来自 W33）

- `dual-system-ms` 已具备：`orders`、`payments`、Gateway、Nacos、至少一条支付→订单更新路径。
- 若 W33 未完成：本周前两天仍以「补齐最小链路」为主，再叠加本章增量。

---

## Day 232（周一）— 复盘 W33：列出「必须升级的 3 个点」

**做什么**

1. `W34-notes.md`：写下当前实现的 5 个痛点（例如：同步调用耦合、报表口径不明、无 MQ、无 Trace、B 端未隔离）。
2. 选定本周必修：
   - **必修 1**：MQ 异步更新订单（或 Outbox 投递）
   - **必修 2**：报表口径说明 + 接口注释/README
   - **必修 3**：最小「报表/B 端」鉴权

**验收**

- 你能用 3 分钟说明：为什么异步更新订单在真实环境常见。

---

## Day 233（周二）— RabbitMQ：`PaymentSucceeded` 事件契约

**学什么**

- 事件字段：`paymentId`、`orderId`、`userId`、`amount`、`paidAt`、`eventId`（幂等键）。
- Exchange/Queue 命名约定（团队级习惯）。

**做什么**

1. `compose.yaml` 增加 RabbitMQ（若已有则复用）。
2. 定义 JSON payload 与 `eventId`/`dedupKey` 规则（写入 README）。
3. payment-service：支付确认成功后 **publish** 事件（若暂时保留同步 Feign，也可两者并行：MQ 为主，Feign 注释掉——避免双写冲突，二选一写文档）。

**验收**

- RabbitMQ 管理界面能看到消息流入队列。

---

## Day 234（周三）— order-service：消费事件 + 幂等更新订单

**学什么**

- 至少一次投递 → **消费者幂等**：`eventId` UNIQUE 表或 `processed_events`。

**做什么**

1. order-service 增加 `PaymentSucceededConsumer`：
   - 收到事件 → 若订单已是 `PAID` 则跳过 → 否则更新并记录幂等键。
2. 单元测试或集成测试至少覆盖：**重复消息不改变最终状态**。

**验收**

- 手工重复 publish（或重复调用 consumer 模拟）订单仍为单一终态。

---

## Day 235（周四）— Outbox（可选但强烈推荐）：支付与 MQ 同事务

**学什么**

- 「DB 提交成功但消息未发出」 vs Outbox：本地事务写 **payments + outbox**，异步投递。

**做什么**

1. `payment_outbox` 表：`id, payload_json, status, retry_count, created_at`。
2. `@Scheduled` 扫描投递；成功后标记。
3. README：对比「不用 Outbox 的风险」一段话。

**验收**

- 模拟 MQ 短暂不可用：支付记录仍一致；恢复后能补发。

---

## Day 236（周五）— 分布式事务：Seata vs Saga（概念 + 选型）

**学什么**

- **2PC/Seata**：强一致倾向，运维与耦合成本高。
- **Saga**：长事务拆分补偿；更适合跨系统业务流程。
- 本周结论：**你的场景更适合「订单状态机 + 事件 + 幂等」**，Seata 可作为「了解即可」。

**做什么**

1. `W34-notes.md` 写半页：本项目若引入 Seata，要解决什么问题、代价是什么。
2. （不写代码也可）画 Saga 补偿示例：支付失败如何取消订单。

**验收**

- 面试能答：**什么时候才值得上 Seata**。

---

## Day 237（周六）— 报表深化 + B 端路由与鉴权

**做什么**

1. **报表口径**：在 `GET /api/reports/daily` 文档中写明：
   - 统计的是 **支付成功时间** 还是 **下单时间**
   - 时区（先用服务器本地或 UTC，写死说明）
2. Gateway：
   - `/api/reports/**` 路由到 report-service
   - `filter`：报表路径校验 `X-Report-Key`（或 Sa-Token，沿用你栈）
3. user 端与 report 端路径前缀清晰：`/api/app/**` vs `/api/admin/reports/**`（按需重构，小步提交）。

**验收**

- 无 key 访问报表返回 401；带 key 返回数据。

---

## Day 238（周日）— 可观测 + smoke + 演示收口

**做什么**

1. **Correlation ID**：Gateway 生成或透传 `X-Request-Id`，各服务 MDC 打印。
2. `smoke-w34-dual-system.sh`：
   - 全流程走一遍 + **MQ 路径验证订单 PAID**
   - 重复事件不破坏状态
   - 报表 + B 端 key
3. `README`：架构图（ASCII 即可）、端口表、环境变量表。
4. `W34-notes.md`：本周 3 个亮点 + 3 个已知局限（诚实写在简历/面试里）。

**验收**

- 15 分钟演示：异步链路 + 报表鉴权 + 幂等；日志可按 request id grep。

---

## 本周交付清单（必须）

| 交付物 | 说明 |
|--------|------|
| MQ 事件 + order 消费幂等 | `PaymentSucceeded` 端到端 |
| （可选）Outbox | 同事务投递 |
| 报表口径文档化 | README/OpenAPI |
| B 端最小鉴权 | Gateway 或 report-service |
| trace/request id | Gateway + 日志 |
| `smoke-w34-dual-system.sh` | 覆盖核心场景 |
| `W34-notes.md` | 复盘与面试话术 |

---

## 与 W35 衔接（预览）

**W35** 高频面试：把你 **dual-system-ms + boot-social-demo** 的技术选型、链路、trade-off 写成「自己的答案」。

---

## 现在开始：Day 232

1. 打开 `W34-notes.md`，写下 W33 实现的 **5 个痛点**。  
2. 勾选本周 **必修 1～3**（MQ / 报表口径 / B 端鉴权）。  
3. 决定：**支付成功后**，主路径走 **仅 MQ** 还是 **MQ + 去掉同步 Feign**（只能保留一条更新订单的主路径，避免双写）。  

把「主路径选择」和事件字段草稿发我，我帮你对齐 **幂等表字段** 与 **队列命名**，避免和现有 payment confirm 逻辑打架。
