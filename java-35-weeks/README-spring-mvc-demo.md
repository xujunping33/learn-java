## spring-mvc-demo（W23 / Day155-161）

这是一个 **纯 Spring MVC（非 Boot）** 的 WAR 示例项目：REST CRUD + 统一异常 + CORS + 拦截器（日志 / API Key / 流量统计）。

### 运行与部署（Tomcat）

- **打包**（生成 `target/spring-mvc-demo.war`）

```bash
cd spring-mvc-demo
mvn -q package
```

- **部署**
  - 把 `spring-mvc-demo/target/spring-mvc-demo.war` 丢到 Tomcat 的 `webapps/` 目录
  - 启动 Tomcat
  - 默认 context path：`/spring-mvc-demo`

### 主要接口

- **Ping**：`GET /api/ping`
  - 不需要 `X-Api-Key`
- **Students CRUD**：`/api/students/**`
  - 需要 `X-Api-Key: w23-demo-key`
- **Stats**：`GET /api/stats`
  - 需要 `X-Api-Key: w23-demo-key`
  - 返回形如：
    - `total`: 总 PV（不含 OPTIONS 预检）
    - `byPath`: 按 `requestURI` 分桶的 PV

### 统一错误体（GlobalExceptionHandler）

错误响应 JSON 统一为：

```json
{"code":"...","message":"..."}
```

### CORS（Day159）

`/api/**` 允许来自：

- `http://localhost:5173`
- `http://127.0.0.1:5173`

### Smoke

Student CRUD 的 smoke：

```bash
cd spring-mvc-demo
./smoke-students.sh
```

