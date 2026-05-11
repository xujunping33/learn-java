# 第 33 周学习计划：双系统项目实战-1（把微服务方案跑进真实业务链路）

> 对应总纲：**第33–34周（双系统项目实战）**  
> 目标：把你在 W32 学到的微服务组合拳，放进一个“更像真实业务”的项目里。  
> 本周交付重点：至少完成 **“用户 → 订单 → 支付 → 报告查询”** 的 **1 条完整后端链路**（先做最小可用版本）。  
> 时间：约 **2 小时/天**；本周 **Day 225 ~ Day 231**。  

---

## 本周项目定义（双系统 = 两个入口/两类角色）

为了更贴近企业项目，这里把“系统”按入口拆成两块：

- **C 端系统（用户端）**：用户下单、支付、查自己的订单
- **B 端系统（运营/报表端）**：查询报表（订单/支付汇总），对外只暴露只读接口

> 你不需要做前端页面：我们用 **REST API + smoke 脚本** 完成交付。

---

## 技术栈约束（沿用你已学过的）

- **注册发现**：Nacos
- **调用**：Feign
- **统一入口**：Gateway
- **保护**：Sentinel（至少保护 1 个热点接口）
- **数据库**：MySQL（每个服务独立 schema 或独立表前缀，先做到逻辑隔离即可）
- **消息（可选）**：RabbitMQ（W34 再把支付/通知异步化更自然）

---

## 目录建议（不要把 `boot-social-ms` 改乱）

新建：`dual-system-ms/`（Maven multi-module）

- `ds-gateway/`
- `ds-user-service/`（最小：用户查询/鉴权可复用你已有做法）
- `ds-order-service/`（下单/订单查询）
- `ds-payment-service/`（模拟支付：创建支付单、回调/确认支付）
- `ds-report-service/`（报表查询：从 order/payment 聚合）
- `deploy/compose.yaml`（nacos + mysql + gateway + 各服务）

> 本周如果时间紧，`report-service` 可以先做“从自己的库聚合”（W34 再做跨服务更合理的聚合/异步快照）。

---

## 领域模型（最小版，够跑通链路）

### user
- `users(id, username, password_hash, created_at)`

### order（订单）
- `orders(id, user_id, amount, status, created_at)`
- `order_items(id, order_id, sku, qty, price)`（可选：不做也行）

状态建议：
- `CREATED`（已创建待支付）
- `PAID`（已支付）
- `CANCELLED`（取消）

### payment（支付单）
- `payments(id, order_id, user_id, amount, status, paid_at, created_at)`

状态建议：
- `INIT`（已创建）
- `SUCCESS`（支付成功）
- `FAILED`（失败）

---

## Day 225（周一）— 需求/边界/数据库：先把“链路”画对

**学什么**

- 服务边界与数据边界：谁拥有订单状态？谁拥有支付状态？谁能改？

**做什么**

1. 写 `W33-notes.md`：画出 1 条链路（文字流程即可）：
   - `create order` → `create payment` → `confirm payment` → `order marked PAID` → `report query`
2. 建表 SQL（放到 `dual-system-ms/deploy/mysql/init.sql`）：
   - 最少 `users/orders/payments`
3. 明确接口清单（先写出来，不要一边写一边改来改去）：
   - user：`GET /api/users/{id}`
   - order：`POST /api/orders`、`GET /api/orders/{id}`
   - payment：`POST /api/payments`、`POST /api/payments/{id}/confirm`
   - report：`GET /api/reports/daily?date=YYYY-MM-DD`（最小聚合）

**验收**

- 你能讲清：订单与支付的状态机；并且 SQL 能一键建表。

---

## Day 226（周二）— 工程骨架：Nacos + Gateway + 3 服务能启动

**做什么**

1. 建 `dual-system-ms/` multi-module。
2. 起 Nacos（compose）。
3. 起 `ds-order-service` / `ds-payment-service` / `ds-gateway`，都注册到 Nacos。
4. Gateway 配路由：
   - `/api/orders/**` → order
   - `/api/payments/**` → payment

