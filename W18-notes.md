# 第 18 周笔记（Ajax / JSON / 正则 / Filter）

## Day 120：后端 JSON（`application/json` + Gson）

### 依赖与接口

- **`servlet-demo/pom.xml`**：增加 **`com.google.code.gson:gson`**（打进 **`WEB-INF/lib`**）。
- **`ApiPingServlet`**：**`GET /servlet-demo/api/ping`**（相对站点根即 **`/api/ping`** 于当前应用内）。
- 响应头：**`Content-Type: application/json;charset=UTF-8`**；**`resp.setCharacterEncoding("UTF-8")`** 与 **`Gson`** 写 **`Writer`**，避免中文被错误码位写出。
- Body 示例：**`{"ok":true,"msg":"就绪"}`**（**`disableHtmlEscaping()`** 便于阅读；字段名与计划 **`{"ok":true}`** 兼容并多一个中文 **`msg`** 做编码自检）。

### 与周计划命名

- 计划中的 **`StudentJsonServlet`** 拆为 **`ApiPingServlet`**（Day120）与 **`StudentsApiServlet`**（Day121+ 学生 JSON）。

### 验收（Network）

- 浏览器 **F12 → Network**：选中 **`api/ping`**，**Response Headers** 里 **`Content-Type`** 为 **`application/json`**（含 **`charset=UTF-8`**）；**Response** 里中文 **`就绪`** 不乱码。

### 口述预习

- **JSON**：键值对 / 数组，语言无关，适合前后端契约；**`charset`** 解决「字节→字符」解释一致性问题。

## Day 121：Ajax · `fetch` GET

### 后端

- **`StudentsApiServlet`**：**`GET /api/students`** → **`application/json`** 数组，内存 **`CopyOnWriteArrayList<Student>`** 种子数据。
- 模型：**`learn.java.servlet.model.Student`**（public 字段，供 **Gson** 序列化）。

### 前端

- **`webapp/static/students.html`**：**`type="module"`** 脚本在加载后 **`await fetch(apiUrl)`**；**`apiUrl = new URL('../api/students', window.location.href).href`**，适配 **`/servlet-demo`** 上下文（周计划里的 **`fetch('/api/students')`** 在根上下文 **`/`** 下才成立）。
- 校验 **`response.ok`**，**`try/catch`** 包住 **`res.json()`**；成功则表格渲染（姓名 **`escapeHtml`**）。

### 口述验收：Ajax 为何是「异步」？

- **`fetch`** 返回 **Promise**，不阻塞 JS 主线程继续执行；**`await`** 只是把「后续逻辑」挂到 Promise 完成后运行，页面仍可响应交互。整段加载由浏览器网络栈在后台完成，因此属于**异步 I/O**（与「整页刷新同步导航」对比）。

## Day 122：POST JSON（`fetch` body + `getReader`）

### 后端

- **`StudentsApiServlet#doPost`**：**`req.setCharacterEncoding("UTF-8")`**；**`req.getReader().lines().collect(joining)`** 读 body；**`Gson.fromJson`** → **`NewStudentPayload`**（**`name` / `score` / `age`**，无 **`id`**）。
- 校验失败 → **400** + **`{"error":"..."}`**；成功 → **201 Created**，响应体为新建 **`Student`** JSON；**`id`** 用 **`nextId()`**（**`synchronized`** + 扫描当前 **`max(id)`**）。

### 前端

- **`static/students.html`**：表单 **`preventDefault`**；**`fetch(apiUrl, { method: 'POST', headers: { 'Content-Type': 'application/json;charset=UTF-8' }, body: JSON.stringify(payload) })`**。
- 成功后 **`await load()`** 再次 **GET**，表格显示新行（周计划「新增成功后列表刷新」）。

## Day 123：PUT / DELETE（query 简化）

### 后端（同一 **`StudentsApiServlet`**）

- **`DELETE /api/students?id=`**：无 body；命中则 **204 No Content**；缺 **`id`** / 非数字 → **400**；不存在 → **404** + **`{"error"}`**。
- **`PUT /api/students`**：body 为完整 **`Student`** JSON（**必须含 `id`**），整行替换；成功 **200** + 更新后对象；未找到 **404**。
- 写操作（**`POST` / `PUT` / `DELETE`**）用 **`STORE_LOCK`** 与 **`CopyOnWriteArrayList`** 保证并发下一致。

### 前端

- **`static/students.html`**：表格每行 **删除** → **`fetch(apiUrl + '?id=' + id, { method: 'DELETE' })`**；**PUT** 表单 **`JSON.stringify`** 后 **`method: 'PUT'`**；成功后 **`await load()`**。

