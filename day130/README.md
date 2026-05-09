# Day130：工程骨架 + 统一 JSON + health / login

## 交付（`oa-demo/`）

- **Maven `war`**：**`pom.xml`**（Jakarta Servlet 6、Gson、**`mysql-connector-j`**）。
- **过滤器**：**`EncodingFilter`**、**`RequestLoggingFilter`**（链顺序见 **`WEB-INF/web.xml`**）。
- **统一响应**：**`ApiResult`** + **`Jsons`**；**`BaseJsonServlet`** 捕获 **`ApiException`** 与未预期异常并写出 JSON。
- **接口**：**`GET /api/health`**；**`POST /api/login`**（JDBC 查 **`users`**，**`MD5(密码+salt)`** 与 **`oa_seed.sql`** 一致）。
- **配置**：**`src/main/resources/db.properties`**（示例见 **`db.properties.example`**）。

## 验收

- **`mvn package`** 得到 **`target/oa-demo.war`**，拷到本仓库 **`tools/apache-tomcat-10.1.54/webapps/`** 后 **`catalina.sh start`**，再用 **curl** 调通 **health** 与 **login**（命令见 **`oa-demo/README.md`** →「本仓库自带 Tomcat」）。
