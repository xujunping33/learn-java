# 第 23 周笔记（Spring MVC：REST / 绑定 / CORS / 拦截器）

纯 **Spring MVC WAR**，与 **`spring-core-demo`** 一样先走 **非 Boot**，便于理解 **`DispatcherServlet`** 与容器如何接起来。

---

## Day 155：骨架（`DispatcherServlet` + 第一个 REST）

### 学习要点

- **`AbstractAnnotationConfigDispatcherServletInitializer`**：在 **Servlet 3+** 容器里注册 **`DispatcherServlet`**，并指定 **Servlet 级** Java 配置类（此处为 **`WebConfig`**）；**无 `web.xml`** 亦可部署。
- **`@EnableWebMvc` + `WebMvcConfigurer`**：启用 MVC 默认行为（消息转换器、HandlerMapping 等）；**`@ComponentScan`** 扫出 **`@RestController`**。
- **JSON**：类路径上有 **Jackson** 时，**`@RestController`** 返回 **`Map`/POJO** 会按 **`application/json`** 写出。

### 交付

- 模块 **`spring-mvc-demo/`**（**`packaging: war`**）。
- **`WebAppInitializer`**、**`WebConfig`**、**`web/PingController`** → **`GET /api/ping`** → **`{"ok":true}`**。
- **`spring-mvc-demo/README.md`**：打包与 **`curl`** 说明。

### 口述验收（约 60s）

1. 请求从 Tomcat 进来到返回 JSON，**经过谁**（**`DispatcherServlet` → Controller**）？  
2. **`getServletConfigClasses`** 与 **`getRootConfigClasses`**（本例为 `null`）各表示什么上下文？（进阶：父子容器；本工程先只挂 Servlet 子上下文即可。）

### `curl` 输出（示例）

```text
{"ok":true}
```

（实际 URL 前缀取决于 WAR 的 **context path**。）

---

## Day 156：映射与数据绑定（CRUD + DTO）

### 学习要点

- **`@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`**：派生自 **`@RequestMapping`**，缩小到具体 HTTP 方法。
- **`@PathVariable`**：路径段 **`{id}`** → 方法参数；**`@RequestBody`**：请求体 JSON → **`record`/POJO**（Jackson）。
- **`consumes` / `produces`**：显式 **`MediaType.APPLICATION_JSON_VALUE`**，避免歧义（多数场景可不写，默认亦能推断）。

### 交付

- **`StudentApiController`**：**`/api/students`** 内存 **`ConcurrentHashMap`** + **`AtomicLong`** 主键；初始 **`alice`(1)**、**`bob`(2)**。
- **`CreateStudentRequest`**、**`StudentResponse`**；**`model/Student`** 为仓储内部模型。
- **`smoke-students.sh`** + **`README`** **`curl`** 示例。

### 口述验收（约 60s）

1. **`POST` JSON** 如何进到 **`CreateStudentRequest`**？离不开哪一块（**消息转换器 `MappingJackson2HttpMessageConverter`**）？  
2. **`GET /api/students/{id}`** 与 **`@RequestParam`** 的 **`GET`** 有何分工？（路径资源 vs 过滤条件，Day157 会扩展查询参数。）

### HTTP 状态码（本日约定）

| 场景 | 状态码 |
|------|--------|
| POST 创建成功 | **201 CREATED** |
| PUT / DELETE 成功 | **200**（PUT body） / **204**（DELETE 无 body） |
| 资源不存在 | **404** — Day157 起改为手写 **`ApiErrorBody` JSON**，见下节 |

---

## Day 157：`ResponseEntity` + `Location` + 列表查询 + 404 体约定

### 学习要点

- **`ResponseEntity<T>`**：在同一返回类型里拼装 **HTTP 状态**、**响应头**、**body**；**`noContent()`** 表示 **204**。
- **`201 Created`**：配合 **`ServletUriComponentsBuilder`** 构造 **`Location`**（新资源 URI），客户端可做缓存或跳转。
- **可选 **`@RequestParam`****：**`required = false`** 实现 **`GET …?name=xx`**；与 **`@PathVariable`** 的资源定位分工明确。

### 交付

- **`POST`**：**`ResponseEntity.created(URI).body(...)`**，**`Location`** 形如 **`…/api/students/{id}`**（相对当前请求路径拼接）。
- **`GET /api/students/{id}`**、**`PUT`**、**`DELETE`**：不存在返回 **404** + JSON **`ApiErrorBody(code, message)`**（手写，Day158 再全局兜底）。
- **`GET /api/students?name=`**：忽略大小写的 **name 子串**过滤。

### 口述验收（约 60s）

1. **Location** 头对「REST 客户端发现新资源 URI」有什么用？  
2. **204** 与 **200 body: null** 在语义上有何差别？

### 接口状态码与 body（约定表）

| 接口 | 成功 | body | 典型失败 |
|------|------|------|----------|
| `GET /api/students` | 200 | `StudentResponse[]` | — |
| `GET /api/students?name=` | 200 | 同上（过滤） | — |
| `GET /api/students/{id}` | 200 | `StudentResponse` | 404 + `ApiErrorBody` |
| `POST /api/students` | 201 | `StudentResponse` + **Location** | — |
| `PUT …/{id}` | 200 | `StudentResponse` | 404 + `ApiErrorBody` |
| `DELETE …/{id}` | 204 | 无 | 404 + `ApiErrorBody` |

