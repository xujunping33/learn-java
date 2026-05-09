# W34 双系统项目实战-2 — 笔记（从 Day 232 起）

## Day 232 — 复盘 W33：痛点与本周必修

### 一、当前实现（W33）的 5 个痛点

1. **支付 → 订单强同步耦合**  
   `payment-service` 在 `confirm` 里用 **Feign 同步** 调 `order-service` 标记 `PAID`。真实环境里：订单服务短暂不可用、超时重试、网络抖动都会导致支付侧逻辑变复杂，且拉长确认接口耗时。

2. **报表口径未在接口层「写死说明」**  
   `ds-report-service` 的 `GET /api/reports/daily` 已能聚合，但「按下单日 vs 按支付成功日」、时区（UTC）与跨库只读的 trade-off 尚未在对外契约里写清楚，运营/对接方容易误读数字。

3. **无消息队列，缺少异步解耦与削峰**  
   支付成功后的「通知订单」没有事件总线；无法自然延伸重试、死信、审计流水，也与「最终一致性」的常见落地形态不一致。

4. **可观测性不足**  
   Gateway 与各服务尚未统一 **Trace / Correlation Id**（如 `X-Request-Id` 进 MDC），排障时难以按一次用户操作 grep 全链路日志。

5. **双系统入口未区分「C 端 vs B/报表端」**  
   报表路径与业务路径同属 `/api/reports/**` 等，缺少最小 **B 端鉴权**（如 `X-Report-Key`），网关层也未按角色做路由/过滤隔离。

---

### 二、本周必修（对应 W34_PLAN）

| 必修 | 内容 | 状态 |
|------|------|------|
| **必修 1** | MQ 异步更新订单（`PaymentSucceeded` → order 消费，消费端幂等） | 本周实施 |
| **必修 2** | 报表口径说明 + README/接口注释（下单日 vs 支付成功日、UTC） | 本周实施 |
| **必修 3** | 最小「报表 / B 端」鉴权（Gateway 或 report 校验 API Key） | 本周实施 |

---

### 三、支付成功后「更新订单为 PAID」主路径选择（避免双写）

**结论：主路径仅走 MQ，confirm 内不再调用同步 Feign `markPaid`。**

| 方案 | 说明 |
|------|------|
| ~~MQ + 同步 Feign 并行~~ | **不采用**：同一笔支付可能触发两次「置 PAID」，即便有 SQL 条件更新，也会增加冲突日志、顺序与幂等设计成本。 |
| **仅 MQ（推荐）** | `confirm` 只负责：DB 将 payment 置 `SUCCESS`（及现有 `order_marked_paid_at` 语义可收敛为「已发事件」或与 Outbox 对齐）；**发布 `PaymentSucceeded` 事件**；由 **order-service 消费** 幂等更新订单。支付侧不再直连订单写状态。 |
| 仅同步 Feign | 与本周「必修 1」冲突，作为 **降级/应急** 可在 README 中一笔带过（例如 MQ 全挂时的手工运维），不作为默认主路径。 |

**用约 3 分钟能讲清的「为什么真实环境常见异步更新订单」**

- 支付通道回调往往 **重试、乱序、超时**；同步调订单会把「支付域」和「订单域」绑在同一请求生命周期里。  
- 订单服务扩容、发布、限流时，**不应反压支付确认**；用事件解耦后，确认可尽快返回，订单最终一致由消费者重试保证。  
- 统一用 **事件 + 幂等键**（如 `eventId`）可在多副本消费者下保证「只生效一次」，比散落在多处的同步补偿更易审计。

---

### 四、`PaymentSucceeded` 事件字段草稿（对齐幂等与队列命名）

**建议队列/交换机命名（团队习惯，可微调）**

- Exchange：`dual.payment.topic`（type: topic）  
- Routing key：`payment.succeeded`  
- Queue：`dual.order.payment-succeeded`（bind `payment.succeeded`）

**JSON Payload（草稿）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `eventId` | string (UUID) | **幂等键**，全局唯一，消费者 `processed_events` 或 UNIQUE 约束去重 |
| `dedupKey` | string | 可选；可与 `eventId` 相同，或与 `paymentId` 绑定（二选一在实现里定死） |
| `paymentId` | long | 支付单主键 |
| `orderId` | long | 订单主键 |
| `userId` | long | 用户 |
| `amount` | string/decimal | 与支付单一致，便于对账 |
| `paidAt` | string (ISO-8601) | 支付成功时间（建议 UTC） |
| `schemaVersion` | int | 例如 `1`，便于演进 |

