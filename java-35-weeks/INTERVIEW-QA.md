# 面试口述题（我自己的答案）

> 按 **`W35_PLAN.md`** 每日补题；表述可随项目迭代改。**禁止**长篇复制八股——每题够用即可，面试时口述会再缩写。

---

## Day 239（周一）— 盘点、STAR、自我介绍标签

### 一、我 Git 上能演示的代表模块（名称 + 启动/验收）

| 模块 | 一句话 | 怎么起 / 怎么验 |
|------|--------|----------------|
| **`boot-social-demo`** | Sa-Token、帖子/评论/点赞、OpenAPI；RabbitMQ 通知相关能力（学习路线里含 Outbox/幂等等） | 见该目录 `README.md`：`docker compose ...` / `mvn spring-boot:run`；冒烟 **`./smoke-boot-social.sh`**（或 README 所写脚本）；CI 可走 **`mvn verify`** |
| **`dual-system-ms`** | 下单→支付→**Outbox→MQ→订单 PAID→报表**；Nacos；网关报表 **`X-Report-Key`**；**`X-Request-Id`** | `dual-system-ms/deploy/docker compose up`，根目录 **`./smoke-w34-dual-system.sh`** |
| **`boot-social-ms`**（可选深挖） | 多模块、网关路由、拆分练习 | 各子模块 `README` / `compose` |

面聊时我一般 **只深讲 2 个**：`**boot-social-demo**`（业务全 + Boot 栈熟） + **`dual-system-ms**`（异步一致性与微服务收口）。

---

### 二、三个「自我介绍标签」（90 秒内会反复回扣）

1. **Spring Boot 后端**：REST、分层、测试与 Compose 冒烟，能端到端说清楚一次请求链路。  
2. **MySQL + 一致性意识**：事务边界、索引与口径；跨服务用 **MQ + 幂等 + Outbox** 而非默认上分布式事务。  
3. **微服务入门（网关 / 注册发现 / 消息）**：Nacos、`lb://` 网关、报表最小鉴权、限流演示（Sentinel）。

---

### 三、STAR 故事 ① — `boot-social-demo`（社交 API 与学习型「生产习惯」）

- **S**：需要在 Spring Boot 3 下单体里贯通 **登录态、分页列表、交互写路径**，并顺带练习 **异步解耦**（评论/点赞后的通知链路），不能只停留在 CRUD Demo。  
- **T**：我负责整块后端契约与关键横切：**鉴权**、分页与校验、以及与 MQ/存储相关的演进（按学习计划分阶段落地）。  
- **A**：技术选型上以 **Sa-Token + MyBatis-Plus + MySQL** 为主业务；文档用 **springdoc-openapi**，可观测保留 **Actuator** 最小暴露；对「写完能验」坚持用 **冒烟脚本 / CI（verify）**，避免只靠手动点浏览器。异步侧按课程节奏接入 **MQ**，并把 **幂等与可靠性**当成必讲设计点（与消费者、去重语义挂钩）。  
- **R**：项目可 **`mvn verify`、本机或 Compose 跑通冒烟**；自我介绍时能 **2 分钟内**从登录讲到发帖列表分页，并说清 **哪些地方必须做幂等 / 哪些地方是同步事务边界**。  

（字数约 290 字，可按面试时限压到 **60～90 秒**。）

---

### 四、STAR 故事 ② — `dual-system-ms`（订单 / 支付 / 报表拆分 + 最终一致）

- **S**：单机 Feign「支付成功顺手改订单」在真实链路里容易耦合、超时难控；同时要练习 **网关、多数据源报表口径、B 端最小鉴权**。  
- **T**：我把「订单变 PAID」改成 **异步主路径**：支付域只保证「钱与支付单事实」入库，订单域靠 **事件**追上；顺带把 **报表**与 **网关**收口到可演示。  
- **A**：支付确认与 **`payment_outbox` 同事务**，定时投递 **`PaymentSucceeded`**；订单 **`processed_payment_events` 幂等**消费 MQ；网关对 **`/api/reports`** 校验 **`X-Report-Key`**，并透传 **`X-Request-Id`** 便于日志关联；脚本 **`smoke-w34-dual-system.sh`** 覆盖 **confirm 幂等、报表鉴权** 等收口场景。文档里写明 **下单日 vs 支付成功日** 两套统计口径（UTC）。  
- **R**：**Compose + 冒烟脚本**一轮可过完核心链路；能向面试官说清楚 **为什么没有默认上 Seata**、**重复消息如何保证订单单一终态**、**报表 key 为何不放在业务同源路径**。  

（字数约 310 字，面试时可删减「文件名」只说能力。）

---

### 五、口述练习 Checklist（Day 239 验收）

- [ ] **90 秒**讲完「项目 1**：背景 → 架构 → **我落地的 2 个技术决定** → 怎么验收。  
- [ ] 「项目 2」再 **90 秒**（同上结构），两条之间不混讲。  
- [ ] 手机录音回放一遍：删掉「就是那个、呃、怎么说呢」三类口头语 ≥ 3 次。

---

## Day 240（周二）— Java 集合 & 并发（口述题 ×10）

> 手写练习：`day240/ProducerConsumerBlockingQueueDemo.java`（阻塞队列版生产者消费者）。运行（在 **`java-35-weeks/`** 下）：`cd day240 && javac ProducerConsumerBlockingQueueDemo.java && java ProducerConsumerBlockingQueueDemo`；若在 Git 仓库根目录，先 `cd java-35-weeks`。

### Q1：`HashMap` put 大致做什么？

