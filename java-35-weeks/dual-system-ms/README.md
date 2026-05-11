## dual-system-ms (W33)

最小可用链路：**下单 → 支付 → 确认支付 → 订单变 PAID → 日报表查询**。

### 模块
- **`ds-gateway`**: `9080`（统一入口，基于 Nacos `lb://` 路由；**Day237** 对 `/api/reports/**`、`/api/admin/reports/**` 校验 **`X-Report-Key`**）
- **`ds-order-service`**: `9082`（订单写入/查询/标记已支付）
- **`ds-payment-service`**: `9083`（创建支付单 / 确认支付；**W34 Day235** 确认成功与 **`payment_outbox` 同事务**，定时投递 RabbitMQ `PaymentSucceeded`）
- **`ds-report-service`**: `9084`（只读聚合报表，直连 MySQL）

### 依赖
- **Nacos**：注册发现（host 端口 `8849`）
- **MySQL**：初始化脚本 `deploy/mysql/init.sql`（host 端口 `3307`，root/root）
- **RabbitMQ（W34）**：消息队列。Compose 默认把容器 **5672 → 宿主机 5673**、**15672 → 15673**（避免与本机已占用 `5672` 的 RabbitMQ 冲突）。管理界面：`http://localhost:15673`（`guest` / `guest`）。若本机用 `mvn` 直连 RabbitMQ，需 `export RABBITMQ_PORT=5673`。

### 架构示意（Day238，ASCII）

```
  Client
     |
     v
+------------+     Nacos        +------------------+     MySQL (ds_order)
| ds-gateway |<--- register --->| ds-order-service |<------------------------+
|   :9080    |     lb://       |      :9082       |                       |
+------------+                 +--------+---------+                       |
     |                                     | Rabbit consume                 |
     | lb://                               v                                |
     |                         dual.order.payment-succeeded                 |
     v                                                                      |
+-------------------+     JDBC      +-------------------+                 |
| ds-payment-service|<-------------->| ds_payment DB     |<----------------+
|      :9083        |               +---------+-----------+
+---------+---------+                           ^
          |                                     | JDBC (同一实例只读报表)
          | publish (after outbox)            |
          v                                     |
+-------------------+                          |
| RabbitMQ (:5673)* |                          |
+---------+---------+                          |
                                          +----+---------------------+
                                          | ds-report-service       |
                                          |      :9084               |
                                          +-------------------------+

* 宿主映射见下表；容器内仍为 5672。
```

### 端口（宿主 / 映射常用值）

| 服务 / 组件 | 说明 | 宿主端口（compose 默认） |
|-------------|------|---------------------------|
| `ds-gateway` | 统一入口 | **9080** |
| `ds-order-service` | 订单 API | **9082** |
| `ds-payment-service` | 支付 API | **9083** |
| `ds-report-service` | 报表 API（直连无 B-key） | **9084** |
| Nacos | 注册配置 | **8849**（映射容器 8848） |
| MySQL | 业务库 | **3307**（映射容器 3306） |
| RabbitMQ AMQP | 消息 | **5673**（映射容器 5672） |
| RabbitMQ 管理台 | HTTP | **15673** |

### 环境变量速查（常与 compose / 本地 `mvn` 共用）

| 变量 | 作用 | 典型值 |
|------|------|--------|
| `SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR` / `NACOS_ADDR` | Nacos 地址 | compose 内 `nacos:8848`；本机 `127.0.0.1:8849` |
| `ORDER_*` / `PAYMENT_*` / `REPORT_*` JDBC | 数据源 | 见各服务 `application.yml` 占位符 |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | 支付 / 订单连 MQ | compose 内 `rabbitmq`、`5672`；本机常 `5673` |
| `REPORT_API_KEY` | **网关**校验报表 **`X-Report-Key`** | `dev-report-key`（compose 默认） |
| `SENTINEL_LOCAL_QPS` | order 网关限流演示 | `0` 关闭；`1` 开启 |

### W34 Day238 — 可观测：`X-Request-Id`

- **网关**：入站无则生成 UUID；**透传或补齐** **`X-Request-Id`** 到下游；**响应头回显**同一值。网关为 WebFlux，默认不在控制台打 correlation（避免刷屏）；排查时可设 `logging.level.learn.java.dualsystem.gateway.CorrelationIdGatewayFilter=DEBUG`。
- **order / payment / report**：`CorrelationIdFilter` 写入 **`MDC` 键 `requestId`**；控制台日志格式 **`[req=…]`**（见各服务 `logging.pattern.console`）。
- **演示 grep**：跑一次 `./smoke-w34-dual-system.sh`，控制台会打印 **`X-Request-Id=`**；在各服务 stdout 搜索该 id。**注意**：MQ 消费线程不在原始 HTTP 请求内，consumer 日志**未必**含同一 `[req]`（属正常现象）。