**与现有库表对齐**

- `payments` 已有 `order_marked_paid_at`（W33 用于「已通知订单」补偿）；切到 **仅 MQ 主路径** 后，建议语义改为「已写入 outbox / 已发布事件」或与 Day235 Outbox 表合并，避免与「订单是否 PAID」双重含义混淆——在 Day233/234 编码时再定一版迁移说明。

---

### 五、下一步（Day 233）

- `compose` 增加 RabbitMQ。  
- `payment-service`：`confirm` 成功后 **publish** 上述事件（并实现主路径去掉同步 Feign）。  
- README：写清 exchange/queue/routing key 与 `eventId` 规则。

---

## Day 236 — 分布式事务：Seata vs Saga（概念 + 选型）

### 一、若在本项目（dual-system-ms）引入 Seata，要解决什么、代价是什么

**想解决的「问题」**（心理预期）  
- 希望 **支付库 `ds_payment` 与订单库 `ds_order` 在同一次用户操作里「像单机事务一样」要么都提交、要么都回滚**，避免出现：钱扣了/支付单成功了，但订单一辈子 `CREATED`（或反过来）。  
- 或希望用 **AT 模式** 把多数据源更新包进一个 `@GlobalTransactional`，减少手写补偿与对账。

**实际代价与不适配点**  
- **运维与耦合**：需部署 **Seata Server（TC）**，各服务集成 **RM/TM**，与 Spring Cloud、数据源代理、连接池、版本矩阵绑定；双系统 demo 变「先起 Seata 再谈业务」。  
- **模型与当前架构冲突**：当前主路径是 **Outbox + MQ + 订单侧幂等 inbox**，本质是 **最终一致**；Seata AT 偏 **同步协调、长事务锁与undo**，和「支付确认尽快返回、订单异步追上」的目标相反。  
- **跨消息边界**：一旦 **发 MQ** 或调外部支付渠道，经典 2PC 很难把「外部世界」也纳进同一原子边界；仍要 **幂等 + 对账**，Seata 不能消灭这些工作。  
- **故障面**：TC 不可用、网络分区时，全局事务 **悬挂、超时、人工介入** 成本上升；小团队维护不如 **明确状态机 + 补偿任务** 清晰。

**小结**：在本项目里，Seata 更像「为强一致多库写路径买的重型装备」；当前业务边界用 **订单状态机 + `PaymentSucceeded` + 幂等表 + Outbox** 已能表达 **支付成功 → 订单 PAID** 的最终一致，**不必为 demo 上 Seata**。

---

### 二、Saga 补偿示例：支付失败 / 未成功时如何「取消订单」（概念）

**编排式思路（顺序 + 补偿函数）**

1. **Forward**：`CreateOrder` → 订单 `CREATED`。  
2. **Forward**：`CreatePayment` → 支付单 `INIT`。  
3. **Forward**：`ConfirmPayment`（调渠道或内部确认）  
   - **若成功**：走现有事件链 → 订单 `PAID`（无需 Saga 回滚整单）。  
   - **若失败或超时判失败**：进入 **Compensate**（按相反顺序调用可逆操作）：

| 步骤失败点 | 补偿动作（示例） |
|------------|------------------|
| 确认支付失败，但支付单已误标 SUCCESS（极少，需对账发现） | `ReversePayment` / 标记支付 **失败** + 人工退款流程（真实世界常不是自动 SQL 回滚） |
| 支付单仍是 INIT 或已关单 | `CancelPayment`：支付单置 **CANCELLED**（或保持 INIT 不再确认） |
| 需要释放订单占用的资源 | `CancelOrder`：订单从 `CREATED` → **CANCELLED**（仅当业务允许未支付取消；若已 PAID 则不能简单 cancel，要走退款 Saga） |

**要点（面试可讲）**  
- Saga **不保证**每一步的中间状态对外不可见，只保证 **补偿之后业务上自洽**。  
- 每个 forward 步骤必须 **幂等**；补偿也要 **幂等**（重复 cancel 仍是 cancel）。  
- 与 **2PC** 对比：Saga **不做全局锁死等多库同时提交**，用 **业务补偿** 换 **可用性与扩展性**。

---

### 三、什么时候才「值得上 Seata」（验收话术）