1. 算 **`hash`**（高位参与扰动后与容量取模定位桶），JDK8 桶里是链表或红黑树。  
2. **桶为空**直接放入 **Node**。  
3. **key 已存在**（`equals`）则 **替换 value**，返回旧值。  
4. **冲突**则链表尾插或树插入；链表过长会 **树化**。  
5. **`size`** 超过 **阈值（负载因子 × 容量）**触发 **扩容**：容量翻倍，**重新分布**节点（rehash）。  
6. 时间复杂度平均 **O(1)**；最坏与冲突与树高有关，面试点到「扩容成本」即可。

### Q2：`HashMap` 为什么并发下不安全？

1. **数据层面**：两个线程同时 put，可能 **覆盖写入**、链表 **成环**（JDK7 经典）或结构损坏。  
2. **可见性**：没有内存语义保证「一个线程写完另一个立刻可见」（除非你用 **ConcurrentHashMap** 或外部同步）。  
3. **扩容**：并发 resize 时更易出现 **死循环/丢链** 等历史问题；JDK8 修了环链，仍 **不应**多线程写普通 `HashMap`。  
4. 结论：**多读单写**也要么 **包一层锁**，要么 **`ConcurrentHashMap`**。

### Q3：`ConcurrentHashMap` JDK8+ 怎么理解（不必背版本细节）？

1. **废除「段锁」大结构**（JDK7 概念知道即可），JDK8 以 **桶头 volatile + synchronized 锁桶** 为主。  
2. **读**：多数情况 **无锁** + **volatile/CAS** 保证必要可见性。  
3. **写**：锁住 **单个桶**，降低锁粒度。  
4. **扩容**：**ForwardingNode** 协助 **多线程协助迁移**（知道「能并行搬」就够）。  
5. 和面官说：**牺牲了绝对简单，换来了「大多数时候比全局一把锁 Hashtable 好扩展」**。  
6. 仍要注意的是：**复合操作**（`if absent then put`）要用 **`compute`/`merge`/`putIfAbsent`** 等 API，自己 `get` + `put` **仍可能错**。

### Q4：`ThreadPoolExecutor` 参数怎么讲？

1. **`corePoolSize`**：常驻核心线程数。  
2. **`maximumPoolSize`**：任务堆积时可达的上限。  
3. **`keepAliveTime`** + **`unit`**：超过核心的线程 Idle 多久回收。  
4. **`workQueue`**：**缓冲队列**（有界 / 无界决定行为）。  
5. **`threadFactory`**：命名、daemon、优先级、异常钩子。  
6. **`RejectedExecutionHandler`**：队列满且线程达到 **max** 时的策略：**Abort / CallerRuns / Discard / DiscardOldest**。  
7. 执行顺序记：**未满 core → 尽量入队 → 加到 max → 拒绝**。

### Q5：为什么线程池不推荐「无限队列」？

1. **`LinkedBlockingQueue` 默认 `Integer.MAX_VALUE`** 看起来像无限：任务都来 **排队**，**`maximumPoolSize` 几乎起不到削峰**。  
2. 结果是：**队列内存暴涨**、**延迟爆炸**（老任务永远在处理队尾），看起来像「没报错但系统死了」。  
3. 应该用 **有界队列 + 明确拒绝策略**（或 CallerRuns **反压上游**），让问题 **快速失败** 而不是 **慢死**。  
4. 和 MQ 一样：**背压**比「啥都接」健康。

### Q6：`volatile` 解决什么、不解决什么？

1. **解决**：**单字段**的 **可见性**（写后其他线程读更可能立刻看到）+ **禁止部分重排序**（建立 **happens-before**）。  
2. **不解决**：**复合操作**如 `i++`（读-改-写）仍 **非原子**，要 **`Atomic*`** 或 **synchronized**。  
3. **典型用法**：**状态标志**（`volatile boolean running`）、**双重检查单例里 instance 引用**（配合禁止重排）。  
4. 一句：**volatile 是「可见 + 部分有序」，不是锁**。

### Q7：`synchronized` 怎么口头讲清楚？

1. **对象监视器锁**：同一把锁 **互斥** 进入 **同步块/方法**。  
2. **可重入**：同一线程已持锁可再次进入。  
3. **内存语义**：释放锁 **happens-before** 后续获取同锁的线程看到更新。  
4. **锁升级**（粗讲）：偏向 → 轻量 → 重量（JVM 优化，不必背状态机）。  
5. 缺点：**粒度大**易竞争；**持锁内别调外部慢 IO**。  
6. **自调用**走不到代理时 **Spring AOP 事务**会踩坑——这是后面 Spring 题再扣。

### Q8：JMM 你最少要记住哪两句？

1. **可见性**：CPU 缓存、指令重排会让「先写后读」在别的线程 **乱序视之**；需要 **happens-before** 规则兜住。  
2. **有序性**：单线程 as-if-serial；多线程靠 **volatile、synchronized、final、线程 start/join** 等建立顺序。  
3. 面试别背全书：能举 **`volatile` 标志位** 或 **`synchronized` 保护共享结构** 即可。  
4. **happens-before**：**A hb B** 则 A 的副作用对 B **可见**（通俗版够答）。

### Q9：死锁四要素 + 怎么破？

1. **互斥**、**占有且等待**、**不可抢占**、**循环等待**。  
2. **破法**：**锁顺序一致**（全按 id 升序加锁）、**超时锁**（`tryLock`）、**减少持锁范围**、**一次性申请资源**。  
3. 线上：**dump 线程栈**找 **BLOCKED** 链。  
4. 预防比救更重要：**别在锁里等 IO / 别嵌套多把锁**。

