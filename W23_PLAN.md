# 第 23 周学习计划：Spring MVC（REST / 绑定 / CORS / 拦截器）

> 对应原课表：**SpringMVC 开发流程与环境配置、数据绑定、RESTful 接口开发、SpringMVC 跨域、拦截器、拦截器实现用户流量统计**。  
> 本周：**纯 Spring MVC（`spring-webmvc` + WAR + Tomcat）**，与 W21–W22 的 **非 Boot Spring** 衔接；Spring Boot 在后续周再切。  
> 时间：约 **2 小时/天**；本周 **Day 155 ~ Day 161**。

---

## 本周总目标

- 能独立搭一个 **Spring MVC WAR 项目**：`DispatcherServlet`、Java 配置、`@RestController`。
- 掌握 **请求映射**、**路径变量 / 查询参数 / 表单 / JSON 请求体** 绑定。
- 能写 **REST 风格 API**：`ResponseEntity`、合适 HTTP 状态码、**统一 JSON 响应**。
- 会用 **`@ControllerAdvice`** 做全局异常与统一错误体。
- 会配置 **CORS**（`WebMvcConfigurer`）。
- 会写 **HandlerInterceptor**：鉴权/日志；**流量统计**（内存计数或按 URI 聚合）。

**本周工程约定**

- 新建 Maven 模块：`spring-mvc-demo`（`packaging: war`），JDK 17，Spring Framework 6.x（与 `spring-core-demo` 对齐）。
- 根路径建议：`/api` 下挂 REST；静态页可选 `src/main/webapp`。
- Git：每完成一天任务至少 **1 次 commit**。

---

## Day 155（周一）— Spring MVC 骨架：DispatcherServlet + 第一个 REST

**学什么**

- Web 应用上下文：`AnnotationConfigWebApplicationContext` 与 Servlet 容器的关系。
- `DispatcherServlet`、**Java 配置**替代 `web.xml`（`AbstractAnnotationConfigDispatcherServletInitializer`）。
- `@Configuration` + `@EnableWebMvc` + 组件扫描；`@RestController` + `@GetMapping`。

**做什么**

1. 创建 `spring-mvc-demo`：`war`、`spring-webmvc`、`jakarta.servlet-api`（Spring 6 用 **Jakarta EE 9+**，`provided`）。
2. 实现 `WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer`：`getRootConfigClasses` / `getServletConfigClasses` / `getServletMappings`（如 `"/"`）。
3. `WebConfig implements WebMvcConfigurer`：`@EnableWebMvc`，`componentScan` 指向 `...web` 包。
4. `PingController`：`GET /api/ping` 返回 JSON `{"ok":true}`（需 **Jackson**：`jackson-databind`）。
5. `mvn package` 生成 WAR，部署到本机 Tomcat，`curl` 验证。

**验收**

- 浏览器或 `curl` 访问 `http://localhost:8080/<context>/api/ping` 返回 JSON。

---

## Day 156（周二）— 映射与数据绑定

**学什么**

- `@RequestMapping` 派生注解：`GetMapping`、`PostMapping` 等。
- `@PathVariable`、`@RequestParam`（`required`、`defaultValue`）。
- `@RequestBody` + DTO（JSON 反序列化）；`consumes` / `produces`（`APPLICATION_JSON`）。

**做什么**

1. `StudentApiController`（可先内存 `Map<Long, Student>` 假数据）：
   - `GET /api/students` 列表；
   - `GET /api/students/{id}`；
   - `POST /api/students` body 为 JSON；
   - `PUT /api/students/{id}`；
   - `DELETE /api/students/{id}`。
2. DTO：`CreateStudentRequest`、`StudentResponse`（字段校验可先省略，W24 可接 Validation）。
3. Postman/`curl` 测 CRUD。

**验收**

- CRUD 全流程 `curl` 脚本或小表记录请求/响应。

---

## Day 157（周三）— REST 规范与 ResponseEntity

**学什么**

- 合适状态码：`201 Created` + `Location`、`204 No Content`、`404` 等。
- `ResponseEntity<T>` 封装 body + headers + status。

**做什么**

1. 创建成功返回 `201`，响应头带 `Location: /api/students/{id}`（可用 `ServletUriComponentsBuilder`）。
2. 删除成功返回 `204`；不存在返回 `404` 统一 JSON（可先手写，Day 158 再全局化）。
3. 列表支持可选查询：`GET /api/students?name=xx`（`@RequestParam` 可选）。

**验收**