**值得认真评估 Seata（或类似 XA/强一致方案）的典型信号**  
- **短事务、少参与者、同数据中心**：例如多个内部服务但共享 **强一致会计分录**，监管或审计要求 **近似同步** 的多账套落账。  
- **团队已有中间件治理力量**：能盯 TC 高可用、监控、升级与故障演练。  
- **确实无法**用 **状态机 + 对账 + 幂等** 讲清业务闭环，且数据错误成本 **远高于** 引入 Seata 的成本。

**多数互联网订单/支付链路**（含本项目）  
- 更常见答案是：**事件驱动 + Outbox + 消费者幂等 + 定时对账/补偿**，而不是默认上 Seata。

---

### 四、本周结论（与 W34_PLAN 对齐）

- **2PC / Seata**：强一致倾向，**运维与耦合成本高**；适合范围可控、团队能承载的场景。  
- **Saga**：长流程、**分步提交 + 补偿**，与 **MQ、异步** 一致；要单独设计 **补偿语义与幂等**。  
- **本项目**：**「订单状态机 + 事件 + 幂等（+ Day235 Outbox）」** 优先；Seata 标为 **了解即可**，面试能说出 **何时才值得上** 即可。

---

## Day 237 — 报表口径（文档化）+ B 端 Gateway 鉴权

### 口径结论（与实现对齐）

- **`ordersCount`**：**下单日** — `ds_order.orders.created_at` 落入参数 `date` 的 **UTC** 日界 \[00:00, 次日 00:00)。  
- **`paidCount` / `paidAmountSum`**：**支付成功日** — `ds_payment.payments` 满足 `status = SUCCESS` 且 `paid_at` 非空，且 `paid_at` 落在 **同一 UTC 日界**。  
- 两组数字 **不可混读为「同一维度」**；跨自然日支付会造成「今天下单、明天才记 paid」等差异。

### Gateway 行为

- **`X-Report-Key`**：仅 **`ds-gateway`** 校验；变量 **`REPORT_API_KEY`** → `report.api-key`。Compose 默认 **`dev-report-key`**，与 **`smoke-w33-*.sh`** 默认一致。  
- **`REPORT_API_KEY` 留空**：网关不拦截报表路径（本地开发可选）；生产应 **必选强 key**。  
- **可选前缀** `/api/admin/reports/**`：Rewrite 到 report-service 的 `/api/reports/**`，语义上与 C/B 路径区分，后续可再拆路由或独立鉴权策略。

### 已知局限（诚实写进简历/答辩）

- **直连 `report-service:9084`** 可绕过 key，属 **demo**；生产网络层应只信任来自网关的流量（或 report 侧再加一道鉴权）。

---

## Day 238 — 可观测 + smoke 收口（演示）

### 本周 3 个亮点（可写简历/述职）

1. **支付成功后订单最终一致**：`payment_outbox` 同事务 **`PaymentSucceeded`** 异步投递 RabbitMQ，`order-service` **`processed_payment_events`** 幂等消费订单 **`PAID`**，避免同步 Feign 与「库成功 MQ 未到」的常见坑。  
2. **报表双维度说清楚**：下单日 **`ordersCount`** vs 支付成功日 **`paidCount`** / **金额**，UTC 日界写入 JavaDoc/README，降低运营误读口径风险。  
3. **网关最小 B 端能力**：**`X-Report-Key`** + 可选 **`/api/admin/reports`** 前缀 + **`X-Request-Id`** 贯穿网关与 Servlet 日志 **`[req=…]`**，排障链路可复述。

### 3 个已知局限（答辩时主动说）

1. **`X-Request-Id` 不进 MQ consumer 线程**：订单消费 **`PaymentSucceededListener`** 的日志未必带浏览器同一次请求的 MDC（异步边界）；要严格 e2e trace 需 OpenTelemetry / W3C traceparent 或对消息带出 trace id（未做）。  
2. **报表鉴权仅在网关**：直连 **`9084`** 仍可读库聚合，仅能靠网络隔离兜底；未到生产级 IAM/审计日志。  
3. **演示向组件**：Sentinel 本地 QPS、固定 **`dev-report-key`**、单 Rabbit 队列无 DLQ 深度治理；与真实支付对账、多活仍有距离。

### 收口脚本与验收提示

- 运行 **`./smoke-w34-dual-system.sh`**：全流程 + **confirm ×3** + 报表 **`X-Report-Key`** + **`X-Request-Id`** 固定在脚本输出便于 **grep**。  
- **README**：已补 ASCII 架构、端口表、环境变量表（Day238）。