**验收**

- 通过 gateway 能分别访问两个服务的 `/ping` 或 health。

---

## Day 227（周三）— 下单：order-service（写入 + 查询）

**做什么**

1. order-service 接 MySQL（MyBatis 或 MP 均可，别混用太多风格）。
2. `POST /api/orders`：
   - 入参：`userId, amount`
   - 出参：`orderId, status`
3. `GET /api/orders/{id}`：返回订单详情。

**验收**

- smoke：创建订单 → 查询订单，状态为 `CREATED`。

---

## Day 228（周四）— 支付：payment-service（创建支付单 + 确认支付）

**做什么**

1. `POST /api/payments`：
   - 入参：`orderId, userId`
   - 创建 payment，状态 `INIT`
2. `POST /api/payments/{id}/confirm`：
   - 模拟第三方回调：把 payment 标记为 `SUCCESS`
3. 关键点：支付成功后 **需要通知 order-service** 更新订单为 `PAID`（两种做法二选一）：
   - **A（本周先用）**：payment-service Feign 调 order-service 的“标记已支付”接口（同步）
   - **B（W34 再做）**：MQ 事件异步更新（更可靠，但这周先别展开）

**验收**

- 确认支付后，订单状态变为 `PAID`（通过 order 查询验证）。

---

## Day 229（周五）— 可靠性第一步：幂等 + Sentinel 保护热点接口

**学什么**

- 分布式里“重复请求”很常见：支付确认必须幂等。

**做什么**

1. 支付确认幂等：
   - 同一个 payment 多次 confirm，不应重复更新/重复报错
2. Sentinel：
   - 对 `GET /api/orders/{id}` 或 `POST /api/payments/{id}/confirm` 设 1 条规则（限流/熔断任选其一）
   - 触发时返回统一错误体（`RATE_LIMITED` 或 `DEGRADED`）

**验收**

- 重复 confirm 行为稳定；限流规则可复现触发。

---

## Day 230（周六）— 报表：report-service（最小可用聚合）

**做什么（两选一，别纠结）**

### 方案 A（更快落地）
- report-service 直连同一个 MySQL（只读）做聚合查询（演示用，承认 trade-off）

### 方案 B（更像微服务）
- report-service 分别 Feign 调 order-service/payment-service 拿数据聚合（接口调用多、性能一般，但更符合边界）

**报表接口建议**
- `GET /api/reports/daily?date=...` 返回：
  - `ordersCount`
  - `paidAmountSum`
  - `paidCount`

**验收**

- 有真实数据后报表能查出正确汇总；写进 README：你选 A/B 的 trade-off。

---

## Day 231（周日）— 收口：一键启动 + smoke 脚本 + 演示稿

**做什么**

1. `deploy/compose.yaml`：
   - nacos + mysql + gateway + order + payment (+ report)
2. `smoke-w33-dual-system.sh`：
   - 下单
   - 创建支付
   - confirm 支付（重复 confirm 验证幂等）
   - 查订单状态为 PAID
   - 查日报表
3. README：
   - 启动顺序
   - 端口/路由
   - sentinel 规则如何触发
4. `W33-notes.md`：写 1 页讲稿（链路、边界、trade-off、下一步要做什么）。

**验收**

- 10 分钟内可演示全链路；别人照 README 可复现。

---

## 本周交付清单（必须）

- `dual-system-ms/`（至少 gateway + order + payment；report 可选但强烈建议）
- Nacos 注册发现 + Gateway 路由
- Feign 调用（支付确认更新订单）
- Sentinel 保护一个接口
- 幂等（至少支付确认）
- compose + smoke + README + notes

---

## 现在开始：Day 225

今天先把“链路与边界”定死：

1. 写出链路步骤（下单→支付→确认→订单PAID→报表）。  
2. 写建表 SQL（users/orders/payments）。  
3. 把接口清单写成 bullet list。  

做完把你的 **接口清单 + 状态机**发我，我可以帮你快速 review：哪些字段缺了会导致后面痛苦（比如幂等键、状态枚举、时间字段、唯一约束）。  