### Q10：`ArrayList` 扩容与 `fail-fast`？

1. **扩容**：默认容量 10，**`add` 不够时**按 **1.5 倍** grow，**数组拷贝**成本 **O(n)**，批量 add 可先 **`ensureCapacity`**。  
2. **fail-fast**：**迭代中结构修改**（非迭代器自己的 `remove`）抛 **`ConcurrentModificationException`**，靠 **`modCount`**。  
3. 这是 **尽力而为**，不是强一致并发检测。  
4. 并发写用 **`CopyOnWriteArrayList`** 或 **外部同步 / 换队列模型**。

### Day 240 自测（计划验收）

- [ ] 抽 **Q2、Q4、Q9** 连续讲满 **2 分钟**（可录音）。  
- [ ] 跑通 `day240` 的 demo，能口头说明 **put/take 为何阻塞**、**毒丸干什么**。

---

## Day 241（周三）— Spring / Spring Boot（IoC、AOP、事务、Web）

### 挂钩 `boot-social-demo`：我为什么这样分层？

1. **`web`（`PostController`、`AuthController` 等）做薄**：**参数校验、HTTP 状态码、DTO 装配**；尽量不写事务与复杂业务分支，便于 **Swagger / 冒烟**对齐接口契约。  
2. **`service`（如 `PostService`、`CommentService`）承载用例**：** `@Transactional`** 开在「一次业务闭包」上（发帖、评论、点赞幂等）；与领域相关的 **编排**（调 mapper、调其它 service、必要时发 MQ/Outbox）放这里，**可读 + 单测 mocking 边界清晰**。  
3. **`mapper` / MyBatis-Plus**：**只扛 SQL / CRUD**，不掺 HTTP 与用户上下文； XML 或 `BaseMapper` 由复杂度决定。**通知、存储**等横切演进为独立 **`NotificationService`、`StorageService`**，避免控制器膨胀。  
4. **一句话收口**：控制器像「外交官」，service 像「项目经理」，mapper 像「仓库保管员」——**责任分离后**，换接口形态（加个 App 网关）也不至于推倒 SQL。

---

### 一次 HTTP 请求到 Controller（极简链路，可画白板）

```
Browser / curl
    -> Tomcat Connector (socket)
    -> Servlet Filter chain  (Ordered, 如 Cors / CorrelationId)
    -> DispatcherServlet (Front Controller)
           -> HandlerMapping (找 @RequestMapping)
           -> HandlerAdapter (调用 controller 方法，绑参数 / 返回值处理)
           -> Controller 返回 (Body / ResponseEntity)
    <- HttpMessageConverter 写 JSON
    <- Servlet Filter chain（返回路径）
```

**面试一句**：`**DispatcherServlet**` 中心化调度；**Interceptor**（若配置）夹在 **映射之后、controller 前后**（与 Servlet Filter 链路位置不同）。

---

### Spring / Boot 口述题 ×8

#### S1：IoC 和 DI 我怎么说？

1. **IoC**：对象的 **创建与装配**由容器负责，而不是业务代码里 `new` 一片。  
2. **DI**：依赖通过 **构造器 / setter / 字段注入**进来，便于 **替换实现**（测试 mock、切换数据源）。  
3. Spring Boot 用 **`@Configuration` + `@Bean`**、`@Component` 扫描、`@Conditional*` 决定是否装配。  
4. 收口：**耦合方向反转**——我依赖抽象，具体的 Bean 生命周期交给容器。

#### S2：`@Component`/`@Service`/`@Repository`/`@Controller` 区别？

1. **都是 `@Component` 特例**，等价于参与 **组件扫描**。  
2. **`@Repository`**（历史语义）常与 **DAO 异常转 DataAccessException** 联系；也可用纯 MP。  
3. **`@Service`** 标注 **领域/应用服务**，提醒团队别把胖逻辑堆在 Controller。  
4. **`@Controller` + `@ResponseBody`** 才是 **REST Controller**（或直接用 **`@RestController`**）。

#### S3：Bean 生命周期我记哪几步？

1. **实例化** → **`@PostConstruct`** / `InitializingBean` → **使用中** → 容器关闭时 **`DisposableBean`/destroy 方法`。  
2. **Aware** 接口（`BeanNameAware` 等）在早期注入容器元数据（了解即可）。  
3. **作用域**：**singleton / prototype**，Web 下有 **request/session**（需慎用）。  
4. 面试点：**prototype** 不会在单例 Bean 里自动「每条请求一个新依赖」，需要 **`@Lookup`/`ObjectFactory`**——知道「有这种坑」即可。

#### S4：AOP 代理 JDK vs CGLIB？

1. **JDK 动态代理**：目标类 **实现了接口**，代理的是 **接口方法**。  
2. **CGLIB**：**子类**代理，可走 **具体类扩展点**（`final` 方法不能代理）。  
3. Spring 默认：**有接口可走 JDK**，否则 **CGLIB**（Boot 2+ 多起 CGLIB，`proxyTargetClass` 可强制）。  
4. **局限性**： **`private`/`final`、`同类 this 调用`** 切面进不去——这是 **事务 / 校验注解失效**重灾区。

#### S5：`@Transactional` 为什么经常「失效」？

1. **同类 `this`** 调用：绕开 Spring 代理，事务切面 **不进**。解法：**拆类**、**注入自代理（不推荐）`、`ApplicationContext.getBean`（丑）**。  
2. **`private`/非 public**（默认代理规则下）切面可能不生效。  
3. **异常类型不匹配**：默认 **运行时异常回滚**，**checked 不回滚**；可按 **`rollbackFor`**。  
4. **传播行为误用**：`REQUIRES_NEW`/`NESTED` **嵌套**与预期不一致先画调用栈再讲。

