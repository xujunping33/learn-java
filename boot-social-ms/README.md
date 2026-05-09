# boot-social-ms（W32）

## Day 218：骨架

- gateway: `8080`
- user-service: `8082`
- post-service: `8083`

三者都提供 `GET /ping`。

## Day 219：Nacos（注册发现）

启动 Nacos（docker compose）：

```bash
cd boot-social-ms/deploy
docker compose up -d
```

Nacos 控制台：

- `http://localhost:8848/nacos/`

启动服务（本地使用 `NACOS_ADDR` 可覆盖 Nacos 地址）：

```bash
cd boot-social-ms

export NACOS_ADDR=127.0.0.1:8848

mvn -pl boot-social-user-service spring-boot:run
mvn -pl boot-social-post-service spring-boot:run
```

验收：

- Nacos 控制台能看到两个服务实例：`boot-social-user-service`、`boot-social-post-service`
- 本地接口可用：`curl http://localhost:8082/ping`、`curl http://localhost:8083/ping`

## Day 222：Sentinel（限流 + 降级）

启动 Sentinel Dashboard：

```bash
cd boot-social-ms/deploy
docker compose up -d sentinel-dashboard
```

Dashboard：

- `http://localhost:8858/`（默认账号/密码通常是 `sentinel/sentinel`，不同镜像可能略有差异）

启动 `post-service` 并连接 dashboard：

```bash
cd boot-social-ms
export SENTINEL_DASHBOARD=127.0.0.1:8858
mvn -pl boot-social-post-service spring-boot:run
```

在 dashboard 里给资源 `getPostById` 配一条 **QPS** 规则（例如 1），然后用 curl 压一下：

```bash
for i in {1..10}; do curl -sS http://localhost:8083/api/posts/1; echo; done
```

触发限流时返回：

- HTTP `429`
- `{"code":"RATE_LIMITED","message":"Too many requests"}`

