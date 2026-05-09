# 第 29 周学习计划：分布式入门（选 1–2 个组件落地到 `boot-social-demo`）

> 对应总纲：**第29–30周（分布式入门：Zookeeper/Dubbo/RabbitMQ/Seata/SaToken等）**  
> 本周定位：不做“组件清单式跑通”，而是在你的 **`boot-social-demo`** 上落地 **1～2 个真实需求**，做到可讲清“为什么用/怎么用/代价是什么”。  
> 时间：约 **2 小时/天**；本周 **Day 197 ~ Day 203**。  

---

## 本周建议选型（默认路径）

为了与你现有项目最贴近、收益最大，本周默认落地两件事：

1. **Sa-Token**：把 W25/W27 的 **Session 登录态**升级为 **Token 登录态**（更贴近前后端分离与分布式场景）。
2. **RabbitMQ**：把“评论/点赞 → 通知作者”改成 **异步消息**（让你体验解耦、削峰、失败重试/补偿的基本思路）。

> 说明：Zookeeper/Dubbo/Seata 这类更偏“多服务/多库”的组件，放到你 W32 开始拆服务或 W30 再加会更自然；本周先把 token 与消息打牢。

---

## 本周总目标

- **理解分布式的 5 个关键词**：一致性、可用性、延迟、幂等、可观测。
- **鉴权从 Session → Token**：
  - 登录返回 token
  - 请求携带 token（header）
  - 未登录/无权限返回统一错误体
  - 关键写接口受保护
- **异步消息落地**：
  - 评论/点赞事件投递到 MQ
  - 消费端落库（notification 表）或打印结构化日志
  - 至少设计 1 个幂等点（避免重复消费导致重复通知）
- **容器化复现**：用 `compose.yaml` 加上 RabbitMQ（和你现有 MySQL/Redis/Nginx 共存）。

---

## Day 197（周一）— 分布式必备概念：为什么要“拆”与“异步”

**学什么（只学必要的）**

- 为什么单体会遇到瓶颈：吞吐、耦合、发布风险、数据库压力。
- 异步的收益与代价：最终一致性、消息丢失/重复、追踪难度。
- 鉴权的收益与代价：token 方便扩展，但要考虑注销/过期/刷新与泄漏风险。

**做什么**

1. 在 `W29-notes.md` 写 2 页以内的“概念备忘”：
   - 你准备把哪些动作做成异步（本周：评论/点赞通知）
   - token 模式相比 session 的变化点（客户端要保存 token、服务端无状态化）
2. 画一个最小流程图（文字也行）：
   - `POST comment` → DB 写入 → MQ publish → consumer → notification 落库

**验收**

- 你能用 3 分钟讲清：为什么这里用 MQ、为什么不直接同步插入通知表。

---

## Day 198（周二）— Sa-Token：接入与最小登录闭环

**学什么**

- Sa-Token 的基本概念：登录、token、会话、权限（只用到最小集）。

**做什么**

1. 给 `boot-social-demo` 加 Sa-Token 依赖与最小配置（token 过期时间、token 风格）。
2. 改造认证接口：
   - `POST /api/auth/login` 成功后返回 token（或 Sa-Token 的 tokenName/tokenValue）
   - `POST /api/auth/logout` 让 token 失效
   - `GET /api/me` 从 token 取当前用户
3. 兼容策略（二选一）：
   - **A（推荐）**：直接切换为 token，不再依赖 session
   - **B**：保留 session 但新增 token（不推荐，会让你后面维护成本翻倍）

**验收**

- 用 `curl`：
  - 登录拿到 token
  - 带 token 调 `me` 成功
  - 不带 token 调 `me` 返回 401

---

## Day 199（周三）— Sa-Token：接口鉴权收口（替换 Interceptor）

**学什么**

- 从“自己写拦截器”转向“框架鉴权”：降低 bug 与重复代码。

**做什么**

1. 把写接口（发帖/评论/点赞）从 session 拦截改成 Sa-Token 的鉴权方式：
   - 可用注解（如 `@SaCheckLogin`）或统一拦截配置（二选一，保持一致）