#### S6：事务传播举一个我最常用的就够

1. 默认 **`REQUIRED`**：**加入已有事务**，没有则新建。  
2. **`REQUIRES_NEW`**：**总是新事务**，适合 **审计日志**必须与主事务隔离提交。  
3. **`NOT_SUPPORTED`**：挂起事务以 **纯读**或非 JDBC 操作（少用但要听过）。  
4. 我讲项目时：**一个 HTTP 请求 = 一层 service `@Transactional`**，内部调其它 service 多数是 **PROPAGATION_REQUIRED**。

#### S7：Spring MVC Filter vs HandlerInterceptor？

1. **Filter**：**Servlet 规范**，包住 **DispatcherServlet**，能拦 **静态资源**。  
2. **Interceptor**：**Spring MVC**，粒度在 **handler 映射之后**，能拿到 **`HandlerMethod`**（做鉴权注解判断等）。  
3. **顺序**：Filter **chain.order** → DispatcherServlet → Interceptor **`preHandle` → Controller → postHandle`**（视图场景）`**afterCompletion`**。  
4. **boot-social-demo**里 **Sa-Token 路由拦截**走的是框架提供的 **Interceptor 语义**（口述时类比即可）。

#### S8：`@SpringBootApplication` 等价什么？

1. **`@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`**（扫描当前包及子包）。  
2. **自动配置**：`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。  
3. **`@Conditional*`**：**classpath / 属性**不满足就不装 Bean，所以 **starter** 才能做到「加个依赖就开功能」。  
4. 我讲项目：**换 profile / 关掉 Docker 时用 `application-test`** 就是靠 **自动配置条件**把重 Bean 摘掉。

---

### Day 241 自测（计划验收）

- [ ] 白板/纸：默画 **`Filter → DispatcherServlet → Controller`** 一页。  
- [ ] **`@Transactional` 失效** 至少背 **自调用 + 异常类型 + 访问修饰符** 三条。  
- [ ] **`boot-social-demo` 分层** 用 **30 秒**讲给假面试官听。

---

## Day 242（周四）— MySQL（索引、事务、`EXPLAIN`）

### 项目真题：`PostMapper.pagePosts`（列表分页 SQL）

**源码位置**：`boot-social-demo/src/main/resources/mappers/PostMapper.xml` → **`pagePosts`**。

核心形态：**`posts p` JOIN `users u`，LEFT JOIN（`post_likes` 按 `post_id` 分组计数）子查询**，条件里可选 **`p.title LIKE '%keyword%'`**、**`p.user_id = ?`**，**`ORDER BY p.created_at DESC, p.id DESC`**，再由 MyBatis-Plus 分页插件拼 **`LIMIT offset, size`**。

#### 1）可能慢在哪里？

1. **`LIKE '%xx%'`**：普通 **B-tree 索引基本用不上**，易 **全表/大范围扫描**。  
2. **子查询对每个 post 左连聚合**：`post_likes` 一大，**GROUP BY post_id + JOIN** CPU/IO 都贵；高并发列表会放大。  
3. **`ORDER BY created_at DESC, id`**：若没有 **合拍复合索引**，常见 **`Using filesort`**（内存排序或磁盘临时）。  
4. **深分页**：`OFFSET` 很大时要 **跳过大量行**，等价读很多却只取一页——经典 **slow limit**。

#### 2）我会先加什么索引？（口述版）

1. **`posts`**：**(user_id, created_at DESC, id)** — 若在「只看某作者的帖子」高频，可 **大幅减少过滤 + 有序扫描**成本（仍以 **EXPLAIN 为准）。  
2. **全局信息流**（不配 `userId`）：考虑 **`(created_at DESC, id)`** 或至少 **`created_at`**，减轻排序压力。  
3. **`post_likes`**：**`post_id` 上索引**（若尚未有），让子聚合 **按 post_id 扫 likes** 更便宜。  
4. **标题搜**：若产品坚持模糊搜，谈 **全文索引 / ES** 比硬加 `title` B-tree 现实；面试点：**知道 `%..%` 不吃普通索引**。

#### 3）`EXPLAIN` 我看什么判断好坏？

1. **`type`**：从好到差心里要有谱——**`const/ref/range/index/ALL`**；出现 **`ALL`** 且 **rows 巨大**要警惕。  
2. **`key` / `possible_keys`**：有没有 **真正用上**我期望的索引。  
3. **`Extra`**：**`Using filesort` / `Using temporary`** 是大户；**`Using index`**（覆盖索引）多是好事。  
4. **`rows`**：**估算扫描行数**粗看成本（不是绝对值）。  
5. 我一贯说法：**先抓 `type + key + Extra`，再决定是改 SQL 还是加索引**。

---

### 口述：支付与订单怎么「一致」？（`dual-system-ms` 口径）

1. **强一致两库同事务**（分布式 2PC/Seata）在我这个练习项目里 **刻意没当默认**，成本与运维重。  
2. **主路径**：支付成功与 **`payment_outbox` 同事务** → 异步发 **`PaymentSucceeded`** → 订单服务 **幂等表**（如 **`processed_payment_events` 按 `eventId`**）后 **`markPaid`**。  
3. **重复消息**：靠 **唯一键/INSERT IGNORE** + 业务 **`markPaid` 条件更新**，保证 **终态不来回跳**。  
4. **对账**：支付 **SUCCESS** 与订单 **PAID** 可通过 **job/报表** 扫「长时间未 PAID」补偿（口述点到即可）。  
5. 一句收口：**最终一致 + 幂等 + Outbox 减丢消息概率**，比「支付接口里同步调订单接口」更可扩展。

