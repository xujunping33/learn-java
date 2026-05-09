# 第 17 周笔记（Java Web：Tomcat + Servlet + Session）

## Day 113：Tomcat 安装与启动

### 本机环境（记录用）

- **JDK**：OpenJDK 21（`java -version`）。
- **Tomcat**：**10.1.54**（Jakarta EE 10 / Servlet 6），解压路径：**`tools/apache-tomcat-10.1.54/`**（已加入 **`.gitignore`**，不提交二进制；换机器按 **`day113/README.md`** 重新下载即可）。

### 常用目录

| 目录 | 作用 |
|------|------|
| **`bin/`** | **`catalina.sh`**（**`start` / `stop` / `run`**）、**`startup.sh`** / **`shutdown.sh`**（包装 catalina） |
| **`webapps/`** | 部署目录：放 **`*.war`** 或已解压的应用目录；默认 **`ROOT`** 对应站点根路径 **`/`** |
| **`logs/`** | **`catalina.out`**（stdout/stderr）、**`localhost.*.log`** 等；启动失败先看这里 |

### 启动与验证

```bash
export CATALINA_HOME="$PWD/tools/apache-tomcat-10.1.54"
"$CATALINA_HOME/bin/catalina.sh" start   # 后台
curl -I http://127.0.0.1:8080/           # 应 200
"$CATALINA_HOME/bin/catalina.sh" stop
```

已在本仓库下实测：**根路径返回 HTTP 200**，**`catalina.sh stop`** 正常结束。

### 端口与上下文路径（context path）

- 默认 **HTTP 8080**（改端口见 **`conf/server.xml`** 的 **`<Connector port="8080" ...>`**）。
- **`webapps/ROOT/`** → 上下文 **`/`**；**`webapps/hello.war`** → 一般解压为 **`webapps/hello/`**，上下文 **`/hello`**（访问 **`http://host:8080/hello/...`**）。

### 口述验收：`webapps/` 里放一个 `war` 为什么会自动解压部署？

- **`Host`**（如 **`localhost`**) 的**自动部署（autoDeploy）**会周期性扫描 **`webapps/`**。
- 发现**新的或更新过的** **`.war`** 时，Tomcat 会把它**解压**成与 war **同名**的目录（若已存在会先卸装再覆盖/更新，视策略与配置而定），再按 **`WEB-INF/web.xml`** / 注解完成**应用加载**。
- 因此把 war 丢进 **`webapps/`** 等价于「交给 Host 管部署」，无需手写解压命令。

## Day 114：第一个 Servlet（映射 + 生命周期）

### 工程

- **`servlet-demo/`**：**`mvn package`** → **`target/servlet-demo.war`**；**`jakarta.servlet-api`** **`provided`**（由 Tomcat 提供实现）。

### 两种映射（本周要求「都练一遍」）

| Servlet | 映射方式 | URL（上下文 **`/servlet-demo`**） |
|---------|----------|-----------------------------------|
| **`HelloServlet`** | **`@WebServlet(urlPatterns = "/hello")`** | **`/servlet-demo/hello`** |
| **`PingServlet`** | 仅 **`WEB-INF/web.xml`** **`<servlet-mapping>`** **`/ping`** | **`/servlet-demo/ping`** |

**`web.xml`** 使用 **`metadata-complete="false"`**，注解与部署描述符可同时生效。

### 生命周期与日志

- **`HelloServlet#init(ServletConfig)`**：**`super.init(config)`** 后打日志；**每个 Servlet 实例一般只调一次**（首次使用前或随部署加载）。
- **`HelloServlet#service`**：重写后打日志再 **`super.service`**，每次请求进入都会走（再分发到 **`doGet`** 等）。
- **`destroy`**：应用卸载或重部署时调用（本练习可在 **`catalina.sh stop`** 前后看 **`logs/`**）。

### 口述验收：第一次访问与后续访问，`init` 会不会重复执行？

- **正常不重复**：同一部署、同一 **`HelloServlet`** 实例，**`init` 只执行一次**；之后只有 **`service` → `doGet`**（等）随请求反复执行。
- **会再次 `init` 的情况**：改类/改配置触发**重部署**、显式 reload、或 Tomcat 卸装后再装**新实例**时，会走新的生命周期。

## Day 115：Web 标准结构（`WEB-INF`、classes、lib）

### Maven 与目录

- **`src/main/webapp/`**：对外静态资源根（如 **`index.html`**）；**`WEB-INF/web.xml`** 部署描述符。
- **`mvn package`**：生成 **`target/servlet-demo.war`**，内含 **`WEB-INF/classes/`**（本工程 **`.class`**）与 **`WEB-INF/lib/`**（**compile** 范围依赖，如 **`commons-lang3`**）；**`provided`**（如 **`jakarta.servlet-api`**）**不打进 war**。
- **`web.xml`** 中 **`welcome-file-list`**：访问 **`/servlet-demo/`** 时默认回落到 **`index.html`**。

### 口述验收：为什么浏览器不能直接访问 `war` 里的 `WEB-INF`？

