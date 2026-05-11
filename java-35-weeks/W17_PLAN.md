## 第17周学习计划：Java Web（Tomcat + Servlet + Web结构 + Session）

对应原路径：第17周《Java Web编程急速入门》。  
学习时长：每天约 2 小时。

本周核心目标
- 能在 Ubuntu 上安装并启动 Tomcat（或使用 Embedded Tomcat，二选一；本周以前者为主）
- 理解 Servlet：映射、生命周期、`doGet/doPost`
- 掌握 Java Web 标准目录结构与打包部署（`WEB-INF/web.xml`）
- 理解 GET/POST、请求参数读取、编码问题基本处置
- 理解 Session 与 ServletContext（各自保存什么、生命周期）

本周交付物（必须完成）
- `servlet-demo/`：一个可部署的 Web 应用（推荐 Maven `war`）
  - `HelloServlet`：输出 Hello + 当前时间
  - `EchoServlet`：演示 GET 查询参数与 POST form 参数
  - `CounterServlet`：Session 计数器（刷新递增）
  - `GlobalCounterServlet`：ServletContext 全局计数器
  - `HealthServlet`：返回 JSON（手写拼接即可，后续 W18 再接 Jackson）
- `W17-notes.md`

目录建议
- `day113` ~ `day119`

每天固定节奏（2小时）
- 20min：复盘（昨天 war 能否重复部署成功）
- 70min：编码 + 部署验证（必须能在浏览器访问）
- 20min：总结入 `W17-notes.md`
- 10min：口述复盘（请求进来后谁来处理、数据存在哪里）

---

## Day 113（Tomcat安装与启动：先跑起来）

学习要点
- Tomcat 目录：`bin`、`webapps`、`logs`
- 端口与应用上下文路径（context path）

任务卡（70min）
- 安装 Tomcat（任选其一）
  - 官方 Tomcat 解压版（推荐）
  - 或包管理器安装（能用即可）
- 启动后访问首页，确认日志无致命错误

验收标准
- 你能解释：`webapps/` 下面放一个 war 为什么会自动解压部署

---

## Day 114（第一个Servlet：映射与生命周期）

学习要点
- `HttpServlet`
- `@WebServlet` 或 `web.xml` 映射（本周两种都练习一遍）
- 生命周期：`init` → `service` → `destroy`

任务卡（70min）
- 创建 `servlet-demo`（Maven）
- 写 `HelloServlet`，映射 `/hello`
- 在日志或响应里打印生命周期（至少打印 init/service）

验收标准
- 你能口述：第一次访问与后续访问，init 会不会重复执行

---

## Day 115（Web标准结构：WEB-INF、classes、依赖）

学习要点
- `src/main/webapp`（Maven Web 工程）
- `WEB-INF/web.xml`
- `WEB-INF/lib`（依赖打包进 war）

任务卡（70min）
- 补齐标准目录：`webapp/WEB-INF/web.xml`
- `mvn package` 生成 `target/*.war`
- 部署到 Tomcat `webapps/`，验证访问路径

验收标准
- 你能解释：`war` 里 `WEB-INF` 为什么不能直接被浏览器访问

---

## Day 116（GET vs POST：参数读取与编码）

学习要点
- GET：query string；POST：`application/x-www-form-urlencoded`
- `request.setCharacterEncoding("UTF-8")`（至少在 POST 场景验证）
- `getParameter` 的基本规则（同名多值）

任务卡（70min）
- `EchoServlet`
  - GET：`/echo?name=...`
  - POST：提供一个简单 `form.html` 提交到 `/echo`
- 故意测试中文参数：确保不乱码（记录你怎么解决的）

验收标准
- 你能解释：GET/POST 的典型使用场景（查询 vs 提交）

---

## Day 117（请求与响应：状态码、Header、重定向）

学习要点
- `response.setStatus`、`sendRedirect`
- `Content-Type`：`text/plain`、`text/html`、`application/json`

任务卡（70min）
- 做一个 `/login`（模拟）：参数不对返回 401 或 400（任选其一，但要合理）
- 做一个 `/home`：`sendRedirect` 跳转到 `/hello`

验收标准
- 你能解释：重定向与转发（forward）差别（先建立概念，forward 可在 W18 深入）

---

## Day 118（Session vs ServletContext：状态放哪里）

学习要点
- Session：每个用户的会话状态（默认基于 Cookie：`JSESSIONID`）
- ServletContext：应用全局共享（全用户共享）

任务卡（70min）
- `CounterServlet`：Session 计数（刷新递增）
- `GlobalCounterServlet`：全局计数（多用户访问累加）
- 用浏览器无痕窗口对比 Session 行为（观察计数是否独立）

验收标准
- 你能口述：哪些数据绝对不能放 ServletContext（用户私有数据）

---

## Day 119（打包发布与复盘：可重复部署）

任务卡（70min）
- 重新走一遍完整流程
  - `mvn clean package`
  - 删除旧部署目录
  - 拷贝新 war 到 Tomcat
  - 验证四个 servlet 都可访问
- 写 `servlet-demo/README.md`
  - Tomcat 版本、端口、访问 URL 列表

验收标准（完成即过关）
- 你能独立部署成功两次（证明不是“碰运气一次成功”）
