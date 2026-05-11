# 第 32 周学习计划：微服务进阶（Nacos + Feign + Gateway + Sentinel）

> 对应总纲：**第32周（Nacos/Feign/Gateway/Sentinel）**  
> 目标：把微服务最常用的组合拳在你的项目上跑起来，并且能讲清楚：**注册发现/远程调用/统一入口/限流熔断**分别解决什么问题、代价是什么。  
> 交付物：**拆 2 个最小服务 + Feign 调用 + Gateway 统一入口 + Sentinel 保护一个接口**。  
> 时间：约 **2 小时/天**；本周 **Day 218 ~ Day 224**。  

---

## 本周总目标（只做“最小但完整”的链路）

- 拆出 **2 个服务**（建议按你的社交项目切）：
  - `user-service`：注册/登录/用户信息（可先保留最小接口）
  - `post-service`：发帖/列表/详情/评论（保留你已有核心）
- 用 **Nacos** 做 **服务注册发现**（两服务都注册上去）+（可选）配置中心。
- `post-service` 通过 **Feign** 调 `user-service` 获取作者信息（例如 username）。
- 用 **Spring Cloud Gateway** 作为统一入口：
  - 外部只访问 gateway
  - gateway 按路径转发到两个服务
- 用 **Sentinel** 保护 1 个热点接口（推荐：`GET /posts/{id}` 或 `GET /posts`）：
  - 做一次限流（QPS 或并发线程）
  - 展示“触发后返回什么降级结果”

---

## 项目结构建议（本周新建一个微服务目录，不要在原 `boot-social-demo` 上硬改到乱）

新建目录：`boot-social-ms/`（Maven multi-module 或多个独立 Maven 项目，二选一即可）

- `boot-social-gateway/`（Spring Cloud Gateway）
- `boot-social-user-service/`（用户服务）
- `boot-social-post-service/`（帖子服务）
- `deploy/`（compose：nacos、mysql、redis、mq 可选；本周至少 nacos + 两服务）

> **最小建议**：先用 **多模块 Maven**，统一管理版本，省心。

---

## Day 218（周一）— 微服务最小骨架：三项目能同时启动

**学什么**

- Spring Cloud 与 Spring Boot 的关系：Cloud 管“分布式能力”，Boot 管“应用本体”。
- 服务拆分的最小原则：先按“变化速度不同/边界清晰”拆，不追求一次拆完美。

**做什么**

1. 新建 `boot-social-ms/`（三模块：gateway/user/post）。
2. 三个服务都跑通 `GET /ping` 或 `actuator/health`。
3. 端口规划（示例）：
   - gateway: 8080
   - user-service: 8082
   - post-service: 8083

**验收**

- 三个服务同时启动无端口冲突；你能清楚说出“为什么先拆 user/post”。

---

## Day 219（周二）— Nacos：注册发现（必须跑通）

**学什么**

- 为什么需要注册发现：IP/端口变化、扩缩容、服务列表动态。
- Nacos 的两件事：**注册中心** 与 **配置中心**（本周先把注册跑通）。

**做什么**

1. 用 docker/compose 起 Nacos（单机模式即可）。
2. `user-service` 与 `post-service` 接入 Nacos discovery 并注册成功。
3. 在 Nacos 控制台看到两个服务实例。

**验收**

- 关掉一个服务再启动，Nacos 实例列表能正确变化；README 写下启动命令。

---

## Day 220（周三）— Feign：post-service 调用 user-service

**学什么**

- 远程调用的最小问题：超时、失败、重试（先知道，W32 不全解决）。

**做什么**

1. `user-service` 提供最小接口：
   - `GET /api/users/{id}` → `{id, username}`
2. `post-service` 用 Feign 声明式调用：
   - 在 `GET /api/posts/{id}` 返回里补上 `authorUsername`
3. 设置合理超时（避免默认无限等）。

**验收**

- 关掉 user-service：post-service 的接口能返回一个“可解释”的错误（或降级，Day 222 再做）。

---

## Day 221（周四）— Gateway：统一入口与路由

**学什么**

- 为什么要网关：统一入口、鉴权、限流、灰度、跨域策略集中。

**做什么**

1. Gateway 路由：
   - `/api/users/**` → user-service
   - `/api/posts/**` → post-service
2. 验证外部只打 gateway：
   - `curl http://localhost:8080/api/posts/...`
3. （可选）加一个最小过滤器：打印请求 path 与耗时。

**验收**

- 你能停掉 post-service，gateway 返回 5xx 并能从日志定位转发失败原因。

---

## Day 222（周五）— Sentinel：保护一个热点接口（限流 + 降级）

**学什么**

- 限流与熔断的区别：限流是“保护自己”，熔断是“保护下游/快速失败”。

**做什么**

1. 接入 Sentinel（推荐先从 `post-service` 的一个 GET 接口开始）。
2. 在 Sentinel 控制台配置一条规则：
   - 对 `GET /api/posts/{id}`：QPS 限制（例如 5）
3. 触发限流时返回一个可读的降级响应（例如 `code=RATE_LIMITED`）。

**验收**

- 用简单压测（循环 curl）触发限流，并能稳定复现。

---

## Day 223（周六）— 微服务最小“可运维”：配置、日志、链路可追

**学什么**

- 分布式系统里“能定位问题”比“功能更多”更重要。

**做什么**

1. 统一日志格式（至少包含 serviceName、traceId（可选））。
2. `application-*.yml` 分环境（dev/docker），写清楚：
   - nacos 地址
   - 服务名
   - gateway 路由
3. README 写一份“一键启动顺序与检查点”：
   - nacos up → user up → post up → gateway up → 访问验证

**验收**

- 你能按 README 从 0 启动并验证：服务都注册上 Nacos，gateway 路由可用，Feign 调用成功。

---

## Day 224（周日）— 收口：交付物与演示稿（你要能讲清楚）

**做什么**

1. `smoke-w32-ms.sh`：
   - 启动检查（health）
   - 通过 gateway 调 user/post
   - 触发 sentinel 限流并验证降级响应
2. `W32-notes.md`：写一页“讲稿”：
   - Nacos/Feign/Gateway/Sentinel 各自解决什么
   - 引入后带来什么成本（配置、排障、复杂度）
3. 记录 3 个真实踩坑点（例如端口、服务名、超时、跨域、路由）。

**验收**

- 你能在 10 分钟内演示完整链路，并讲清 trade-off。

---

## 本周交付清单（必须）

- `boot-social-ms/` 三个可运行服务（gateway/user/post）
- Nacos（docker/compose）+ 两服务注册成功
- Feign 调用成功（post → user）
- Gateway 路由可用（外部只打 gateway）
- Sentinel 规则生效并可复现触发
- `smoke-w32-ms.sh` + `W32-notes.md`

---

## 现在开始：Day 218

今天只做“骨架”：

1. 新建 `boot-social-ms/`（gateway/user/post 三模块）。  
2. 三个服务都跑通 `/ping`。  
3. 写下端口规划与服务名（后面 Nacos 要用）。  

做完把你的模块目录结构（`ls` 输出）和三个服务的启动命令发我，我再带你 Day 219 一次性把 Nacos + discovery 依赖与配置对齐，避免版本/依赖踩坑。