---

### MySQL 高频口述题 ×8

#### M1：为什么 InnoDB 索引用 B+树（直觉版）？

1. **树矮**：同样数据量比链表/二叉 **IO 次数少**。  
2. **叶子链表**：**区间扫描、`ORDER BY` 利用顺序**很方便。  
3. **数据（或 PK）通常在叶子**：非聚簇二级索引指向主键再走 **二次查找**。  
4. 面试收口：**多读少改的 OLTP**，B+ 更平衡 **读放大与维护成本**。

#### M2：联合索引「最左前缀」举例

1. 索引 **`(a, b, c)`** 可走 **`WHERE a=?`**、`**a=? AND b=?**`；不走 **`WHERE b=?`** 单独一类（一般）。  
2. **跳过左列**就失去了 **排序上的连续性**，优化器没法从同一棵前缀树直接定位。

#### M3：覆盖索引是什么？

1. 查询所列字段 **二级索引上都有**，则可能 **不回表**，`EXPLAIN Extra` 常见 **`Using index`**。  
2. 代价：索引 **更宽**、写入 **更重**——和业务读写比权衡。

#### M4：`EXPLAIN ANALYZE` 和只看 `EXPLAIN`？

1. **`EXPLAIN`**：**优化器估算**（老牌 `EXPLAIN`）。  
2. **`EXPLAIN ANALYZE`**（MySQL 8.0.18+）：真跑一遍抽样，看 **actual time / loops**。**练习环境能跑则更有说服力**。

#### M5：ACID 各讲一句

1. **A** **原子**：事务内要么全成要么全败。  
2. **C** **一致**：约束与业务不变式不被破坏（与「乐观业务一致」区分开）。  
3. **I** **隔离**：并发事务互相「看到的历史」有度。  
4. **D** **持久**：`commit` 后崩溃不丢已提交日志链路的语义（口述到 **redo/binlog** 可加分）。

#### M6：三类读问题 + RR 我能讲到哪里？

1. **脏读**：读未提交——**RU** 可能发生。  
2. **不可重复读**：同一会话两次读之间 **另一事务提交改过**。  
3. **幻读**：范围条件 **多了新行**（定义讨论可简化：「多读一次多了行」）。  
4. **InnoDB RR + MVCC + gap lock/next-key** 对部分幻读情景有抑制——**背「完全消灭」不如说「快照读 vs 当前读差异」**。

#### M7：深分页替代方案？

1. **`LIMIT offset`** 大到爆 → 改 **`WHERE id > ? ORDER BY id LIMIT n`** **游标/seek**分页。  
2. 或用 **搜索/HBase 式 key**维护「下一页游标」，业务配合。

#### M8：为什么「count(*)」有时也会慢？

1. **引擎要扫的范围大**、**二级索引与主键选择**、**MVCC 可见性判断**（InnoDB 某些场景）都会让 **count 不是 O(1)**。  
2. 超大规模靠 **汇总表/近似 count/搜索引擎**。

---

### Day 242 自测（计划验收）

- [ ] 不看稿讲 **`pagePosts` 慢点 + 两个索引 + EXPLAIN 看哪三列**。  
- [ ] **支付/订单一致** 用 **45 秒**讲完（Outbox + 幂等 + 最终一致）。  
- [ ] **M1/M2/M5** 任抽一题讲满 **1 分钟**。

---

## Day 243（周五）— Redis / MQ / 微服务（用项目复述）

### 一、`boot-social-demo`：我在 Redis 里放了什么？

| 用途 | Key 形态（概念） | TTL / 语义 | 失效 |
|------|------------------|------------|------|
| **帖子详情缓存** | `post:detail:{postId}`（见 `PostDetailCache.key`） | 命中：**`app.api.post-detail-cache-ttl`**（默认 **120s**）；**不存在**：写 **`__NULL__`** **负缓存**，TTL **`post-detail-absent-cache-ttl`**（默认 **30s**），防穿透 | **写路径**（发帖/改帖/点赞/评论等）`**evict(postId)**` |
| **写操作固定窗口限流** | `bootsocial:rate:{action}:{postId}:{userId}`，`INCR+EXPIRE` Lua（`WriteActionRateLimiter`） | 窗口秒数由 **`app.rate-limit.*`** | 窗口期满 key 过期 |
| Redis 不可用 | — | — | 代码 **`catch`** 后 **降级**（不测速 / 不穿缓存但不挡主链路） |

口述 **trade-off**：**负缓存短 TTL**，避免刷单 id 捅穿 DB；**TTL 太短**击穿风险仍在，大批量热点要靠 ** mutex/逻辑过期/永不过期+异步刷新**（知道名词即可）。

---

### 二、缓存三害（穿透 / 击穿 / 雪崩）— 用我的项目对齐

#### R1：**穿透**：查一个不存在的 hot id，绕过缓存打 DB？

1. **`PostDetailCache`** 用 **`NULL_SENTINEL` + 短 TTL** 占位，连环请求 **打在 Redis**。  
2. 其它业务可谈 **Bloom / 网关鉴权**。  
**Trade-off**：占一点内存，换 DB 护盾。

#### R2：**击穿**：热点 key **刚好过期**，并发同时 miss？

1. 现实方案：**mutex 单线程回源**、**逻辑过期异步重建**、`SETNX` 抢锁。  
2. 我当前的 demo：**短 TTL + 写少读多**可先扛；追问再展开。  
**Trade-off**：互斥会降低瞬时吞吐，要选热点 key。

