# 第 24 周学习计划：SSM 社交网站实战（Spring + Spring MVC + MyBatis）

> 对应总纲：**第24周（SSM社交网站实战）**  
> 目标：做一个更贴近企业的项目结构（用户、内容、评论、缓存、审核等）。  
> 交付物：至少完成“**用户注册登录 + 列表/详情 + 评论/点赞**”的主流程。  
> 时间：约 **2 小时/天**；本周 **Day 162 ~ Day 168**。  
> 技术约束：**不使用 Spring Boot**（Spring Boot 3 在 W25–W26）。

---

## 本周总目标（做完你就具备 SSM 项目基本盘）

- **项目形态**：一个可部署 Tomcat 的 `war` 工程，分层清晰（web/controller → service → repository/mapper）。
- **会话登录**：注册/登录/退出，使用 **HttpSession** 维持登录态；未登录访问受保护接口返回 401。
- **内容流**：发帖（内容）、列表、详情（含评论列表）。
- **互动**：评论、点赞（防重复点赞）。
- **工程化**：统一返回体 + 统一异常；日志可读；SQL 可追踪；最少量的 smoke 脚本（curl）。
- **可选加固（本周末加）**：简单缓存（内存）与审核状态（draft/published/blocked）。

---

## 本周项目建议（你照这个建就不容易乱）

新建 Maven 模块：`ssm-social-demo/`（`packaging: war`）

- **核心依赖**（建议在本周逐日补齐）
  - Spring：`spring-context` / `spring-jdbc` / `spring-tx`
  - Spring MVC：`spring-webmvc`
  - MyBatis：`mybatis` + `mybatis-spring`
  - DB：`mysql-connector-j` + 连接池（`druid` 或你已熟悉的一个）
  - JSON：`jackson-databind`
  - 日志：`slf4j` + `logback`
  - Servlet：`jakarta.servlet-api`（`provided`）

- **包结构**（建议）
  - `learn.java.ssmsocial`
    - `web`：controller / interceptor / dto / exception
    - `service`：业务编排（事务边界）
    - `mapper`：MyBatis Mapper 接口
    - `model`：实体（User/Post/Comment/Like…）
    - `config`：Spring / MVC / MyBatis / DataSource 配置（Java Config 优先）

---

## 数据库设计（最小可用）

数据库：`learn_java`（或新建 `ssm_social` 也行，选一个固定即可）

**表（建议 4 张起步）**

- `users`
  - `id` bigint PK
  - `username` varchar UNIQUE
  - `password_hash` varchar（先用你 OA 的 MD5+salt 或更简单的 demo 方案；W25 再换更工程化）
  - `salt` varchar（可选）
  - `created_at` datetime
- `posts`
  - `id` bigint PK
  - `user_id` bigint FK(users.id)
  - `title` varchar
  - `content` text
  - `status` tinyint（可选：0草稿/1发布/2屏蔽）
  - `created_at` datetime
- `comments`
  - `id` bigint PK
  - `post_id` bigint FK(posts.id)
  - `user_id` bigint FK(users.id)
  - `content` text
  - `created_at` datetime
- `post_likes`
  - `id` bigint PK
  - `post_id` bigint FK(posts.id)
  - `user_id` bigint FK(users.id)
  - `created_at` datetime
  - UNIQUE(`post_id`,`user_id`)（防重复点赞）

**索引最低配**

- `posts(user_id, created_at)`
- `comments(post_id, created_at)`
- `post_likes(post_id)` 与 UNIQUE 组合即可

---

## Day 162（周一）— 需求拆解 + 建库建表 + 项目脚手架

**学什么**

- SSM 项目里：Spring 管 Bean、Spring MVC 管请求、MyBatis 管 SQL；事务边界一般在 Service。

**做什么**

1. 建库建表：写 `sql/ssm_social_schema.sql`（可执行的一份建表脚本）。
2. 创建 `ssm-social-demo`（war）：能 `mvn -q package` 出 WAR。
3. 跑通最小接口：
   - `GET /api/ping` → `{"ok":true}`
4. 配好日志：启动时能看到清晰的启动日志（至少 controller 里打一次 log）。

**验收**

- SQL 脚本可从 0 建出表；WAR 可部署 Tomcat；`/api/ping` 可 `curl` 访问成功。

---

## Day 163（周二）— MyBatis + DataSource + 第一个 Mapper

**学什么**

- `SqlSessionFactory`、Mapper 扫描、`@Mapper`/XML 映射、连接池参数。

**做什么**

1. 加上 DataSource（连接池）+ MyBatis 配置（Java Config）。
2. 完成 `UserMapper`：
   - `insertUser`
   - `findByUsername`
   - `findById`
3. 用一个最小的 `UserService` 做“创建用户（暂不含登录）”的通路。

**验收**