### 本地运行（不使用 Docker 跑服务）

先启动依赖：

```bash
cd dual-system-ms/deploy
docker compose up -d
```

再分别启动各服务（不同终端）：

```bash
cd dual-system-ms
export NACOS_ADDR=127.0.0.1:8849
export RABBITMQ_HOST=127.0.0.1
export RABBITMQ_PORT=5673

mvn -pl ds-order-service spring-boot:run
mvn -pl ds-payment-service spring-boot:run
mvn -pl ds-report-service spring-boot:run
# 报表经网关时需与网关侧的 REPORT_API_KEY 一致（Compose 默认为 dev-report-key）
export REPORT_API_KEY="${REPORT_API_KEY:-dev-report-key}"
mvn -pl ds-gateway spring-boot:run
```

### W34 Day237 — 报表口径 + B 端鉴权（Gateway）

**`GET /api/reports/daily?date=YYYY-MM-DD` 口径（均为 UTC 日界）**

| 字段 | 时间维度 | 数据来源 |
|------|----------|----------|
| `ordersCount` | **下单日** | `ds_order.orders.created_at` |
| `paidCount` / `paidAmountSum` | **支付成功日** | `ds_payment.payments`，`status = SUCCESS` 且 `paid_at` 非空 |

同一 `date` 下「下单数」与「支付成功笔数」**无必然相等**（跨日支付、未支付订单等），对外说明见 `ReportController` JavaDoc。

**B 端最小鉴权（仅网关）**

- 请求头：`X-Report-Key: <与网关配置一致>`
- 网关配置：`report.api-key` / 环境变量 **`REPORT_API_KEY`**。`deploy/compose.yaml` 中 **`ds-gateway`** 默认 `REPORT_API_KEY=dev-report-key`（可用宿主机环境覆盖）。
- 若 **`REPORT_API_KEY` 未设置且留空**：网关**不**校验报表路径（便于本地未配 key 开发）；**生产务必设强随机 key**。
- 未带或带错 key：`401`，body 形如 `{"code":"UNAUTHORIZED","message":"missing or invalid X-Report-Key"}`。
- **可选路径前缀**：`GET /api/admin/reports/daily?...` 与 `/api/reports/daily` 等价（Gateway `RewritePath` 转发到 report-service）。
- **直连 `9084` 绕过网关**时不做此鉴权（demo）；生产应对 `ds-report-service` 做网络隔离，仅允许自网关访问。

### 一键 Docker Compose（包含服务）

> 需要先打包 jar（Dockerfile 会从 `target/*.jar` 复制）。

```bash
cd dual-system-ms
mvn -DskipTests package

cd deploy
docker compose up -d --build
```

### W34 Day233 — `PaymentSucceeded` 事件（RabbitMQ）

**命名约定**

| 类型 | 名称 |
|------|------|
| Exchange（topic） | `dual.payment.topic` |
| Routing key | `payment.succeeded` |
| Queue（支付侧声明，便于管理台看到堆积；订单消费在 Day234） | `dual.order.payment-succeeded` |

**JSON 消息体**

| 字段 | 说明 |
|------|------|
| `eventId` | UUID，**幂等键**；消费者按此去重 |
| `dedupKey` | 与 `eventId` 相同（可扩展为业务键） |
| `paymentId` / `orderId` / `userId` | 业务主键 |
| `amount` | 字符串十进制，与支付单一致 |
| `paidAt` | ISO-8601（UTC） |
| `schemaVersion` | 当前为 `1` |

**行为说明**

- 支付 `confirm` 成功后：在同一本地事务内将 payment 置 `SUCCESS`，并 **写入 `payment_outbox`（`PENDING`）**；`@Scheduled` 轮询投递到 RabbitMQ，成功后 outbox 置 `SENT` 且 **`payments.order_marked_paid_at`** 置为非空（表示「已成功发出 MQ 消息」，**不是**「订单已 PAID」）。
- **订单改 `PAID`**：由 `order-service` 的 **`PaymentSucceededListener`** 消费队列 `dual.order.payment-succeeded` 后，在事务内 `markPaid` + `processed_payment_events` 幂等落库。
- 已有 MySQL 数据卷时，需对 `ds_order` 执行一次建表（或重建卷）：见 `deploy/mysql/init.sql` 中 `processed_payment_events`。
- **Day235**：若数据卷在 Day235 之前已创建，请在 `ds_payment` 上补建 **`payment_outbox`**（见同脚本末尾 `CREATE TABLE payment_outbox`），否则支付确认会失败。
- 验收 Day233：启动 RabbitMQ 后走一笔支付确认，打开管理台 **Queues** → `dual.order.payment-succeeded` 可见消息入队（Outbox 下消息可能在轮询间隔后几毫秒～数百毫秒才入队，属正常）。

