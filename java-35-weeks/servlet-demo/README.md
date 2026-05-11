# servlet-demo（W17）

Maven **`war`**，Tomcat **10.x**（Jakarta Servlet **6**）。

## 运行环境（Day119）

| 项 | 说明 |
|----|------|
| **JDK** | **21**（`maven.compiler.release=21`） |
| **Tomcat** | **10.1.54**（Jakarta EE 10）；解压路径示例：**`java-35-weeks/tools/apache-tomcat-10.1.54/`**（相对 Git 仓库根；在本路径根下则为 **`tools/apache-tomcat-10.1.54/`**，见 **`day113/README.md`**） |
| **HTTP 端口** | **8080**（默认 **`conf/server.xml`** 的 **`<Connector port="8080">`**） |
| **上下文路径** | **`/servlet-demo`**（与 **`servlet-demo.war`** 文件名一致） |
| **应用根 URL** | **`http://127.0.0.1:8080/servlet-demo/`** |

## 构建

```bash
cd servlet-demo
mvn clean package
```

产物：**`target/servlet-demo.war`**（`finalName` 已固定为 **`servlet-demo`**）。

## 可重复部署（Day119，建议做两遍）

Tomcat **先 `stop`**。在本路径根目录 **`java-35-weeks/`**（与 **`tools/`**、**`servlet-demo/`** 同级；若在 Git 仓库根则先 **`cd java-35-weeks`**）：

```bash
export CATALINA_HOME="$PWD/tools/apache-tomcat-10.1.54"
rm -rf "$CATALINA_HOME/webapps/servlet-demo" "$CATALINA_HOME/webapps/servlet-demo.war"
cp servlet-demo/target/servlet-demo.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" start
```

等待解压完成（约数秒）后，用下方 **URL 清单** 或 **`curl`** 自检，再 **`"$CATALINA_HOME/bin/catalina.sh" stop"`**。  
**第二遍**：再执行一次 **`mvn clean package`** → **删 `webapps/servlet-demo*`** → **再拷 war** → **再 start** → **再验**（与第一遍相同），满足「独立部署成功两次」。

### 一键自检（可选）

```bash
BASE=http://127.0.0.1:8080/servlet-demo
curl -fsS "$BASE/hello" | head -1
curl -fsS "$BASE/ping" | head -1
curl -fsS "$BASE/echo?name=x" | head -1
curl -fsS "$BASE/health"
curl -fsS "$BASE/api/ping"
curl -fsS "$BASE/api/students" | head -c 200
echo
curl -fsS "$BASE/counter" | head -2
curl -fsS "$BASE/global-counter" | head -2
```

## war 里有什么（Day115）

```bash
jar tf target/servlet-demo.war | grep -E '^WEB-INF/(classes/|lib/)' | head -40
```

应能看到：**`WEB-INF/classes/...`**（本工程编译的 **`.class`**）与 **`WEB-INF/lib/`** 下的 **`commons-lang3-*.jar`**、**`gson-*.jar`**（**非 `provided`** 依赖）。**`jakarta.servlet-api`** 为 **`provided`**，**不会**打进 war，运行时由 Tomcat 提供。

静态页：**`src/main/webapp/index.html`** → 部署后 **`/servlet-demo/`**（或 **`/servlet-demo/index.html`**）。

## 部署到本机 Tomcat（与 Day113 解压版一致）

**推荐**升级部署前先 **`stop`** 并删除旧 **`webapps/servlet-demo*`**（见上文 **「可重复部署（Day119）」**），避免混用旧 class。

在本路径根目录 **`java-35-weeks/`**（与 **`tools/`** 同级）执行：

```bash
export CATALINA_HOME="$PWD/tools/apache-tomcat-10.1.54"
cp servlet-demo/target/servlet-demo.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" start
```

若当前目录已是 **`servlet-demo/`**，可用：

```bash
export CATALINA_HOME="$(dirname "$PWD")/tools/apache-tomcat-10.1.54"
cp target/servlet-demo.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" start
```

上下文路径为 **`/servlet-demo`**（与 war 文件名一致）。

## 访问 URL（部署后）