- **规范与安全**：Servlet 容器对 **`/WEB-INF/**` 与 **`/META-INF/**`** 下的资源**禁止直接 URL 访问**（即使有人在 **`webapp`** 下放同名路径也不会当静态文件裸奔），避免配置、类、依赖 jar 被下载。
- **谁能读**：**`WEB-INF`** 内的 **`web.xml`**、**`classes`**、**`lib`** 由**类加载器**在**服务端**加载；页面只能通过**映射好的 URL**（Servlet、过滤器、转发等）间接使用其中的类与逻辑，而不是当静态目录列出来。

## Day 116：GET / POST 参数与编码

### `EchoServlet`（`/echo`）

- **GET**：参数在 **query string**；浏览器对中文会 **UTF-8 百分号编码**；Tomcat 对 URI 默认按 UTF-8 解码（本练习用 **`index.html`** 里已编码的示例链接即可稳定复现）。
- **POST**：**`echo-form.html`** 提交 **`application/x-www-form-urlencoded`**；在 **`EchoServlet#doPost`** 里**必须在首次读取参数前**调用 **`request.setCharacterEncoding("UTF-8")`**，否则 body 可能按 ISO-8859-1 等错误码位解析导致**中文乱码**。
- **`getParameter` / `getParameterValues`**：同名多值（如多个 **`tag`**）用 **`getParameterValues`**；**`getParameter(name)`** 等价于取该名**第一个**值。

### 口述验收：GET 与 POST 典型场景

- **GET**：幂等、可收藏/分享 URL、适合**查询**；参数暴露在地址栏，敏感信息不宜用 GET。
- **POST**：body 传参，适合**提交/变更**（注册、下单、文件上传另论）；URL 更干净，仍需 HTTPS 防窃听。

## Day 117：状态码、Header、`sendRedirect`

### `LoginServlet`（`/login`，GET 模拟）

- **400 Bad Request**：**`user` / `pass`** 缺失或去空格后为空；**`Content-Type: text/plain`**。
- **401 Unauthorized**：口令不等于课堂演示值 **`demo`**；**`Content-Type: application/json`**（手写 JSON 字符串）。
- **200 OK**：参数合法且口令正确；**`Content-Type: text/html`**（极简成功页，用户名做 HTML escape）。

### `HomeServlet`（`/home`）

- **`response.sendRedirect(req.getContextPath() + "/hello")`**：返回 **302**，**`Location`** 指向同应用的 **`/hello`**；浏览器**再次发起 GET**，地址栏变为 **`/hello`**。

### 口述验收：重定向（redirect）与转发（forward）

- **重定向**：客户端收到 **3xx** 后**换 URL 再请求**；两次往返；可跳到**站外**；浏览器历史可区分。
- **转发**：**服务端**把请求交给另一个组件，**客户端不知道**、地址栏不变；**同一次请求**内完成（后续 W18 可结合 **`RequestDispatcher`** 深入）。

## Day 118：Session vs ServletContext

### `CounterServlet`（`/counter`）

- **`req.getSession(true)`** 取得当前浏览器会话；**`session.setAttribute("day118.sessionHitCount", n)`** 存刷新次数。
- 默认通过 **`Set-Cookie: JSESSIONID=...`** 绑定会话；**无痕窗口**没有原 Cookie → **新 Session**，计数从 **1** 再起。

### `GlobalCounterServlet`（`/global-counter`）

- 在 **`ServletContext`** 上懒加载 **`AtomicLong`**（**`synchronized (ctx)`** 双检），**`incrementAndGet`** 全站累加；换无痕/换用户仍共享同一总数。

### 口述验收：哪些数据绝不能放 `ServletContext`？

- **用户私有数据**：登录态、用户 ID、购物车、权限令牌、个人信息等——放进 **`ServletContext`** 等于**全用户共享**，严重泄密与串号。
- **`ServletContext`** 只适合**全应用级**配置/缓存（只读或线程安全）、**全局统计**等；**按用户隔离**的用 **Session** 或 **DB + 自己的会话机制**。

## Day 119：打包发布与复盘

### 流程（可重复两遍）

1. **`mvn clean package`**（清 **`target/`** 再打 war，避免旧 class 残留）。
2. Tomcat **`stop`** 后删除 **`webapps/servlet-demo.war`** 与解压目录 **`webapps/servlet-demo/`**（避免旧资源混用）。
3. 再 **`cp target/servlet-demo.war`** 到 **`webapps/`**，**`catalina.sh start`**。
4. 用 **`index.html`** 或 **`curl`** 按 **`servlet-demo/README.md`** 的 URL 表逐项验证（含 **`/health`** JSON）。
5. **`stop`** 后**重复步骤 1–4 一次**，证明部署稳定。

### 运行环境记录

- **Tomcat 10.1.54**、**端口 8080**、上下文 **`/servlet-demo`**：见 **`servlet-demo/README.md` →「运行环境（Day119）」** 与 **URL 表**。

### 周交付补全

- **`HealthServlet`**（**`/health`**）：**`application/json`**，手写 **`{"ok":true,"app":"servlet-demo"}`**（后续 W18 可换 Jackson）。
