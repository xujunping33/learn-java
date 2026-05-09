# 第 25 周学习计划：Spring Boot 3（Jakarta）+ JDK 21 + MyBatis（/MyBatis-Plus）

> 对应总纲：**第25–26周（Spring Boot 3 + JDK21 新特性 + MyBatis/MyBatis Plus）**  
> 本周定位：先把 **Spring Boot 3 的工程化“正确姿势”**跑通，并把 W24 的 SSM 社交主流程迁移到 Boot 项目里。  
> 时间：约 **2 小时/天**；本周 **Day 169 ~ Day 175**。  
> 说明：W26 再加深（MyBatis-Plus、更系统的配置/部署、接口文档等）。

---

## 本周总目标

- **能从 0 起一个 Boot 3 项目**：分层清晰、配置清晰、日志清晰、可测试。
- **掌握 Boot 做了什么**：自动配置、starter、配置绑定、profiles（dev/test）。
- **REST 工程化**：统一返回体、统一异常、参数校验（Validation）、规范状态码。
- **数据库整合**：MyBatis + MySQL + 连接池（Hikari 默认即可），事务边界在 Service。
- **可运行交付物**：一个 Boot Web 应用，包含（至少）注册登录 + 发帖 + 列表/详情 + 评论/点赞（可先简化 UI，重点是后端）。

---

## 项目建议（本周就做一个“能持续迭代”的 Boot 项目）

新建 Maven 模块：`boot-social-demo/`

- 包名建议：`learn.java.bootsocial`
  - `web`：controller / dto / exception / interceptor
  - `service`
  - `mapper`
  - `model`
  - `config`
- 端口建议：`8081`（避免跟你 Tomcat/其它 demo 冲突）
- 配置文件：
  - `application.yml`
  - `application-dev.yml`
  - `application-test.yml`

---

## Day 169（周一）— Boot 3 起步：第一个可运行 API + Profiles

**学什么**

- Boot 的最小启动：`@SpringBootApplication`、内置 Tomcat、starter。
- Profiles：`dev/test` 的差异（端口、日志、数据库）。

**做什么**

1. 创建 `boot-social-demo`（JDK 21）：
   - `spring-boot-starter-web`
   - `spring-boot-starter-validation`
   - `spring-boot-starter-test`
2. 写 `GET /api/ping`。
3. 写配置：
   - `application.yml`：通用配置 + `spring.profiles.active=dev`
   - `application-dev.yml`：`server.port=8081`、更详细日志
4. 用 `mvn -q test` 与 `mvn -q spring-boot:run` 验证能跑。

**验收**

- `curl http://localhost:8081/api/ping` 返回 JSON；`mvn -q test` 全绿。

---

## Day 170（周二）— 数据库接入：MyBatis + MySQL + 第一张表

**学什么**

- Boot 下 MyBatis 整合的最小套路：mapper 扫描、XML/注解二选一。
- 连接池与 datasource 配置（先用 Hikari 默认）。

**做什么**

1. 引入 MyBatis：
   - `mybatis-spring-boot-starter`
   - `mysql-connector-j`
2. 配 `spring.datasource.*`（dev 指向你的本机 MySQL）。
3. 迁移 W24 的建表 SQL 到 `boot-social-demo/src/main/resources/sql/`（至少 `users`）。
4. 写 `UserMapper`：`insert`、`findByUsername`、`findById`。
5. 写一个最小 `UserService`，并做一个 `CommandLineRunner` 或简单 controller 验证连库成功。

**验收**

- 启动无报错；能插入/查询用户；SQL 日志可读（至少 dev 环境打开）。

---

## Day 171（周三）— 注册 / 登录 / 退出（Session 版）+ 参数校验

**学什么**

- `@Valid` + `jakarta.validation`：`@NotBlank/@Size` 等。
- Session 登录态：`HttpSession`；以及“未登录 → 401”的统一处理方式。

**做什么**

1. DTO：
   - `RegisterRequest` / `LoginRequest`（带 `@Valid` 校验）
   - `MeResponse`
2. API：
   - `POST /api/auth/register`
   - `POST /api/auth/login`
   - `POST /api/auth/logout`
   - `GET /api/me`
