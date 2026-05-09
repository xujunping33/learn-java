# 第 25 周笔记（Spring Boot 3 + JDK 21 + MyBatis：Boot 社交 API）

本周定位：**从 0 搭一个 Boot 3 工程**，把 W24 SSM 里的「注册登录 + 帖 + 评 + 赞」后端主流程搬到 **`boot-social-demo`**，并完成工程化：统一返回/异常、Session 登录、Profiles、拦截器鉴权、可跑测试与 curl 冒烟。

时间安排对应计划：**Day 169 ~ Day 175**。

---

## 本周总览

### 学习目标

| 主题 | 要点 |
|------|------|
| Spring Boot 3（Jakarta） | `@SpringBootApplication`、starter、嵌入式 Tomcat、自动配置取舍（test 下可排除 JDBC/MyBatis） |
| Profiles | `dev` 连 MySQL、`8081`；`test` 轻量（无库）便于 `mvn test` |
| REST 约定 | `ApiResult<T>` 成功、`ApiError` + `BizException` + `@RestControllerAdvice` 一致错误体 |
| MyBatis | `mapper-locations`、`@Mapper`、`resultMap`，列表/详情 `JOIN`，点赞数子查询/`COALESCE` |
| Session 会话 | `HttpSession` 存 `uid`；发帖/评论/点赞等写接口必须登录 |
| 拦截器 | `HandlerInterceptor` + `WebMvcConfigurer#addInterceptors`，与 Controller 分层（Controller 变薄） |

### 交付物清单

- `boot-social-demo/`：`learn.java.bootsocial.{web,service,mapper,model,auth,config}`
- `README.md`：启动、环境变量、`dev/test`、API 一览
- **`smoke-boot-social.sh`**：注册 → 发帖 → 列表 → 详情 → 评论 → 点赞 → 再拉聚合详情
- **`smoke-day174.sh`**（可选加严）：双用户点赞幂等、`unlike` 后计数回退等
- 测试：`PingTest`、`PostMvcTest`、`AuthInterceptorTest`（`mvn test`）

---

## Day 169：Boot 3 起步 + Profiles

### 学习要点

- **`spring-boot-starter-web`**：内嵌 Servlet 容器，`DispatcherServlet` 与 MVC 开箱即用。
- **`application.yml` + Profile**：默认 `spring.profiles.active=dev`；**`application-dev.yml`** 单独放端口与数据源友好配置。
- 最小自检接口：**`GET /api/ping`** → `ApiResult.ok(true)`。

### 交付

- 模块 `boot-social-demo`（JDK 21，`spring-boot-starter-parent` 3.x）
- `application-test.yml`：后续用于测试时关掉数据源/MyBatis（见 Day 175）

---

## Day 170：MySQL + MyBatis + User

### 学习要点

- **`mybatis-spring-boot-starter`**：`mybatis.mapper-locations` 指向 `classpath*:mappers/*.xml`。
- **Mapper 接口**加 `@Mapper`（或全局 `@MapperScan`），XML 中与 `model`、列名对齐；`map-underscore-to-camel-case` 减少样板代码。
- 连接池：**Hikari**（Boot 默认），`spring.datasource.*` + 环境变量 `DB_URL / DB_USER / DB_PASSWORD`。

### 交付

- `UserMapper` + `UserService`，以及连库自检（Runner 或通过 API）
- SQL 可参考 W24 **`ssm_social`** schema，库表需含 **`users`、`posts`、`comments`、`post_likes`** 等再走全链路

---

## Day 171：注册 / 登录 / 退出（Session）+ `@Valid`

### 学习要点

- **DTO**：`RegisterRequest`、`LoginRequest` 使用 Jakarta **`jakarta.validation`**（`@NotBlank`、`@Size` 等）。
- **Session**：`SessionKeys.UID`，注册/登录成功后写入；`/api/me` 未登录 → **`BizException` 401**（由全局异常处理统一成 JSON）。
- **密码**：`Passwords` 封装哈希（可与 OA/SSM demo 对齐思路）。

### 接口

- `POST /api/auth/register`、`POST /api/auth/login`
- `POST /api/auth/logout`（204）
- `GET /api/me`

---

## Day 172：统一返回体 + `@RestControllerAdvice`

### 学习要点

- **`ApiResult<T>`**：成功固定形状 `ok + data`，便于脚本 `jq`/Python 解析。
- **`ApiError`**：`code`、`message`、`details`（校验错误可带字段明细）。
- **集中异常**：`MethodArgumentNotValidException` → **400** + `VALIDATION_FAILED`；`BizException` 自定义 HTTP 状态与业务码；`Exception` 兜底 **500**。

### 实践要点