- 文档中列出各接口的 **HTTP 状态码** 与 body 约定。

---

## Day 158（周四）— 全局异常：@ControllerAdvice

**学什么**

- `@RestControllerAdvice` / `@ControllerAdvice` + `@ExceptionHandler`。
- 业务异常与系统异常分层；对外只暴露稳定错误码。

**做什么**

1. 定义 `ApiException`（含 `code`、`message`）或复用 OA 里的 `ApiResult` 风格。
2. `GlobalExceptionHandler`：处理 `ApiException`、`MethodArgumentNotValidException`（若已用校验）、兜底 `Exception`。
3. 控制器里删除重复的 try/catch，404 改为抛 `ApiException` 或 `ResponseStatusException`。

**验收**

- 故意触发 404、400，响应体格式一致（字段名固定）。

---

## Day 159（周五）— CORS

**学什么**

- 浏览器预检 `OPTIONS`；`Access-Control-*` 头。
- `WebMvcConfigurer#addCorsMappings` 或 `@CrossOrigin`（二选一为主，另一种了解）。

**做什么**

1. 在 `WebConfig` 中 `addCorsMappings`：允许 `http://localhost:5173`（或你本机静态页端口）、方法 `GET/POST/PUT/DELETE/OPTIONS`、常用头。
2. 用 **简单 HTML + fetch**（放在 `webapp` 或任意静态服务器）跨域调 `POST /api/students`，确认无 CORS 报错。

**验收**

- 截图或文字说明预检与正式请求各一次成功。

---

## Day 160（周六）— 拦截器：日志与简易鉴权

**学什么**

- `HandlerInterceptor`：`preHandle`、`postHandle`、`afterCompletion`。
- 注册：`WebMvcConfigurer#addInterceptors`；`excludePathPatterns`（如 `/api/ping`、`/static/**`）。

**做什么**

1. `RequestLoggingInterceptor`：记录 method、URI、耗时（`afterCompletion` 里算）。
2. `AuthInterceptor`：读 header `X-Api-Key`（或固定 token），不匹配则 `response.setStatus(401)` 并 `return false`（先不搞 Session，与 REST 一致）。
3. `/api/students/**` 需鉴权，`/api/ping` 放行。

**验收**

- 无 token 访问 students 返回 401；带 token 正常。

---

## Day 161（周日）— 拦截器：流量统计

**学什么**

- 在拦截器或单例组件中维护 **原子计数**（`ConcurrentHashMap` + `LongAdder` 或 `AtomicLong`）。
- 区分 **总 PV** 与 **按 URI** 统计（可选：按 method+uri）。

**做什么**

1. `TrafficStatsInterceptor` + `TrafficStatsService`（`@Component`）：每次 `preHandle` 累加总 PV；按 `request.getRequestURI()` 分桶。
2. `GET /api/stats`（可放行或仅管理员 token）：返回 JSON，如 `total`、`byPath`。
3. 本周小结：整理 `README-spring-mvc-demo.md`（如何打包部署、主要 URL、CORS 与鉴权说明）。

**验收**

- 压测几次不同 URI 后 `/api/stats` 数字合理；README 可让别人复现。

---

## 本周交付清单

| 交付物 | 说明 |
|--------|------|
| `spring-mvc-demo/` | WAR 工程，可部署 Tomcat |
| REST CRUD + 统一异常 + CORS + 双拦截器 + stats | 代码在仓库内 |
| `README-spring-mvc-demo.md` | 构建、部署、测试命令 |

---

## 与后续周衔接

- **W24**：可在本 demo 上接 **Spring Validation**、文件上传、视图解析（若做半前后端分离可略讲 Thymeleaf）。
- **W25+**：Spring Boot 3 会 **内置 DispatcherServlet 与自动配置**，本周手写配置有助于理解 Boot 在替你做哪几件事。

---

## 现在开始：Day 155

1. 在仓库根目录旁新建 `spring-mvc-demo`，`packaging` 为 `war`。  
2. 加入：`spring-webmvc`、`jackson-databind`、`jakarta.servlet-api`（`provided`）。  
3. 按上面实现 `WebAppInitializer`、`WebConfig`、`PingController`。  
4. `mvn -q package` 后部署 WAR，`curl` 通 `/api/ping`。

做完把 **`pom.xml` 里关键依赖**、`WebAppInitializer` 类名、以及 **`curl` 输出** 发我，我们再收紧 **Day 156** 的 Student CRUD 包结构与 DTO 命名。
