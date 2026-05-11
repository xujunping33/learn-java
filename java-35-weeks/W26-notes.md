# 第 26 周学习笔记（W26）— `boot-social-demo` 工程化与坑点

> 对应计划：[W26_PLAN.md](W26_PLAN.md)。工程目录：`boot-social-demo/`。  
> 本文记 **实现时容易踩坑的点** 和 **验收/联调习惯**，方便以后周次或 CI 对照。

---

## 1. MyBatis-Plus（Day 176–177）

### 1.1 `PaginationInnerInterceptor` 找不到类

从 **3.5.9** 起分页相关实现拆到单独依赖，仅加 **`mybatis-plus-spring-boot3-starter`** 可能编译报「找不到 `PaginationInnerInterceptor`」。  
**处理**：`pom.xml` 再显式加 **`mybatis-plus-jsqlparser`**（版本与 MP 对齐）。

### 1.2 与原生 MyBatis XML 共存

**`UserMapper`** 典型双栈：`extends BaseMapper<User>`，同时保留 XML 的 **`insertUser` / `findById`**；按用户名查询若再留 XML **`findByUsername`** 容易与 **`LambdaQueryWrapper`** 重复维护，选一种即可（本仓库已去掉 XML 的 `findByUsername`）。

### 1.3 `test` profile 要排除 MP 自动配置

原先用 **`MybatisAutoConfiguration`** 排除；换 MP 后改为排除 **`MybatisPlusAutoConfiguration`**，否则 **`PingTest`** 仍会尝试拉数据源/MyBatis。

### 1.4 复杂 JOIN 列表 + 分页

帖子列表带 **`users` 用户名**、**`post_likes` 聚合**，不适合硬套单表 **`LambdaQueryWrapper.selectPage`**。  
**做法**：自定义 **`pagePosts(Page<Post> page, …)`** XML（**不要手写 LIMIT**），由 **`PaginationInnerInterceptor`** 改写成 count + limit。  
**筛选条件** 用 XML **`<where>`** 动态 SQL；与「纯 Wrapper」是工程上的折中，分页仍是 MP 标准路径。

### 1.5 列表返回结构变更

**`GET /api/posts`** 的 **`data`** 从「数组」改为 **`PageResult`（`items` / `total` / `page` / `size`）** 后，所有脚本里若写 **`d['data']`** 当列表会挂，要改成 **`d['data']['items']`**（如 `smoke-boot-social.sh`）。

---

## 2. springdoc / Swagger（Day 178）

### 2.1 与 Spring Boot 3.5 的版本

**Spring Boot 3.5** 需 **springdoc 2.8.9+**（本仓库用 **`springdoc.version` 2.8.13**），否则可能遇到与 Spring 内部 API 不兼容的问题。

### 2.2 OpenAPI 里「方案名」和「Cookie 名」不是一回事

安全方案在组件里注册的是 **逻辑名**（本仓库为 **`sessionCookie`**，常量 **`OpenApiConfiguration.SECURITY_SCHEME_NAME`**），**`@SecurityRequirement`** 引用的是这个名字；**真正浏览器里的 Cookie 名** 由 **`SecurityScheme#name(cookieName)`** 决定，与 **`app.session.cookie-name`** 一致。改 Session cookie 配项后，Swagger **Authorize** 里填的域名也要跟着变。

### 2.3 只把业务 API 写进文档

**`springdoc.paths-to-match: /api/**`** 时，Swagger 文档里只看到业务接口；**`/actuator`、`/swagger-ui` 不受影响**，仍可访问。

### 2.4 Session 在 UI 里怎么测

同源下先 **`register` / `login`**，浏览器会带上 **`Set-Cookie`**；或从开发者工具复制 cookie 后在 **Authorize** 按配置的 cookie 名粘贴。

---

## 3. Actuator（Day 179）

### 3.1 暴露范围

全局 **`management.endpoints.web.exposure.include: health,info`**，**不要用 `*`**，避免环境里意外打开 **env、beans** 一类敏感端点。

### 3.2 `show-details`

根配置 **`never`** 减少默认信息泄露；**`dev`** 里覆盖为 **`always`**，便于本地看 **`db` / datasource** 子项是否为 **UP**。

### 3.3 `/actuator/info` 里的 `build`

**`spring-boot-maven-plugin`** 配置了 **`build-info`** goal 后才会有 **`BuildProperties`**，自定义 **`BootSocialInfoContributor`** 才能打出 **`artifact` / `version` / `time`**；只跑 **`compile`** 不跑 **`process-resources`** 时也要注意是否生成了 **`META-INF/build-info.properties`**（一般 **`package` / `test` / `verify`** 会带上）。