#### R3：**雪崩**：大批量 key **同一时刻过期**？

1. **TTL 随机抖动**、`never expire + lazy refresh`。  
**Trade-off**：逻辑复杂 vs 瞬时 DB 峰值。

---

### 三、`boot-social-demo` / `dual-system-ms`：**MQ** 我讲什么？

| 维度 | **`boot-social-demo`** | **`dual-system-ms`** |
|------|-------------------------|----------------------|
| **事件** | 评论/点赞等触发的通知（学习计划里 RabbitMQ 消费）|`PaymentSucceeded`，支付 **Outbox 投递后**发往 **`dual.payment.topic`** |
| **语义** | 常见 **最少一次**，消费者需 **幂等** | **最少一次**，订单 **`processed_payment_events`/`eventId` 唯一** |
| **失败** | 重试、DLQ/开发控制器（按需了解） | **`payment_outbox` 失败 `retry_count` → FAILED**（需运维补发）；消费端异常看 **重试与不 ack** |

**顺序消息**：**单 partition + 同一 key** 才能保证严格顺序；我支付→订单仅用 **单笔业务 id** 粒度，不靠全局顺序。

**Trade-off**：MQ **解耦 + 峰值削峰**，代价是 **最终一致 + 运维队列**。

---

### 四、**Nacos / Gateway / Sentinel**（`dual-system-ms` 主讲）

| 组件 | **解决什么问题** | **Trade-off（一句话）** |
|------|------------------|--------------------------|
| **Nacos Discovery** | 服务实例列表 + 健康，`lb://` 做客户端负载 | 要强一致视图要 **推拉结合 + 订阅延迟** 心中有数 |
| **Spring Cloud Gateway** | 南北向入口、路由、**`X-Request-Id`/`X-Report-Key`** | 中心化故障点 → **多起 + 网关无状态 + 后端网络隔离** |
| **Sentinel（本地 demo）**| **资源限流**，如 `GET /api/orders/{id}` **QPS** | **限流失真误杀** vs **过载保护**，要 **metrics + 规则治理** |

（`boot-social-ms`：**同类栈练习**，话术可套用。）

---

### 五、「为什么不用 **Seata**？」— 口述段（对齐 W34 笔记）

1. 我的 **订单–支付跨库**，主路径已是 **本地事务 Outbox + MQ + 消费端幂等**，满足 **异步最终一致**，不需要 **全局 2PC 锁全链路**。  
2. **中间件成本高**：TC 高可用、与数据源/框架版本耦合；本练习 **更看重可交付与冒烟**。  
3. **MQ 与客户不可纳入 XA**：现实里 **`PaymentSucceeded`/外部回调**照样要 **幂等 + 对账**，Seata **省不掉**这层。  
4. 面试收口：**能用状态机与事件闭环讲清楚，就不必为 demo 扛 Seata 运维**。

---

### 六、组件 **trade-off** 速记（计划验收口诀）

| 组件 | Trade-off |
|------|-----------|
| **Redis** | 快内存 vs **一致性/击穿**要靠工程手段补 |
| **MQ** | 解耦异步 vs **重复消息 + 可见延迟** |
| **Nacos** | 省心注册 vs **依赖又多一个可用性面** |
| **Gateway** | 统一拦截 vs **单点/延迟一跳** |
| **Sentinel/限流** | 自保 vs **误伤正常流量** |

---

### Day 243 自测（计划验收）

- [ ] 背 **`post:detail:` + 两个 TTL + 何时 evict**。  
- [ ] **`PaymentSucceeded`**：**Outbox→队列→收件箱**，各 **一句话**。  
- [ ] 上表 **trade-off**：随意抽 **2 词**即兴扩成 **20 秒小段**。

---

## Day 244（周六）— 场景设计（10 行版 ×3 + 项目映射）

> 口述目标：每个场景 **5 分钟内**讲完「表 / 接口 / 风险 / 兜底」，**不靠现场列组件清单**。下面压缩成提纲，可按需拷贝到卡片上背诵。

---

### 场景① 简化秒杀 / 抢券

1. **表**：`sku`（sku_id / 名称）；**`inventory`**（sku_id **库存、`version`** 乐观锁 或单行 **FOR UPDATE**）。  
2. **表**：`order_kill`（user_id, sku_id, status, **`idempotency_key` UNIQUE**）——防用户连点。  
3. **接口**：`POST /seckill/try`（排队中/成功/已抢光）；`GET /seckill/result` 轮询。  
4. **网关层**：用户级 **QPS/token**（类似固定窗口计数）；黑名单 device。  
5. **库存扣减**：**数据库 CAS** `WHERE stock>0 AND version=?` **或** Redis **预热库存 + Lua 原子减 + 异步落 DB**。  
6. **风险**：**超卖** → **单行串行 / CAS + 幂等键**；**热点行** → **分队列 / 分 shard**（口述）。  
7. **风险**：突刺 DB → **先入队**，Worker **顺序消费**下单。  
8. **兜底**：**对账**（成功率 vs inventory 快照）；超长未支付 → **回补库存**。  
9. **限流兜底**：超限 **快速失败**，别让线程池无限排队。  
10. **与项目钩子**：`boot-social-demo` 的 **`WriteActionRateLimiter`** 是同类「热门资源 + 单机写保护」，口述时把 **postId+user → skuId+user** 类比即可。

---

### 场景② Feed 流 / 发帖列表