### 口述预习

- **DELETE** 多次删同一资源：第二次常 **404**（已无），与「幂等删除返回 204」的工程约定可后续再统一。
- **PUT** 常表达「替换/全量更新」语义；本周用 **`?id` 只给 DELETE** 简化路由，**PUT** 仍把 **`id` 放 body**（与计划一致）。

## Day 124：正则校验（前端 + 后端）

### 规则（至少三类）

| 字段 | 规则 |
|------|------|
| **name** | 非空；长度 **1～20**（Java **`String.matches("(?s)^.{1,20}$")`**；前端 **`/^[\s\S]{1,20}$/`**） |
| **score** | **0～100** 整数 |
| **phone** | 大陆号简化：**`^1[3-9]\d{9}$`**（**`Pattern`** / 前端 **`RegExp`**） |
| **age** | **1～120** 整数（与前几日一致，后端 **`StudentValidation.validateAge`**） |

### 实现位置

- **后端**：**`learn.java.servlet.validation.StudentValidation`**；**`POST` / `PUT`** 在写库前校验，失败 **400** + **`{"error":"..."}`**。
- **前端**：**`static/students.html`** 内 **`validateClient`**；失败则 **不 `fetch`**，状态栏提示 **「前端拦截」**。
- **模型**：**`Student` / `NewStudentPayload`** 增加 **`phone`**；种子数据带合法手机号。

### 口述验收：为何后端校验必须有？

- 前端可被 **禁用 JS、改 DevTools、用 curl/Postman、恶意客户端** 完全绕过；**只有服务端**能信。

## Day 125：`EncodingFilter`（UTF-8）

### 实现

- **`learn.java.servlet.filter.EncodingFilter`**（**`web.xml`** 注册，**`/*`**）：
  - **`init` / `destroy`**：打日志，观察生命周期。
  - **`doFilter`**：**`request.setCharacterEncoding("UTF-8")`**（解决 **POST 表单 / JSON body** 等**请求解码**）；**`HttpServletResponseWrapper`** 重写 **`setContentType`**，对 **`text/html`**、**`application/json`** 若未带 **`charset`** 则补上 **`;charset=UTF-8`**（减少**响应编码**遗漏）；**`chain.doFilter`** 放行（不调用则链路中断）。
- 与 Servlet 里已有的 **`setCharacterEncoding`** / **`setContentType(...;charset=...)`** 可并存，过滤器先做一层默认保障。

### 口述验收：乱码可能出在哪？

- **请求解码**：字节流按错误字符集解释（典型：POST 未 **UTF-8** 读 **`getParameter`** / reader）。
- **响应编码**：声明 **`Content-Type`** 未带 **`charset`** 或 **`Writer`** 与头不一致，浏览器按默认码位解字节导致错字。

## Day 126：过滤器链 + 日志 + 简单鉴权

### `web.xml` 链顺序（自上而下 = 请求进入顺序）

1. **`EncodingFilter`**  
2. **`RequestLoggingFilter`**：**`LOG.info(method + URI + ?queryString)`**  
3. **`SimpleAuthFilter`**：路径以 **`/api/`** 开头且**不是** **`/api/login`**、**`/api/ping`**（Day120 探针免登录）时，要求 **`session.getAttribute("user") != null`**，否则 **401 JSON** 并**不调用** **`chain.doFilter`**。

### Session 登录

- **`ApiLoginServlet`**：**`POST /api/login`**，JSON **`{user,pass}`**，口令 **`demo`** 时 **`req.getSession(true).setAttribute("user", user)`**；**`GET`** 返回 **405** 提示用 POST。（与 Day117 **`/login`** 查询串版区分。）
- **`static/login.html`**：**`fetch` + `JSON.stringify`** 调 **`/api/login`**。
- **`static/students.html`**：若 **GET /api/students** 为 **401**，提示先打开 **`login.html`**。

### 口述验收：Filter 链「下去再上来」

- **请求**：依次进入各 **`doFilter`**，每个在 **`chain.doFilter` 之前**可做前置逻辑；**`chain.doFilter`** 之后才轮到 **Servlet**。  
- **响应**：**Servlet 写完**后，从**最内层 Filter 往外**依次返回（各 Filter 在 **`chain.doFilter` 之后**还可写响应头/体，进阶用法后续再练）。

### 周计划过关

- **未登录**访问 **`/api/students`** → **401**；**`login.html` 登录后**同一浏览器 **Session** 带 **`user`** → **CRUD 正常**。