### 3.4 `UP` / `DOWN`

**UP** 表示参与聚合的健康检查通过；**DOWN** 表示有关键项失败（常见为库连不上）。探活一般看总体 **status** 与 HTTP 状态码（整体 DOWN 时多为 **503**，依网关/配置而定）。

---

## 4. `@ConfigurationProperties`（Day 180）

### 4.1 嵌套类绑定

**`app.api` / `app.cors` / `app.session`** 用 **JavaBean 式 getter/setter** 内嵌类即可；对外 **`@Validated`** + 字段 **`@Min`/`@Max`** 可在启动期校验非法默认分页大小。

### 4.2 分页默认值与 Controller

**`size`** 用 **`Integer` + required=false**，在方法里再用 **`AppProperties#getDefaultPageSize()`** 补默认；若在 **`@RequestParam(defaultValue="20")`** 写死会与配置双来源冲突。

### 4.3 WebMvcTest 重复注册 `AppProperties`

**`@EnableConfigurationProperties(AppProperties.class)`** 已与 **同一 `@ConfigurationProperties` Bean** 同时 **`@Import(AppProperties.class)`** 时会出现 **两个 `AppProperties` Bean**，导致 **`DevWebConfig`** 注入失败。**只保留 **`@EnableConfigurationProperties`**即可。

### 4.4 CORS 与 **`allowCredentials`**

**`allowCredentials(true)`** 时 **不能用 `*` 作为 allowed origin**，必须列具体前端 origin；**`origins` 为空** 则不注册全局 CORS，行为与从前一致。

### 4.5 Session Cookie 与 `server.servlet.session.cookie.name`

用 **`server.servlet.session.cookie.name: ${app.session.cookie-name}`** 保持 Tomcat Session 名与 **`AppProperties`**、OpenAPI 描述一致。

---

## 5. JDK 21 小改动（Day 181）

- 对外 **DTO** 早已统一为 **`record`** + **`jakarta.validation`** + **`@Schema`**，Jackson 一般用参数名映射 JSON 字段即可。
- **`PostController.longOrZero(Long)`**：用 **`switch (v) { case null -> 0L; case Long n -> n; }`** 替代多处三元式，语义等价、对外 JSON 不变。

---

## 6. Testcontainers + Failsafe（Day 182）

### 6.1 **`mvn test` vs `mvn verify`**

- **Surefire**：`**/*Test`、`**/*Tests`，**排除了 `**/*IT.java`**。
- **Failsafe**：只跑 **`**/*IT.java`**，挂在 **`integration-test` / `verify`**。  
日常无 Docker：**`mvn test`**；本机/RD 装了 Docker：**`mvn verify`** 才跑 **`SocialFlowIT`**。

### 6.2 无 Docker：跳过而非失败

**`@Testcontainers(disabledWithoutDocker = true)`** 在无 Docker 环境下会让 **`SocialFlowIT` 被 skipped**，**`verify` 仍可成功**。控制台可能出现 Testcontainers 「找不到 Docker」的 **ERROR**，属探测日志；**Failsafe 报告里 `skipped=1`**。

### 6.3 初始化脚本

**`withInitScript("testcontainers-schema.sql")`** 在容器已创建的数据库上执行；脚本里 **不要** 再 **`CREATE DATABASE` / `USE`**，否则与容器内置库名/JDBC URL 容易打架。表结构可与本路径下 **`sql/ssm_social_schema.sql`** 对齐。

### 6.4 无 Docker 的替代路线（计划中的 B）

手工起 MySQL，执行 **`sql/ssm_social_schema.sql`**，用环境变量或 profile 指 **`spring.datasource.*`**，再写一条 **`@SpringBootTest` HTTP 集成**；本仓库默认以 **Testcontainers 为 A 路线**。

---

## 7. 命令速查

| 场景 | 命令 |
|------|------|
| 仅单元/切片 | `cd boot-social-demo && mvn -q test` |
| 含 Testcontainers 集成 | `cd boot-social-demo && mvn -q verify`（需 Docker） |
| 本地冒烟 | `BASE=http://127.0.0.1:8081 ./smoke-boot-social.sh` |
| 覆盖默认分页 size | `export APP_API_DEFAULT_PAGE_SIZE=7` 后启动 |

---

## 8. 与 W27–W28 的衔接（计划里已写）

**Redis、Nginx、完整 Docker 部署** 按总纲放在后续周；本周 Actuator、Testcontainers、配置外置是在为「可上线」打基础，不必在 W26 强行收尾生产级安全（例如 management 独立端口、Spring Security 保护 actuator 等可留到后面专项做）。