| 说明 | 路径 |
|------|------|
| 注解映射 **`HelloServlet`** | `http://127.0.0.1:8080/servlet-demo/hello` |
| **`web.xml`** 映射 **`PingServlet`** | `http://127.0.0.1:8080/servlet-demo/ping` |
| **`EchoServlet`**（GET / POST） | `http://127.0.0.1:8080/servlet-demo/echo?...`；POST 见静态页 **`/servlet-demo/echo-form.html`** |
| **`HomeServlet`**（302 → **`/hello`**） | `http://127.0.0.1:8080/servlet-demo/home` |
| **`LoginServlet`**（400 / 401 / 200） | `http://127.0.0.1:8080/servlet-demo/login?...`（见首页示例链接） |
| **`CounterServlet`**（Session 刷新计数） | `http://127.0.0.1:8080/servlet-demo/counter` |
| **`GlobalCounterServlet`**（ServletContext 全站累加） | `http://127.0.0.1:8080/servlet-demo/global-counter` |
| **`HealthServlet`**（JSON） | `http://127.0.0.1:8080/servlet-demo/health` |
| **`ApiPingServlet`**（Day120，Gson） | `http://127.0.0.1:8080/servlet-demo/api/ping` |
| **`StudentsApiServlet`**（Day121，列表 JSON） | `http://127.0.0.1:8080/servlet-demo/api/students` |
| **`static/students.html`**（Day121，Ajax） | `http://127.0.0.1:8080/servlet-demo/static/students.html` |
| **`static/login.html`**（Day126，Session） | `http://127.0.0.1:8080/servlet-demo/static/login.html` |
| **`POST /api/login`**（JSON） | `http://127.0.0.1:8080/servlet-demo/api/login` |
| 首页（含 GET 中文示例链接） | `http://127.0.0.1:8080/servlet-demo/` |

**Day116**：GET 用 query string；POST 为 **`application/x-www-form-urlencoded`**，**`EchoServlet#doPost`** 在读取参数前 **`request.setCharacterEncoding("UTF-8")`**；同名多值用 **`getParameterValues`**，**`getParameter`** 只取第一个。

**Day117**：**`setStatus`** / 默认状态与 **`Content-Type`**（**`text/plain`**、**`application/json`**、**`text/html`**）；**`sendRedirect`** 与 **`getContextPath()`** 拼出同应用内路径。

**Day118**：**`/counter`** 用 **Session** 存「本会话点击次数」；**`/global-counter`** 用 **`ServletContext`** 存 **`AtomicLong`** 全站共享命中数。

**Day120**：**`GET /api/ping`**，**Gson** 输出 JSON，**`Content-Type: application/json;charset=UTF-8`**，响应体含中文 **`msg`** 便于 Network 验编码。

**Day121**：**`GET /api/students`** 返回学生数组；**`static/students.html`** 用 **`fetch` + `await`** 拉取并表格展示（**`response.ok` / try·catch`**）。

**Day122**：**`POST /api/students`**，body 为 JSON；**`getReader`** + **Gson** 解析；**201** 返回新建学生；前端 **`JSON.stringify`** 提交后 **`await load()`** 刷新。

**Day123**：**`DELETE /api/students?id=`** → **204**；**`PUT /api/students`**（body 含 **`id`**）→ **200**；见 **`static/students.html`** 行内删除与 PUT 表单。

**Day124**：学生含 **`phone`**；**`StudentValidation`**（**`Pattern` + `matches`**）与页面 **`RegExp`** 双保险；非法 **400 JSON** 或前端不发请求。

**Day125**：**`EncodingFilter`**（**`web.xml`** **`/*`**）— 请求 **`UTF-8`**；**`text/html`** / **`application/json`** 无 **`charset`** 时自动补 **`;charset=UTF-8`**。

**Day126**：**`RequestLoggingFilter`** + **`SimpleAuthFilter`**（**`/api/*`** 需 Session **`user`**，豁免 **`/api/login`**、**`/api/ping`**）；**`POST /api/login`** + **`static/login.html`**。

**`HelloServlet`** 在 **`logs/`**（如 **`catalina.out`** / **`localhost.*.log`**）打印 **`init` / `service` / `destroy`**，便于对照生命周期。

停止：**`"$CATALINA_HOME/bin/catalina.sh" stop`**。