---

## Day 158：`@RestControllerAdvice` 全局异常

### 学习要点

- **`@RestControllerAdvice`**（**`@ControllerAdvice` + `@ResponseBody`** 语义）：集中 **`@ExceptionHandler`**，对各 **`@RestController`** 生效。
- **对外稳定字段**：继续使用 **`ApiErrorBody(code, message)`**；控制器里 **`throw ApiException`**，不再手写 **`ResponseEntity` 404**。
- **`HttpMessageNotReadableException`**：**JSON 反序列化失败** → **400**；**`MethodArgumentNotValidException`**：校验失败（W24 **`@Valid`**）；**兜底 `Exception`**：**500**、不暴露堆栈字符串。

### 交付

- **`web/exception/ApiException`**：**`apiCode`** + **`HttpStatus`**（**`notFound`**、**`badRequest`**）。
- **`web/exception/GlobalExceptionHandler`**：**`ApiException`**、校验、非法 JSON、**`Exception`** 四类处理。
- **`StudentApiController`**：**404 / 合法 name** 校验改为 **`throw ApiException`**。

### 口述验收（约 60s）

1. **`@RestControllerAdvice` 与切面 `LoggingAspect`** 的差别（**MVC 异常解析** vs **任意 bean 代理**）。  
2. 为何 **`INTERNAL_ERROR`** 不要把 **`ex.getMessage()`** 原文返回给前端？

### 错误码约定（扩充）

| `code` | HTTP | 场景 |
|--------|------|------|
| `NOT_FOUND` | 404 | 资源不存在 |
| `BAD_REQUEST` | 400 | 字段规则 / 非法 JSON |
| `VALIDATION_FAILED` | 400 | **`@Valid`** 字段错误摘要 |
| `INTERNAL_ERROR` | 500 | 未分类异常 |

---

## Day 159：CORS

### 学习要点

- **同源策略**：浏览器只允许页面默认请求**同源**资源；**协议+主机+端口**任一不同即跨域，XHR/fetch 需服务端返回 **`Access-Control-Allow-*`**。
- **预检 `OPTIONS`**：`Content-Type: application/json` 等「非简单请求」会先发 **`OPTIONS`**，服务端需允许对应 **Method/Header/Origin**。
- **配置方式**：本工程以 **`WebMvcConfigurer#addCorsMappings`** 为主；类/方法上 **`@CrossOrigin`** 为局部声明（二选一或组合，本课表以前者为主）。

### 交付

- **`WebConfig`**：**`addCorsMappings("/api/**")`** → **`localhost:5173` / `127.0.0.1:5173`**；**`configureDefaultServletHandling`** 放行 **`webapp`** 静态资源。
- **`src/main/webapp/cors-demo.html`**：从 **5173** 打开后 **`fetch` POST** 8080 API，验证无 CORS 报错。

### 口述验收（约 60s）

1. **预检请求**解决什么问题？没有预检时浏览器缺什么信息？  
2. **允许 `*` Origin 与写明 `http://localhost:5173`** 在生产上各有什么风险？

### 手测预检（可选）

```bash
curl -si -X OPTIONS "http://localhost:8080/spring-mvc-demo/api/students" \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type"
```

---

## Day 160：`HandlerInterceptor`（访问日志 + 简易 API Key）

### 学习要点

- **`HandlerInterceptor`**：**`preHandle`**（可做鉴权，`return false` 中断）、**`postHandle`**（较少用）、**`afterCompletion`**（无论成功与否，适合做耗时统计）。
- **`WebMvcConfigurer#addInterceptors`**：**`pathPatterns`** 与 **`order`**（数值越小越早进入 **`preHandle`** 链）。
- **与 CORS**：浏览器 **`OPTIONS`** 预检通常**不带**自定义鉴权头；在 **`AuthInterceptor`** 中对 **`OPTIONS`** 直接 **`return true`**，避免预检被 401。
- **与全局异常**：拦截器里 **`return false`** 并手写响应时，**不会**经过 **`@RestControllerAdvice`**，错误体需自行与 **`ApiErrorBody`** 对齐或复用 JSON 工具。

### 交付

- **`web/interceptor/RequestLoggingInterceptor`**：在 **`afterCompletion`** 打印 **`[INTERCEPTOR] METHOD uri -> status ms`**。
- **`web/interceptor/AuthInterceptor`**：校验 **`X-Api-Key: w23-demo-key`**（**`ApiKeyConstants`**）；仅匹配 **`/api/students/**`**；**`/api/ping`** 不在模式中故公开。
- **`WebConfig#addInterceptors`**：日志拦截 **`/api/**`**，`order(0)`；鉴权拦截 **`/api/students/**`**，`order(1)`。
- **`smoke-students.sh` / README / cors-demo.html`**：补充 **`X-Api-Key`** 与「无 Key → 401」示例。

### 口述验收（约 60s）

1. **`preHandle` false** 时，Controller 还会执行吗？**`afterCompletion`** 仍会回调吗（带不带 `Exception`）？  
2. 若把鉴权扩展到整个 **`/api/**`**，要 **`excludePathPatterns`** 哪些路径才能保证 **`ping`** 与健康检查仍匿名？
