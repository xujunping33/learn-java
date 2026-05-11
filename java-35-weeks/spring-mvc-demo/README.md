# spring-mvc-demo

W23 **纯 Spring MVC**（非 Spring Boot）：`DispatcherServlet` + Java 配置 + `@RestController`。

## 构建

```bash
cd spring-mvc-demo
mvn -q package
```

产物：`target/spring-mvc-demo.war`。

## 一键部署（本仓库自带 Tomcat）

在仓库根目录执行（默认 **`tools/apache-tomcat-10.1.54`**，会先 **stop Tomcat**，再拷贝 WAR，再 **start**）：

```bash
./deploy/deploy-spring-mvc-demo.sh
```

仅拷贝已有 WAR、跳过构建：`SKIP_BUILD=1 ./deploy/deploy-spring-mvc-demo.sh`

## 部署与验证（Day 155）

1. 将 WAR 拷到 Tomcat `webapps/`（或使用你本机 Tomcat 管理界面部署）。  
2. 若 context 为 `spring-mvc-demo`，访问：

```bash
curl -s http://localhost:8080/spring-mvc-demo/api/ping
```

期望 JSON：`{"ok":true}`

（若部署为 ROOT，则路径为 `http://localhost:8080/api/ping`。）

## Day156～157：`/api/students` 内存 CRUD + `ResponseEntity`

| 方法 | 路径 | 成功 | 失败（本阶段手写体） |
|------|------|------|---------------------|
| GET | `/api/students` | **200**，JSON 数组 | — |
| GET | `/api/students?name=` | **200**，按 **name 子串**（忽略大小写）过滤 | — |
| GET | `/api/students/{id}` | **200**，JSON 对象 | **404**，**`ApiErrorBody`**：`code`,`message` |
| POST | `/api/students` | **201**，JSON 对象；响应头 **`Location: …/students/{id}`** | （body 非法等 → 后置校验见 W24） |
| PUT | `/api/students/{id}` | **200**，JSON 对象 | **404**，**`ApiErrorBody`** |
| DELETE | `/api/students/{id}` | **204**，无 body | **404**，**`ApiErrorBody`** |

```bash
export BASE=http://localhost:8080/spring-mvc-demo
K=( -H 'X-Api-Key: w23-demo-key' )
curl -s "${K[@]}" "$BASE/api/students"
curl -s "${K[@]}" "$BASE/api/students?name=ali"
curl -s "${K[@]}" "$BASE/api/students/1"
curl -si -X POST "$BASE/api/students" "${K[@]}" -H 'Content-Type: application/json' \
  -d '{"name":"carol","score":92}'
curl -s "${K[@]}" "$BASE/api/students/404"
curl -s -X PUT "$BASE/api/students/3" "${K[@]}" -H 'Content-Type: application/json' -d '{"name":"carol","score":95}'
curl -si -o /dev/null -w '%{http_code}\n' -X DELETE "$BASE/api/students/3" "${K[@]}"
```
（不写数组时也可每条命令手写 **`-H 'X-Api-Key: w23-demo-key'`**；脚本见 **`smoke-students.sh`**。）

一键脚本：`bash smoke-students.sh`。DTO：**`CreateStudentRequest`**、**`StudentResponse`**、**`ApiErrorBody`**（错误体）。

**Day160**：访问 **`/api/students/**`** 须带请求头 **`X-Api-Key: w23-demo-key`**（演示固定值，见 **`ApiKeyConstants`**）；缺失或错误 → **401**，body 与 **`ApiErrorBody`** 同形。**`/api/ping`** 仍无需密钥。日志拦截器在控制台输出 **`[INTERCEPTOR] METHOD /path -> status ms`**。

```bash
curl -sS -w '\nHTTP %{http_code}\n' "$BASE/api/students"
curl -sS -H 'X-Api-Key: w23-demo-key' "$BASE/api/students"
```

### Day158：全局异常 `GlobalExceptionHandler`

- **`@RestControllerAdvice`**：`learn.java.springmvcdemo.web.exception.GlobalExceptionHandler`
- **`ApiException`**：**`NOT_FOUND` / `BAD_REQUEST`** 等 → 对应 HTTP 状态 + **`ApiErrorBody`**
- **JSON 无法解析**：**`BAD_REQUEST`** + `invalid or missing JSON body`
- **`MethodArgumentNotValidException`**：**`VALIDATION_FAILED`**（W24 接上 **`@Valid`** 后触发）
- **其它异常**：**`500`** + `INTERNAL_ERROR`（不泄漏内部细节）

演示：

```bash
curl -s "${K[@]}" "$BASE/api/students/99999"
curl -s -X POST "$BASE/api/students" "${K[@]}" -H 'Content-Type: application/json' -d 'not-json'
curl -s -X POST "$BASE/api/students" "${K[@]}" -H 'Content-Type: application/json' -d '{"name":"","score":1}'
```
（若未定义 **`K`**，请先执行上一段中的 **`K=( -H 'X-Api-Key: w23-demo-key' )`**。）

### Day159：CORS（`WebMvcConfigurer#addCorsMappings`）

- **`/api/**`** 允许来源：**`http://localhost:5173`**、**`http://127.0.0.1:5173`**；方法：**`GET` `POST` `PUT` `DELETE` `OPTIONS`**；头：**`*`**；**`maxAge` 3600**（减少预检频率）。
- **`configureDefaultServletHandling`**：未匹配的 URL 走容器 **DefaultServlet**，可访问 WAR 里的 **`src/main/webapp/cors-demo.html`**（同源下直接打开也可，但**验收跨域**请用下面 5173）。

**跨域验收（推荐）**

1. 部署 **`spring-mvc-demo.war`**（`./deploy/deploy-spring-mvc-demo.sh`），保证 **`http://localhost:8080/spring-mvc-demo/api/ping`** 可用。  
2. 在项目里起静态目录监听 **5173**，例如：
   ```bash
   cd spring-mvc-demo
   npx --yes serve src/main/webapp -l 5173
   ```
3. 浏览器打开 **`http://localhost:5173/cors-demo.html`**，确认 **`X-Api-Key`** 与 **`ApiKeyConstants.DEMO_VALUE`** 一致（默认 **`w23-demo-key`**，Day160），再点按钮 **POST**。控制台 Network 可见先 **`OPTIONS`（若有预检）** 再 **`POST`**，且无 CORS 红字。

也可用 **`python3 -m http.server 5173 --directory spring-mvc-demo/src/main/webapp`**（在仓库根或 `spring-mvc-demo` 下注意路径）。

## 关键依赖（`pom.xml`）

| 依赖 | 作用 |
|------|------|
| `spring-webmvc` | `DispatcherServlet`、MVC 注解与配置 |
| `jackson-databind` | JSON 序列化 |
| `jakarta.servlet-api`（`provided`） | Tomcat 10+ 提供实现 |
