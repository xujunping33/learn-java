# W32-notes（Nacos / Feign / Gateway / Sentinel）

> 目标：10 分钟演示一条最小微服务链路，并能讲清楚 trade-off。

## 演示链路（建议顺序）

- Nacos 控制台：能看到 `boot-social-user-service`、`boot-social-post-service`、`boot-social-gateway`
- 只访问 gateway：
  - `GET /api/users/1`
  - `GET /api/posts/1`（会在 post 内部 Feign 调 user，拼出 `authorUsername`）
- Sentinel 限流：
  - 对资源 `getPostById` 配 QPS=1
  - 压测 `GET /api/posts/1`，出现 `HTTP 429` + `RATE_LIMITED`

## 四件事各自解决什么

- **Nacos（注册发现）**
  - **解决**：服务实例 IP/端口变化、扩缩容、动态实例列表
  - **代价**：多一个关键基础设施；排障要看“注册是否成功/心跳/命名空间/服务名”

- **Feign（远程调用）**
  - **解决**：跨服务 HTTP 调用的样板代码，声明式客户端 + 集中超时配置
  - **代价**：网络不可靠（超时/重试/雪崩）；需要有“失败时的策略”（超时、降级、熔断）

- **Gateway（统一入口）**
  - **解决**：统一路由、跨域、鉴权、限流、灰度等能力集中到入口层
  - **代价**：网关本身变成关键路径；路由/过滤器出问题会影响所有流量

- **Sentinel（保护热点接口）**
  - **解决**：限流、熔断降级，避免被打爆；让系统在压力下“可控地失败”
  - **代价**：规则维护与观测；需要定义清晰的降级响应（否则业务体验不一致）

## 本周最小交付物（对应仓库）

- `boot-social-ms/`：gateway/user/post 三服务
- Nacos：`boot-social-ms/deploy/compose.yaml`
- Feign：`post-service` 调用 `user-service`
- Gateway：`/api/users/**`、`/api/posts/**` 走 `lb://...`
- Sentinel：资源 `getPostById` 限流返回 `429 + RATE_LIMITED`
- Smoke：`boot-social-ms/smoke-w32-ms.sh`

## 3 个踩坑点（本周真实遇到/容易遇到）

- **版本兼容**：Spring Boot 与 Spring Cloud 版本不匹配会被 compatibility verifier 直接拦启动
- **lb:// 依赖**：Feign/Gateway 用服务名做负载均衡需要 `spring-cloud-starter-loadbalancer`
- **链路追踪**：需要把 trace header 真正传播到下游（gateway→service→feign→service），否则 traceId 会断