2. 统一错误输出：
   - 未登录 401
   - （可选）无权限 403（先不做复杂 RBAC，只留口子）
3. 更新 Swagger/OpenAPI：说明 token 怎么传（通常是 header）。

**验收**

- 你能删掉一部分自写的登录判断逻辑；业务 controller 变薄；错误体格式仍一致。

---

## Day 200（周四）— RabbitMQ：容器启动 + Boot 接入 + 第一个事件

**学什么**

- exchange / queue / routing key 最小概念（能用就行）。
- 消息可靠性最小集：确认机制、重试思路（先别深入）。

**做什么**

1. 在 `compose.yaml` 加一个 RabbitMQ（带 management UI，方便观察）。
2. `boot-social-demo` 引入 `spring-boot-starter-amqp`。
3. 写一个最小消息：
   - `POST /api/dev/mq-test`（仅 dev）发一条消息
   - consumer 打印日志（或落库）

**验收**

- 能在 RabbitMQ 管理 UI 看到队列/消息流；应用启动能正常连 MQ。

---

## Day 201（周五）— 异步通知：评论/点赞 → publish → consumer 落库

**学什么**

- “业务事件”抽象：CommentCreated / PostLiked。
- 幂等：同一事件重复消费，如何不重复落库？

**做什么**

1. 设计通知表（最小）：
   - `notifications(id, user_id, type, ref_id, payload, created_at, dedup_key UNIQUE)`
2. 评论成功/点赞成功后发布事件（包含 postId、actorId、ownerId、时间）。
3. consumer 消费并插入通知表：
   - 以 `dedup_key` 做幂等（例如 `like:{postId}:{actorId}` 或 `comment:{commentId}`）

**验收**

- 重复发送同一消息不会插两条通知（靠 UNIQUE 或代码幂等）。

---

## Day 202（周六）— 失败与补偿：重试、死信队列（了解 + 最小演示）

**学什么**

- 为什么会失败：网络抖动、DB 超时、consumer 崩溃。
- 最小可行策略：重试次数 + 人工补偿（先不做复杂平台）。

**做什么**

1. 人为制造一次消费失败（例如 consumer 抛异常），观察重试/堆积。
2. 配一个最小的失败处理策略（二选一）：
   - A：固定次数重试后记录到表（`failed_messages`）
   - B：DLQ（死信队列）+ 手工脚本再投递（写个 `replay` 小脚本即可）

**验收**

- 你能解释：生产里为什么要 DLQ、为什么不能无限重试。

---

## Day 203（周日）— 收口：文档 + smoke 脚本 + 复盘（为 W30/W32 做准备）

**做什么**

1. 更新 `README`：
   - token 鉴权怎么用（header 示例）
   - MQ 一键启动（compose）
   - 评论/点赞如何触发通知
2. 新增 `smoke-w29-token-mq.sh`：
   - 登录拿 token
   - 发帖
   - 评论/点赞触发通知
3. `W29-notes.md` 写复盘：
   - 这两件组件各自解决了什么问题
   - 引入它们带来了什么新复杂度

**验收**

- 一条 smoke 脚本跑通“登录→发帖→评论/点赞→通知落库”；文档能让你未来快速复现。

---

## 本周交付清单（必须）

- `boot-social-demo`：Session → **Sa-Token**（token 鉴权）
- `compose.yaml`：新增 **RabbitMQ**
- 异步通知：评论/点赞事件 → MQ → consumer → `notifications` 落库（含幂等）
- 文档 + `smoke-w29-token-mq.sh` + `W29-notes.md`

---

## 现在开始：Day 197

今天先不写代码，先把“用组件解决什么问题”想清楚：

1. 写 `W29-notes.md`：你要异步化哪个动作、为什么。  
2. 写出事件结构（字段）：CommentCreated / PostLiked。  
3. 定好通知表的最小字段与 dedup_key 方案。  

写完把你的 **事件字段设计** 和 **dedup_key** 发我，我会帮你检查是否容易踩“重复通知/漏通知”的坑，然后再进入 Day 198 的 Sa-Token 接入。