1. **表**：`posts`（id, user_id, content, created_at）；`follows`（follower, followee）若做关注流。  
2. **读模型 A（读扩散）**：拉关注人的帖子 **UNION + 时间序** — 实现简单，**大 V 读放大**。  
3. **读模型 B（写扩散）**：发帖时写 **fan-out 信箱表** `feed_inbox(user_id, post_id, ts)` — **读快写重**。  
4. **接口**：`GET /feed?cursor=` 游标分页；`POST /posts` 发帖。  
5. **缓存**：热点帖详情 **Redis**；**timeline** 可缓存「最近 N 条 id」短 TTL。  
6. **风险**：深分页 → **游标 / `WHERE id > ?`**；timeline 击穿 → **mutex / logical expire**。  
7. **风险**：作弊刷流 → **限流 + 内容安全异步**。  
8. **写读扩散选型**：小规模 **读扩散**，千万粉 **异步写信箱 + 分页拉取**。  
9. **兜底**：离线推荐、**回填冷数据**（点到为止）。  
10. **项目映射（主）**：**`boot-social-demo`**：**`GET /api/posts`** + **`PostMapper.pagePosts`**（keyword/userId/order + MP 分页）；详情 **`PostDetailCache`** **`post:detail:{id}`**；**没有做关注写扩散**，口述时主动说清楚边界。

---

### 场景③ 文件上传（S3 / MinIO）

1. **桶策略**：私有桶；严禁 **永久公开 PUT**。  
2. **Key**：`covers/{yyyy}/{uuid}` 或 `{tenant}/{userId}/{contentHash}`，避免文件名冲突。  
3. **接口**：`POST /upload/sign` 返回 **presigned PUT**（短 TTL、限制 Content-Type/MaxSize）；客户端直传对象存储。  
4. **完成回调**：`POST /posts/{id}/cover` 仅存 **`objectKey`**；前端展示用 **`presigned GET`** 短链。  
5. **鉴权**：sign 与用户 **JWT 绑定**，服务端校验 **MIME/大小**。  
6. **风险**：盗链 → GET **短时** + **CDN 私有签名**。  
7. **风险**：大文件 → **multipart 分片 + 合并**。  
8. **风险**：恶意文件 → 杀毒 / 后缀白名单。  
9. **兜底**：回调失败 → **定时扫描 orphan object GC**。  
10. **项目映射**：**`StorageService`**（`boot-social-demo`）：**`putObject` / `presignedGetUrl`**；**`CoverUploadResponse`** 含 **`objectKey` + 短期 coverUrl**；**`DevStorageController`** smoke 验证 MinIO + **AWS SDK Presigner**。

---

### Day 244 口述结构（每场 5 分钟照这个走）

| 段落 | ~时长 |
|------|-------|
| 需求一句 + QPS / 数据量级假设 | 30s |
| **核心表 2～3 张 + 为什么这么拆** | 90s |
| **2～3 个对外接口** | 60s |
| **最大风险及兜底** ×2（超卖 / 一致性 / 成本） | 90s |
| **项目映射 1 句**（本题相关） | 30s |

---

### Day 244 自测（计划验收）

- [ ] **三个场景**录音各 **≤5 min**，回放剪掉 **「然后、反正、各种」**。  
- [ ] **Feed** 能说清 **读扩散 vs 写扩散**，并指出 **本项目未实现关注写扩散**。  
- [ ] **上传**能说清：**DB 仅存 key**、浏览器拿 **presigned**。

---

## Day 245（周日）— 模拟面试 + 简历 bullet + 清单封口

> 验收：别人只看 **`INTERVIEW-QA.md` + `PROJECT-HIGHLIGHTS.md`** 也能回答「你会什么、做过什么」。  
> 题库：**Day240 Q1～10 + Day241 S1～8 + Day242 M1～8 + Day245 R1～8 ≈ 42 条可抽问题**（另有 Day239 STAR、Day242/243/244 项目段可当作追问扩展）。

---

### 一、45 分钟模拟面试台本（可按铃）

| 段 | 时长 | 你做什么 |
|----|------|----------|
| **开场** | 2 min | **自我介绍**：标签 3 词 + 代表项目 2 个 + 当前方向 1 句（忌背简历全文）。 |
| **项目 1 深挖** | 10 min | 假面试官只准问 **「为什么 / 如果重来 / 怎么验收」**；你主要讲 **`boot-social-demo`**：分层、缓存 key、限流、MQ/幂等边界各 **1 例**。 |
| **项目 2 深挖** | 10 min | 主谈 **`dual-system-ms`**：**Outbox→PaymentSucceeded→订单 PAID**、重复消息、报表 **`X-Report-Key`**、**为什么不用 Seata**（各能接一句话）。 |
| **基础抽问** | 15 min | 从 **Day240～242 + 下方 R1～R8** 里 **盲抽 5～6 题**；每题 **90～120s**，不会就说「我目前理解到…课后会补」。 |
| **场景/系统** | 5 min | **Day244 三选一**（秒杀 / Feed / 上传）走一遍 **5 分钟结构表**。 |
| **你问对方** | 3 min | 见下 **「反问面试官」**。 |

---

### 二、反问面试官（准备 2～3 句即可）

1. **团队当前后端主线**：Boot 版本、是否 K8s、CI（能否讲讲你们一次发布怎么走）？  
2. **业务侧**：我这个岗位对接的产品节奏（双周迭代 / on-call）与学习预期？  
3. **技术面**：服务端可观测统一用啥（tracing / 日志规范）？新人前 30 天一般敲哪块代码？

（忌问「加班多吗」作为唯一问题；可换成 **协作方式 / 文档文化**。）