### W34 Day235 — 支付 Outbox（同事务 + 异步投递）

**目的**：避免「DB 已提交成功但 MQ 未发出」与「先发 MQ 再写 DB 失败」的两难：确认支付时 **只同事务写 `payments` + `payment_outbox`**，由调度器异步 `publish`。

**表 `payment_outbox`**（见 `deploy/mysql/init.sql`）

| 字段 | 说明 |
|------|------|
| `payment_id` | 对应支付单；`UNIQUE` 保证一笔成功只落一条 outbox |
| `payload_json` | `PaymentSucceeded` JSON（与 Day233 契约一致） |
| `status` | `PENDING` → 投递成功后 `SENT`；连续失败可变为 `FAILED` |
| `retry_count` | 投递失败累加；达到 `payment.outbox.max-retries` 后标记 `FAILED` |

**配置**（`ds-payment-service` `application.yml`）

- `payment.outbox.dispatch-interval-ms`：轮询间隔（默认 `250`）
- `payment.outbox.batch-size`：每批最多条数（默认 `50`）
- `payment.outbox.max-retries`：超过后标记 `FAILED`，需人工或脚本补发（默认 `120`）

**不用 Outbox 的风险（对比）**

若确认接口里 **先改库再同步调 MQ**：MQ 短暂宕机或网络抖动时，常见结果是 **支付状态已变 SUCCESS，但消息永远没发出**，订单侧长期收不到 `PaymentSucceeded`，只能靠人工对账或补偿任务扫「成功未发」的脏数据。Outbox 把「要发的内容」与支付成功 **同事务落库**，MQ 恢复后调度器可 **自动补发**；代价是多一张表、一次轮询与「至多一次 / 至少一次 + 下游幂等」的运维约定。

### Smoke

**W34 收口（Day238）**：全链路 + 支付确认 **3 次** + 报表 B-key + 固定 **`X-Request-Id`** 便于 grep 日志：

```bash
cd dual-system-ms
./smoke-w34-dual-system.sh
```

全链路（与上类似，`smoke-w33-dual-system.sh`）：订单 `PAID` 依赖 MQ/outbox；需 RabbitMQ：

```bash
cd dual-system-ms
./smoke-w33-dual-system.sh
```

Day229（幂等 + Sentinel 限流可复现）：

```bash
cd dual-system-ms
export SENTINEL_LOCAL_QPS=1
./smoke-w33-day229-idempotent-sentinel.sh
```

Day230（报表）：

```bash
cd dual-system-ms
./smoke-w33-day230-report.sh
```

### Sentinel（本地规则开关）

`ds-order-service` 启动时设置：

```bash
export SENTINEL_LOCAL_QPS=1
```

会对资源 `getOrderById`（对应 `GET /api/orders/{id}`）做 QPS 限流，被限流时返回：

- HTTP 429
- body: `{"code":"RATE_LIMITED","message":"too many requests"}`

### Docker Compose 排错

若 `ds-gateway` 日志出现 `serverAddr='null'` 或 Nacos 注册失败：

- Compose 已为各服务设置 `SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848`（容器内直连 `nacos` 服务名）。
- 应用配置优先读该变量，再回退到 `NACOS_ADDR` / 本机默认 `127.0.0.1:8849`。
- 修改 `deploy/compose.yaml` 后需重新构建并启动：`docker compose up -d --build`。
- 若 Nacos 的 `healthcheck` 因镜像无 `curl` 一直失败，可暂时删掉 `nacos.healthcheck` 段，或把 `test` 改成镜像内可用的探测命令。
- RabbitMQ 若报 `Bind for 0.0.0.0:5672 failed: port is already allocated`：说明宿主机 **5672** 已被占用。当前 compose 已默认改用 **5673 / 15673**；仍冲突时可自定义：  
  `RABBITMQ_HOST_PORT=5674 RABBITMQ_MGMT_HOST_PORT=15674 docker compose up -d`

