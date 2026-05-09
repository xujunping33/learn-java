# W33-notes（双系统项目实战-1）

## Day 225：链路 / 边界 / 状态机 / 接口清单（先写死）

### 1) 目标链路（最小可用）

1. **create order**（C 端）  
   用户创建订单，订单进入 `CREATED`（待支付）
2. **create payment**（C 端）  
   为订单创建支付单，支付单进入 `INIT`
3. **confirm payment**（模拟三方回调）  
   支付单进入 `SUCCESS`
4. **order marked PAID**（同步更新，W33 用 Feign）  
   order-service 把订单状态更新为 `PAID`
5. **report query**（B 端只读）  
   查询日报：订单数/已支付笔数/已支付总金额

### 2) 服务边界（谁拥有谁的状态）

- **order-service**
  - 拥有：`orders.status`
  - 允许修改订单状态的入口：
    - 创建订单：`CREATED`
    - 支付成功回调：`PAID`（由 payment-service 调用“mark paid”接口触发）
- **payment-service**
  - 拥有：`payments.status`
  - 允许修改支付状态的入口：
    - 创建支付：`INIT`
    - 确认支付：`SUCCESS`（幂等）
- **report-service**
  - 只读聚合，不修改业务状态

### 3) 状态机（最小版）

- **order.status**
  - `CREATED` → `PAID`
  - `CREATED` → `CANCELLED`（W33 可不做，先留扩展点）
  - 约束：`PAID` 不可回退

- **payment.status**
  - `INIT` → `SUCCESS`
  - `INIT` → `FAILED`（W33 可不做，先留扩展点）
  - 约束：`SUCCESS` 不可回退；重复 confirm 必须幂等

### 4) 接口清单（Day225 定稿，后面不随便改）

- **user-service**
  - `GET /api/users/{id}` → `{id, username}`

- **order-service**
  - `POST /api/orders`  
    入参：`userId, amount`  
    出参：`orderId, status`
  - `GET /api/orders/{id}` → `order detail`
  - `POST /api/orders/{id}/mark-paid`（内部接口，供 payment-service 调用）  
    入参：`paymentId`（用于幂等/追踪）

- **payment-service**
  - `POST /api/payments`  
    入参：`orderId, userId, amount`（W33 先直接传 amount，后面可改为查 order）  
    出参：`paymentId, status`
  - `POST /api/payments/{id}/confirm`（幂等）  
    行为：payment → `SUCCESS`，然后 Feign 调 order-service `mark-paid`

- **report-service**
  - `GET /api/reports/daily?date=YYYY-MM-DD` → `{ordersCount, paidCount, paidAmountSum}`

### 5) 约束与约定（为 Day229 幂等提前留好）

- **幂等键建议**
  - `payments.confirm_request_id`（可选，W33 可先用 `payments.status` 幂等）
  - `orders.paid_payment_id`（推荐，防止重复更新/重复支付）
- **唯一约束**
  - 一个订单最多一个成功支付：`unique (orders.paid_payment_id)`（paid 时写入）
  - 一个订单最多一个支付单（W33 简化）：`unique (payments.order_id)`（可选）