- 通过接口或简单 smoke 脚本，能插入一条用户并查出来；SQL 日志能看清楚执行了什么。

---

## Day 164（周三）— 注册 / 登录 / 退出（Session 登录态）

**学什么**

- Session 登录态：登录成功后写入 `session.setAttribute("uid", ...)`。
- 接口约定：统一返回体、错误码、401/403/400 的边界。

**做什么**

1. REST API（建议）
   - `POST /api/auth/register`：用户名/密码
   - `POST /api/auth/login`
   - `POST /api/auth/logout`
   - `GET /api/me`：返回当前用户信息（用于验证 session）
2. 密码策略：先做可重复的 demo（例如 `salt+md5`），并把算法封装到 `Passwords` 工具类。
3. 统一异常：`ApiException` + `@ControllerAdvice` 输出统一错误体。

**验收**

- 注册 → 登录 → `me` 成功；退出后 `me` 返回 401。

---

## Day 165（周四）— 发帖 / 列表 / 详情（含作者信息）

**学什么**

- 典型分层：controller 只做入参/鉴权/返回；service 做编排与事务；mapper 只做 SQL。
- 关联查询：post + author（用户名）。

**做什么**

1. `PostMapper` + `PostService`：
   - `createPost`（需登录）
   - `listPosts`（按时间倒序，先不分页或先做最简分页：limit/offset）
   - `getPostDetail`（含作者信息）
2. API（建议）
   - `POST /api/posts`
   - `GET /api/posts`
   - `GET /api/posts/{id}`

**验收**

- 未登录发帖 401；登录后能发帖；列表/详情能看到作者字段。

---

## Day 166（周五）— 评论（新增 + 列表）+ 详情页聚合

**学什么**

- “详情页聚合”：一个接口里返回 post + comments（或拆成两个接口，前端联调更常见；这里你二选一）。

**做什么**

1. `CommentMapper`：
   - `insertComment`
   - `listCommentsByPostId`
2. API（建议）
   - `POST /api/posts/{id}/comments`（需登录）
   - `GET /api/posts/{id}/comments`
3. 详情聚合（可选但推荐）
   - `GET /api/posts/{id}` 返回 comments（或提供 `includeComments=true` 参数）

**验收**

- 登录后可评论；评论列表按时间正序或倒序稳定；详情能拿到评论数据。

---

## Day 167（周六）— 点赞（防重复）+ 计数

**学什么**

- 用数据库 UNIQUE 防重复点赞；如何把“点赞数”查出来（聚合查询）。

**做什么**

1. `PostLikeMapper`：
   - `like(postId, userId)`（插入，冲突视为已点赞）
   - `unlike(postId, userId)`（可选）
   - `countLikes(postId)`
2. API（建议）
   - `POST /api/posts/{id}/like`
   - `DELETE /api/posts/{id}/like`（可选）
   - `GET /api/posts/{id}` 或 `GET /api/posts` 返回 `likeCount`

**验收**

- 同一用户对同一 post 多次点赞不会重复计数；likeCount 正确。

---

## Day 168（周日）— “更像企业”的收尾：拦截器鉴权 + 最小缓存/审核（可选）+ README

**学什么**

- Spring MVC Interceptor 做登录鉴权（复用你 W23 的经验，但这里改成 Session）。
- 缓存与审核：不是为了“功能更多”，而是体验一次“需求 → 结构扩展”。

**做什么**

1. `AuthInterceptor`：保护写操作接口（发帖/评论/点赞等）。
2. （可选其一）简单缓存
   - 内存缓存 `postDetail` 30 秒（`ConcurrentHashMap` + 时间戳）
3. （可选其一）审核状态
   - `posts.status`：只有 `published` 才能在列表出现；新增 `POST /api/posts/{id}/publish`（作者操作）
4. 写 `ssm-social-demo/README.md`
   - 如何建库、如何配置 DB、如何部署 Tomcat、curl 示例（至少注册/登录/发帖/评论/点赞）

**验收**

- README 可让你未来 10 分钟内从 0 跑起来；核心主流程全通。

---

## 本周交付清单（必须）

- `W24_PLAN.md`（本文件）
- `sql/ssm_social_schema.sql`（可一键建表）
- `ssm-social-demo/`（WAR，可部署 Tomcat）
- `ssm-social-demo/README.md`（可复现）
- 一个 `smoke-*.sh`（curl 脚本）跑主流程

---

## 现在开始：Day 162

今天只做三件事就算达标：

1. 写出 `sql/ssm_social_schema.sql`（建表 + 必要索引）。  
2. 新建 `ssm-social-demo`（war）并跑通 `GET /api/ping`。  
3. `mvn -q package` 能稳定出 WAR。  

做完把：**建表 SQL**、`pom.xml` 依赖区块、以及 `curl /api/ping` 输出贴我，我再带你把 Day 163 的 MyBatis + DataSource 配置一次性写对。