Controller 不写重复 try/catch；业务语义用 **`BizException`** 表达。

---

## Day 173：发帖 / 列表 / 详情（含作者）

### 学习要点

- **事务**：写在 **Service**，`@Transactional` 包住发帖等写操作。
- **分页**：`GET /api/posts?page=&size=` → `LIMIT/OFFSET`（并限制 `size` 上限）。
- **`PostMapper`**：列表与详情 **`JOIN users`** 取 **`author_username`**；详情 `findDetailById`。

### 接口

- `POST /api/posts`（需登录，201）
- `GET /api/posts?page=&size=`（可读）
- `GET /api/posts/{id}`（可读；Day 174 起升级为聚合详情，见下文）

---

## Day 174：评论 + 点赞幂等 + 聚合详情

### 学习要点

- **评论**：表 `comments`；按 `created_at`（及 `id`）排序；JOIN `users` 得到评论者用户名。
- **防重复点赞**：`(post_id, user_id)` 唯一约束 + **`INSERT IGNORE`**（或服务层等价幂等）。
- **聚合**：单个 **`GET /api/posts/{id}`** 返回 post 基本信息、**`comments` 列表**、**`likeCount`**（与列表一致用子查询/聚合算出）。

### 接口

- `POST /api/posts/{id}/comments`（201）；`GET /api/posts/{id}/comments`
- `POST /api/posts/{id}/like`、`DELETE /api/posts/{id}/like`（204）
- `GET /api/posts/{id}` → **`PostDetailResponse`**

---

## Day 175：拦截器鉴权 + 测试 + README / smoke

### 学习要点

- **`AuthInterceptor`**：在 **`preHandle`** 里按 **HTTP Method + URI** 判断是否为「帖子域写路径」；只对 **`POST /api/posts`**、`POST …/comments`、`POST|DELETE …/like`** 校验 Session。
- **`WebMvcConfigurer`**（`DevWebConfig`）：`registry.addInterceptor(...).addPathPatterns("/api/posts", "/api/posts/**")`；**GET 列表/详情/评论列表**仍匿名可读。
- **Controller**：写接口改用 **`@SessionAttribute(SessionKeys.UID) Long userId`**，避免与拦截器重复的 `if (!(uid instanceof Long))`。
- **测试分层**：
  - **`@WebMvcTest`** + `@MockBean`：未登录发帖 **401**，带 Session + 非法 body **400**，并 **`@Import`** 全局异常与会拦截器配置。
  - **`@SpringBootTest` + RandomPort**：`test` profile 下探活 **`GET /api/ping`**（无 MySQL）。
  - **纯单元**：拦截器路径规则 `requiresAuth` 单独测，锁住「别把 GET 写坏」这类回归。

### 验收脚本

```bash
cd boot-social-demo
export DB_PASSWORD='...'   # 按本机MySQL修改
mvn -q spring-boot:run      # dev，默认 8081

# 另一终端
chmod +x smoke-boot-social.sh
BASE=http://127.0.0.1:8081 ./smoke-boot-social.sh
```

---

## 与 W24（SSM）的对照小结

| 维度 | W24 `ssm-social-demo` | W25 `boot-social-demo` |
|------|------------------------|---------------------------|
| 打包/运行 | WAR → 外置 Tomcat | 可执行 JAR / `spring-boot:run` |
| 配置方式 | Java Config + `@PropertySource` | `application-*.yml` + Profiles |
| MyBatis 接入 | Spring XML / `SqlSessionFactoryBean` | Starter 自动配置 + `mapper-locations` |
| 鉴权位置 | Servlet `Filter` 或手写 session 判断 | **`HandlerInterceptor` + `@SessionAttribute`** |
| 业务形态 | 同：Session、帖评赞、`ApiResult`/`ApiError` | 同上，API 前缀与语义对齐便于迁移认知 |

---

## 常见问题（FAQ）

1. **`mvn test` 不报 JDBC 错，但本地 run 报错**  
   **`test`** profile 排除了 DataSource/MyBatis；真正连库必须用 **`dev`**（或等价配置），并导出 **`DB_*`**。

2. **发帖 401**  
   Cookie/Session 未带上（`curl -c/-b`）；或走的是写路径但未先 `register/login`。

3. **点赞数不涨**  
   查库唯一约束、`INSERT IGNORE` 是否生效；同一用户重复点赞本来就不应计数增加。

---

## 下周预习（W26 方向）

- MyBatis-Plus、分页插件、批量与代码生成  
- Swagger/OpenAPI 文档、更系统的 Docker / 部署  
- 若想接近生产：**JWT**、刷新令牌、或与 **Spring Security** 官方链路的对比（仍为学习延伸）
