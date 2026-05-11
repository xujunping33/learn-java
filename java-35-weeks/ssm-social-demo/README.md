## ssm-social-demo（W24 / Day162-168）

这是一个 **不使用 Spring Boot** 的 SSM（Spring + Spring MVC + MyBatis）练习项目，打包成 `war` 部署到 Tomcat。

本周主流程（Day164~Day167）：
- 注册 / 登录 / 退出（Session）
- 发帖 / 列表 / 详情（作者信息）
- 评论（新增 + 列表 + 详情聚合可选）
- 点赞（防重复）+ likeCount

### Day162 验收目标

- `mvn -q package` 能生成 `target/ssm-social-demo.war`
- 部署 Tomcat 后 `GET /api/ping` 返回 `{"ok":true}`

### DB 配置（Day163 开始需要）

项目从 classpath 读取 `src/main/resources/db.properties`。

- `db.properties` 已加入 `.gitignore`（本地文件，不建议提交）
- 你需要把 `db.user` / `db.password` 改成你本机可用的 MySQL 账号
- 建议执行一次建表脚本：`sql/ssm_social_schema.sql`

### 构建

```bash
cd ssm-social-demo
mvn -q package
```

### 部署（推荐用脚本）

仓库根目录：

```bash
./deploy/deploy-ssm-social-demo.sh
```

### 本机 Tomcat 验证（接口）

默认 context path：`/ssm-social-demo`

```bash
curl -s http://localhost:8080/ssm-social-demo/api/ping
```

### Smoke（Day163~Day167）

注意：如果你 shell 里曾经设置过通用环境变量 `BASE`（例如给其他 demo 用），本项目的 smoke 脚本改用 `SSM_BASE` 避免冲突。

```bash
cd ssm-social-demo
./smoke-users.sh
```

如需自定义地址：

```bash
SSM_BASE=http://localhost:8080/ssm-social-demo ./smoke-users.sh
```

推荐按天跑：

```bash
SSM_BASE=http://localhost:8080/ssm-social-demo bash smoke-auth.sh
SSM_BASE=http://localhost:8080/ssm-social-demo bash smoke-posts.sh
SSM_BASE=http://localhost:8080/ssm-social-demo bash smoke-comments.sh
SSM_BASE=http://localhost:8080/ssm-social-demo bash smoke-like.sh
```

### 手工 curl 主流程（最短版本）

```bash
export SSM_BASE="http://localhost:8080/ssm-social-demo"
COOKIE=/tmp/ssm_cookie.txt
rm -f "$COOKIE"

# register (会返回 Set-Cookie: JSESSIONID=...)
U="u$RANDOM"; P="p$RANDOM$RANDOM"
curl -sS -i -c "$COOKIE" -X POST "$SSM_BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\",\"password\":\"$P\"}"

# create post (201)
curl -sS -i -b "$COOKIE" -X POST "$SSM_BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d '{"title":"hello","content":"first post"}'

# list posts (200, each item has authorUsername + likeCount)
curl -sS "$SSM_BASE/api/posts?limit=5&offset=0"

# like (204), repeat like should not increase likeCount
curl -sS -i -b "$COOKIE" -X POST "$SSM_BASE/api/posts/1/like"

# add comment (201)
curl -sS -i -b "$COOKIE" -X POST "$SSM_BASE/api/posts/1/comments" \
  -H 'Content-Type: application/json' \
  -d '{"content":"nice"}'

# detail includeComments (200)
curl -sS "$SSM_BASE/api/posts/1?includeComments=true"
```

### Day168：Session 鉴权拦截器 + 详情缓存

- 写操作统一由 `AuthInterceptor` 保护：未登录直接返回 401 JSON。
- `PostService#getPostDetail` 做了一个最小的 30 秒内存缓存（帮助体验“加需求不改结构”）。

