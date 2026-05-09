## 第18周学习计划：Ajax/JSON + 正则校验 + Filter（过滤器链）

对应原路径：第18周《Java Web编程进阶-常用功能与过滤器》。  
学习时长：每天约 2 小时。

本周核心目标
- 会用 **Ajax（fetch）** 调用后端接口，理解异步与错误处理
- 会用 **JSON** 作为前后端数据格式（序列化/反序列化）
- 会用 **正则** 做常见数据校验（手机号/邮箱/学号等）
- 会写 **Filter**：生命周期、链式执行、解决中文乱码、做通用能力（日志/简单鉴权）

本周交付物（必须完成）
- 在 `servlet-demo/` 增加静态页面：`webapp/static/students.html`（或 `webapp/pages/`）
- JSON API（Servlet）
  - `GET /api/students`：返回学生列表 JSON
  - `POST /api/students`：新增学生 JSON
  - `PUT /api/students/{id}`：更新（可用 `/api/students?id=` 简化）
  - `DELETE /api/students/{id}`：删除（可先用 query 简化）
- 正则校验：前端 + 后端各做一层（双保险）
- Filter
  - `EncodingFilter`：统一 UTF-8（请求/响应）
  - `RequestLoggingFilter`：打印 method/path/queryString
  - `SimpleAuthFilter`（可选）：保护 `/api/*`（例如检查 session 里是否有 login）
- `W18-notes.md`

目录建议
- `day120` ~ `day126`

每天固定节奏（2小时）
- 20min：复盘（用浏览器 Network 面板确认请求/响应头）
- 70min：编码 + 部署验证（必须可访问）
- 20min：总结入 `W18-notes.md`
- 10min：口述复盘（解释：Filter 链顺序、JSON 为什么适合前后端）

---

## Day 120（JSON基础：后端返回 application/json）

学习要点
- `Content-Type: application/json; charset=UTF-8`
- JSON 数组/对象的基本结构
- Servlet 输出 JSON（本周先用 **Gson** 或 **Jackson** 二选一；推荐 Gson 上手快）

任务卡（70min）
- 给 `servlet-demo/pom.xml` 增加 JSON 依赖（Gson 或 Jackson）
- 写 `StudentJsonServlet`（或拆成多个 servlet）
  - `GET /api/ping` 返回 `{"ok":true}`

验收标准
- Network 面板里能看到响应头是 JSON，且中文不乱码

---

## Day 121（Ajax：fetch 调用 GET）

学习要点
- `fetch(url)`、`await response.json()`
- 错误处理：`response.ok`、`try/catch`

任务卡（70min）
- 写 `static/students.html`
  - 页面加载后 `fetch('/api/students')`
  - 把结果显示到页面（表格或 `<pre>` 先都行）

验收标准
- 你能解释：为什么 Ajax 请求在浏览器里属于“异步”

---

## Day 122（POST JSON：fetch + body + headers）

学习要点
- `fetch(url, {method:'POST', headers, body})`
- `JSON.stringify`

任务卡（70min）
- 页面增加表单：name/score/age
- 点击提交：`POST /api/students` 发送 JSON
- Servlet 读取 body：`request.getReader()` + JSON 解析

验收标准
- 新增成功后列表能刷新显示新数据（前端重新 GET）

---

## Day 123（PUT/DELETE：REST-ish API + 简化路由）

学习要点
- 真实项目里路由会更复杂；本周允许用 query 简化：`/api/students?id=1`
- 幂等直觉：DELETE/PUT 的语义（先理解即可）

任务卡（70min）
- 实现删除：`DELETE /api/students?id=...`
- 实现更新：`PUT /api/students`（body 带 id）

验收标准
- Network 面板能看到不同 method；后端能正确分支处理

---

## Day 124（正则校验：前端 + 后端双保险）

学习要点
- `String.matches`、Java `Pattern/Matcher`（二选一）
- 前端 `RegExp`：`/^...$/` 全匹配

任务卡（70min）
- 校验规则（至少 3 类）
  - name：非空、长度 1–20
  - score：0–100 整数
  - phone：大陆手机号（简化规则即可）
- 前端校验失败：不发送请求
- 后端校验失败：返回 `400 + {"error":"..."} `

验收标准
- 你能解释：为什么后端校验必须有（前端可被绕过）

---

## Day 125（Filter-1：EncodingFilter 解决乱码）

学习要点
- Filter 生命周期：`init/doFilter/destroy`
- `chain.doFilter` 必须调用，否则链路中断
- 包装 request/response（先不做 deep dive）

任务卡（70min）
- 写 `EncodingFilter`：对 `text/html` 与 `application/json` 设置 UTF-8
- 验证：中文参数 + 中文 JSON 都不乱码

验收标准
- 你能解释：为什么乱码可能发生在“请求解码”或“响应编码”

---

## Day 126（Filter-2：过滤器链 + 日志/简单鉴权）

学习要点
- `web.xml` 中 `<filter-mapping>` 的顺序 = 过滤器链顺序（重点）
- 典型用途：日志、鉴权、跨域（CORS 可先了解）

任务卡（70min）
- 增加 `RequestLoggingFilter`：打印 method、uri、query
- 增加 `SimpleAuthFilter`（可选但推荐）
  - 规则：访问 `/api/*` 必须已登录（session 有 `user`）
  - 未登录返回 `401 JSON`
- 写 `LoginServlet` + 简单 `login.html`：设置 session

验收标准（完成即过关）
- 未登录访问 API 会 401；登录后可正常 CRUD
- 你能口述：Filter 链从上往下、再从下往上返回响应的过程