---

### 三、简历项目 bullet — 写法公式（中英可选）

每条尽量：**动词 + 技术名词 + 可验证锚点**（脚本名、.Compose、指标、p99 若真能测再写）。

1. **`[动词]` + `[手段]` + `[结果/证明]`**：例 — **落地** **`payment_outbox` 与同事务**，**冒烟脚本** **`smoke-w34-dual-system.sh`** **一轮验** **confirm 幂等 + 报表鉴权**。  
2. **`[动词]` + `[手段]` + `[结果/证明]`**：例 — **`PostDetailCache`** **负缓存 + TTL**，发帖等写路径 **evict**，Redis 异常 **降级不挡主链路**。  
3. **`[动词]` + `[手段]` + `[结果/证明]`**：例 — **OpenAPI + `mvn verify` + Compose** 冒烟，保证接口契约 **可回归**。（按需替换为你的真实条目。）

**中英**：英文 bullet 用词 **past tense**（*Implemented*, *Designed*）；技术栈专有名词保留英文。

---

### 四、本周交付自检（封口清单）

| 文件 | 标准 | 你已做到？ |
|------|------|------------|
| **`INTERVIEW-QA.md`** | **≥ ~40** 条可单独抽问的口述单元（上表题库 + STAR/场景段） | [ ] |
| **`PROJECT-HIGHLIGHTS.md`** | **1 页内**，只保留 **可讲、可验** 的亮点 | [ ] |
| **简历** | 每项目 **3 条量化/可验证 bullet**（或学习项目写清 **如何一键验收**） | [ ] |
| **录音** | 至少 **1 次完整 45 min** 或 **分三段各 15 min** 连续说完 | [ ] |

---

### Day 245 随机抽问池（R1～R8，基础收口）

#### R1：GET 和 POST 的幂等性你怎么讲？

1. **HTTP 语义**：**GET 应安全且幂等**（多读不变更）；**POST 创建**默认 **非幂等**（重复点会多条）。  
2. **工程上**：写接口靠 **`Idempotency-Key` / 业务唯一单号** 防重，与动词无关。  
3. **项目**：支付 confirm、点赞等 **用 DB 唯一键或状态机** 接住重复请求。

#### R2：JWT 和 Session 各适合什么？

1. **Session**：服务端存状态，**吊销快**、**改密立刻失效**；要 **黏会话 / Redis**。  
2. **JWT**：**无状态验签**，跨服务方便；**吊销与续期**要额外设计（黑名单/短 access + refresh）。  
3. **项目**：`boot-social-demo` 用 **Sa-Token** ——口述时归到「框架帮我封了 cookie/token 与踢下线策略，我关注 **鉴权边界**」即可。

#### R3：502 / 504 区别（面向网关 / Nginx）？

1. **502 Bad Gateway**：网关 **连上上游但拿到非法/空响应**（进程挂了、协议错、连接被 RST）。  
2. **504 Gateway Timeout**：**等上游响应超时**（业务慢、线程池满、网络抖）。  
3. 排查：**下游日志 + 网关 access + `X-Request-Id` 串起来**。

#### R4：`mvn verify` 和 `mvn test` 差在哪？

1. **`test`**：跑单元测试（默认）。  
2. **`verify`**：走完整 **生命周期到 verify 阶段**，常包含 **集成测试、checkstyle、jacoco** 等绑定在 `verify` 的插件。  
3. 你说项目：**CI 用 verify 表示「合并前一套门槛」**。

#### R5：为什么列表接口常返回 DTO 而不是直接 `User` entity？

1. **稳定契约**：避免 **表字段一调** 就把 **密码哈希/内部字段** 漏出去。  
2. **聚合视图**：列表常要 **join 后的展示字段**，不是单表一行。  
3. **版本演进**：DTO 可 **加字段默认可空**，entity 更贴近持久化。

#### R6：你怎么看待「接口版本化」？

1. **URL 版本** `/v1/` 最直观；**Header** `Accept-Version` 也行。  
2. **兼容策略**：**只加字段少删**；删改走 **新 major + 文档周期**。  
3. 学习项目：**OpenAPI 一条服务说明** 也算「契约意识」。

#### R7：Docker Compose 与本机 `spring-boot:run` 你怎么选讲？

1. **Compose**：**依赖齐**（MySQL/Redis/MQ），**别人机器可复现**，接近「小集成环境」。  
2. **本机 run**：改代码 **快**，但 **易「我电脑能跑」**。  
3. 面试一句：**对外演示用 Compose + 冒烟脚本；开发时本机 profile**。

#### R8：线上问题第一步你怎么切？（无真线上也可答「演练口径」）

1. **时间与范围**：故障点 ~ 影响面（接口 / 用户比例）。  
2. **关联 ID**：`**X-Request-Id`/traceId** 从网关穿到日志。  
3. **分层猜**：网关超时 / 应用异常 / DB 慢 / 下游 MQ 堆积 —— **先证伪再深挖**。  

---

### Day 245 自测（计划验收）

- [ ] **完整走表「45 分钟台本」1 次**（可自拍视频，重点听 **项目 2** 是否讲成「我与同事/系统边界」而非背文件名）。  
- [ ] **`PROJECT-HIGHLIGHTS.md`** 打印或 PDF **≤1 页**，读给非技术朋友听 —— 对方能复述 **两个项目各 1 句**。  
- [ ] **简历每项目 3 bullet** 已填，且每条能指到 **仓库里一个类/脚本/compose 服务**。  
- [ ] **R1～R8** 盲抽 **4 题**，各 **90s** 不脱稿。
