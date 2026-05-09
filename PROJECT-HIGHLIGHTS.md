# 项目亮点简历素材（自用，1 页内）

> 按 **`W35_PLAN.md`**：**每天追加 3～5 条**；每条尽量 **动词 + 技术点 + 可验证**，避免空话。

---

## Day 239 增补（盘点与故事线）

- **双线项目组合**：`**boot-social-demo**`（单体 Spring Boot + 异步通知能力）与 **`dual-system-ms**`（网关 + 多服务 + MQ 最终一致）覆盖 **单体 / 拆分**两条叙述，面试择 2 深挖即可。  
- **`boot-social-demo` 工程化**：**OpenAPI** 对外契约、**Actuator** 健康与信息、学习计划内 **CI/`mvn verify`/冒烟**，可一句话证明「我不是只写完能跑的练习」。  
- **`dual-system-ms` 收口能力**：支付 **`payment_outbox`** 与同事务、`PaymentSucceeded` **幂等收件箱**、`smoke-w34-dual-system.sh` **一键验全链路**。  
- **B 端与可观测**：网关 **`X-Report-Key`**、**`/api/admin/reports`** 前缀、**`X-Request-Id` + MDC**，能讲「为什么报表鉴权在网关、直连报表的风险」。  
- **自我介绍标签已定**：Spring Boot 后端交付；MySQL 与一致性/口径；网关 + MQ 微服务入门——后续答题都往这三条线扣。

---

## Day 240 增补（集合 & 并发口述 + 手写）

- **能口述**：`HashMap` put/扩容/并发问题；`ConcurrentHashMap` **桶锁 + CAS** 思路（JDK8）。  
- **能口述**：`ThreadPoolExecutor` **7 参数**、执行顺序、**有界队列 + 拒绝策略** 与「无限队列慢死」的 trade-off。  
- **能口述**：`volatile` vs **`synchronized`**；JMM **可见性 + happens-before** 各举一例。  
- **能口述**：死锁 **四要素** + **锁序 / tryLock / 缩小临界区**。  
- **手写已练**：`day240/ProducerConsumerBlockingQueueDemo.java` — **`ArrayBlockingQueue` + put/take 阻塞 + 毒丸停消费**。

---

## Day 241 增补（Spring / MVC / 事务口述）

- **`boot-social-demo` 分层话术**：**Controller 薄**（HTTP + 校验 + DTO）；**Service** 承载 **事务与用例编排**；**Mapper/MP** **只碰 SQL** — 与 `INTERVIEW-QA.md` Day241 段落一致，可原样压缩进面试。  
- **能讲清**：**IoC/DI**、`@SpringBootApplication = @Configuration + @ComponentScan + @EnableAutoConfiguration`。  
- **能讲清**：**AOP JDK vs CGLIB**，以及 **`this` 自调用导致事务/切面失效**。  
- **`@Transactional`**：**传播默认 REQUIRED**、**rollbackFor**、`private`/自调用坑。  
- **能画/能指**：**Filter 链 → DispatcherServlet → Interceptor（若有）→ Controller**（见 `INTERVIEW-QA.md` ASCII）。

---

## Day 242 增补（MySQL / 一致性与项目 SQL）

- **真实 SQL 案例**：`boot-social-demo` 的 **`PostMapper.pagePosts`** — **LIKE `%`**、**likes 子聚合**、**ORDER BY**、**深分页 OFFSET** 四类慢点能说清。  
- **索引提案**：**`posts(user_id, created_at, id)`**、**`post_likes(post_id)`**；模糊标题搜引向 **FULLTEXT / 检索引擎**。  
- **`EXPLAIN`**：优先看 **`type`/`key`/`Extra`** + **`rows` 粗判**；了解 **`EXPLAIN ANALYZE`（MySQL 8）**。  
- **事务口述**：**ACID**；**脏读 / 不可重复读 / 幻读**；**RR + MVCC** 够用不必背 gap lock 细节。  
- **订单与支付**：**Outbox + PaymentSucceeded + 收件箱幂等**，讲 **最终一致**，对照 **为何不强上分布式事务**。

---

## Day 243 增补（Redis / MQ / 微服务）

- **`PostDetailCache`**：**`post:detail:{id}`**、详情 **TTL / 穿透占位 TTL**、`evict` 时机；Redis 挂了 **降级**不挡主链路。  
- **`WriteActionRateLimiter`**：**Lua INCR+EXPIRE**，**`bootsocial:rate:{action}:{post}:{user}`**。  
- **三害话术**：穿透用 **项目负缓存** 举例；击穿/雪崩讲 **mutex、随机 TTL、异步刷新**。  
- **MQ**：两边的 **最少一次 + 幂等**；**`payment_outbox`** **重试封顶 FAILED** vs 订单 **`eventId`** 收件箱。  
- **为什么不用 Seata**：**Outbox/MQ + 幂等** 收口最终一致 vs **运维与外部不可 XA**。  
- **微服务三角**：**Nacos / Gateway / Sentinel** 各自 **问题解决 + 代价**一句（见 `INTERVIEW-QA.md` 表）。

---

## Day 244 增补（场景设计小步）

- **秒杀简化版**：表（**inventory + 幂等 + 订单**）、**CAS/排队**、网关限流；与 **`WriteActionRateLimiter`** 类比。  
- **Feed**：**读扩散 vs 写扩散**；映射 **`pagePosts` + 游标/深分页意识 + PostDetailCache**；**坦诚未做关注信箱**。  
- **对象存储**：**presigned PUT/GET**、**key 规范**、DB **只存 objectKey**；映射 **`StorageService` / Cover 流程 / DevStorage smoke**。  
- **5 分钟结构**：需求 → **2～3 表** → **2～3 API** → **2 风险+兜底** → **1 句项目挂钩**（见 `INTERVIEW-QA.md` 表）。

---

## Day 245 增补（模拟面试 + 简历封口）

- **45 min 台本**：自我介绍 **2 min** → 两项目深挖各 **10 min** → 基础抽问 **15 min** → **Day244 场景 5 min** → 反问 **3 min**（表在 `INTERVIEW-QA.md`）。  
- **反问**：团队栈与发布、业务节奏、可观测/新人上手 —— **各准备一句**即可。  
- **简历公式**：**动词 + 技术点 + 可验证**（脚本 / Compose / `mvn verify` / 具体类名级锚点）。  
- **交付**：`INTERVIEW-QA.md` **题库封口**（R1～R8 + 此前 Q/S/M）、`PROJECT-HIGHLIGHTS` **压到 1 页**、**录音至少一轮全场**。  
- **基础收口**：**502/504**、**DTO 原因**、**幂等 / JWT vs Session**、**`mvn verify`** —— 见 **Day245 R1～R8**。