3. 把密码算法封装成 `Passwords`（可以先沿用你 OA/SSM 的 demo 方案）。
4. 加统一异常处理（先给框架，Day 172 补齐细节）。

**验收**

- 注册 → 登录 → me 成功；参数不合法返回 400 且错误体结构固定。

---

## Day 172（周四）— 统一返回体 + 统一异常 + 状态码规范

**学什么**

- `@RestControllerAdvice`：把错误响应“集中管理”。
- `ResponseEntity`：状态码/headers/body。

**做什么**

1. 统一返回体（建议两类）
   - `ApiResult<T>`：成功响应
   - `ApiError`：错误响应（含 `code/message/details`）
2. 统一异常：
   - 业务异常：`BizException(code, message)`
   - 参数校验异常：`MethodArgumentNotValidException`
   - 兜底异常：`Exception`
3. 把 controller 里的分散校验/try-catch 去掉，让它变“薄”。

**验收**

- 401/404/400/500 都有一致的 JSON 格式；前端/脚本很好处理。

---

## Day 173（周五）— 帖子：发布 / 列表 / 详情（含作者）

**学什么**

- 典型 service 编排与事务边界（写操作 `@Transactional`）。
- 列表查询的最简分页：`page/size` → `limit/offset`（先把形态立住）。

**做什么**

1. 迁移 `posts` 表与 `PostMapper`。
2. API：
   - `POST /api/posts`（需登录）
   - `GET /api/posts?page=&size=`
   - `GET /api/posts/{id}`
3. 返回体包含作者用户名与 `createdAt`。

**验收**

- 未登录发帖 401；登录后发帖成功；列表/详情能看到作者字段；分页参数生效。

---

## Day 174（周六）— 评论 + 点赞（防重复）+ 聚合返回

**学什么**

- “写操作幂等”：点赞用 UNIQUE（post_id,user_id）防重复。
- 详情聚合：post + comments + likeCount（一个接口就能拿齐）。

**做什么**

1. 迁移 `comments` / `post_likes` 表与 mapper。
2. API：
   - `POST /api/posts/{id}/comments`
   - `POST /api/posts/{id}/like`
   - （可选）`DELETE /api/posts/{id}/like`
3. `GET /api/posts/{id}` 返回：
   - post 基本信息
   - comments 列表
   - likeCount

**验收**

- 重复点赞不会重复计数；评论列表稳定；详情接口一次拿齐信息。

---

## Day 175（周日）— 拦截器鉴权（Session）+ 测试 + 可复现 README

**学什么**

- Boot 下的 interceptor 注册方式（`WebMvcConfigurer`）。
- 测试分层：
  - `@WebMvcTest`（只测 controller）
  - `@SpringBootTest`（端到端/集成）

**做什么**

1. `AuthInterceptor`：保护写接口（posts/comments/like）。
2. 测试（至少 3 个）
   - 未登录写接口返回 401（MVC test）
   - 参数校验失败返回 400（MVC test）
   - 一个最小的集成测试（可选：用 Testcontainers 放到 W26；本周先占位或用本地库）
3. 写 `boot-social-demo/README.md`
   - 启动方式
   - 配置说明（dev/test）
   - curl 主流程（注册/登录/发帖/评论/点赞）

**验收**

- README 让你 10 分钟内从 0 跑起来；最少 3 个测试可重复跑；关键写接口都受 session 保护。

---

## 本周交付清单（必须）

- `W25_PLAN.md`（本文件）
- `boot-social-demo/`（Spring Boot 3 项目，能跑）
- 最少跑通主流程：注册登录 + 发帖 + 列表/详情 + 评论/点赞
- `boot-social-demo/README.md` + `smoke-boot-social.sh`（curl 脚本）
- 最少 3 个测试用例

---

## 现在开始：Day 169

今天先把“Boot 工程起得漂亮”作为唯一目标：

1. 创建 `boot-social-demo`（JDK 21），跑通 `GET /api/ping`。  
2. 配好 `application.yml` + `application-dev.yml`（端口 8081）。  
3. `mvn -q test` 全绿。  

做完把：`pom.xml`、`application*.yml`、以及 `curl /api/ping` 输出贴我，我再带你把 Day 170 的 MyBatis 配置一次性写稳（避免常见的 mapper 扫描/驼峰映射/时区坑）。

