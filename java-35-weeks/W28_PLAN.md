# 第 28 周学习计划：项目重构升级（Redis 缓存加固 / Docker&Nginx 打磨 / CI / 协作流程）

> 对应总纲：**第27–28周（Redis7 / Nginx / Docker / Git&GitLab + 项目重构升级）** 中的 **第28周（收口与升级）**。  
> 本周定位：把 W27 跑通的链路“打磨成可长期维护、可复现、可协作”的形态，并为 W29–W30 的分布式组件接入打基础。  
> 时间：约 **2 小时/天**；本周 **Day 190 ~ Day 196**。  
> 主线工程：继续迭代 **`boot-social-demo/`**（包含你的 `compose.yaml`、Nginx 配置、缓存代码、README）。  

---

## 本周总目标（更接近上线与团队协作）

- **缓存加固**：缓存命中/失效逻辑更完整（覆盖写路径），并能解释“为什么这样失效”。可选：加入 **缓存穿透/击穿** 的最小保护。
- **容器化可复现**：`docker compose up -d` 后，**无需手工改代码**就能跑通主流程（文档 + 默认配置）。
- **Nginx 更像真实入口**：反代配置清晰（headers、健康检查入口、静态资源/路径规则可选）。
- **CI**：每次 push 自动跑 `mvn -q verify`（或 `test`），保证主干稳定。
- **重构升级**：把最“难维护”的 1–2 块代码重构掉（命名、分层、重复逻辑、异常码、DTO 一致性）。
- **协作流程**：再跑一遍 feature 分支 → PR/MR → review → 合并，并形成固定模板（PR checklist）。

---

## Day 190（周一）— 复盘 W27 交付：把“能跑”变成“可解释、可维护”

**学什么**

- 复盘驱动重构：先列出 5 个“最可能出事故/最难维护”的点，再逐个解决。

**做什么**

1. 用 15 分钟写一份 `W28-notes.md`（或直接写在周笔记里）：列出你当前工程的 5 个风险点，例如：
   - 缓存 key 设计是否会膨胀？
   - 写路径是否漏了 evict？
   - compose 环境变量是否散落？
   - Nginx 是否反代到正确的 host？
   - CI 是否缺失？
2. 定下本周“必修”重构目标：**只选 2 个**，其余延后。

**验收**

- 你能用 2 分钟讲清：本周要解决哪 2 个风险点、为什么先解决它们。

---

## Day 191（周二）— 缓存失效全覆盖 + 最小穿透/击穿保护

**学什么**

- 写操作触发失效：新增/删除评论、点赞、修改帖子，影响哪些读接口？
- 保护策略最小集（选 1 个就够）：
  - **缓存穿透**：对不存在的 id 缓存一个短 TTL 的 “null 标记”
  - **击穿**：对热点 key 用简易互斥（本周可先不做分布式锁，理解思路即可）

**做什么**

1. 做一张“影响面表”（写在 README 或 notes）：
   - 写操作 → 需要 evict 的 key 列表
2. 把 evict 补齐（你可以继续手写 Redis，也可以切 `@Cacheable/@CacheEvict`，但本周建议保持一种风格）。
3. 增加一个最小保护（推荐：**null 标记**）：
   - 查不到的 postId：缓存 `post:detail:{id}` → `__NULL__`，TTL 30s

**验收**

- 用一组 curl/smoke：证明写操作后再读会回源；不存在 id 连续访问不会每次都打 DB。

---

## Day 192（周三）— Compose 打磨：健康检查、初始化脚本、环境变量收口

**学什么**

- compose 里服务健康：MySQL ready、Redis ready、App ready。
- schema 初始化：`docker-entrypoint-initdb.d`（或你已有 migration 方案）。

**做什么**

1. `compose.yaml` 加上：
   - `healthcheck`（mysql/redis/app 至少两个）
   - 统一的 `.env`（例如 `DB_PASSWORD`、`MYSQL_DATABASE`、`APP_PORT`）
2. 数据库初始化：
   - 挂载 `init.sql`（建议把你 schema 放到 `boot-social-demo/deploy/mysql/init.sql`）
3. 文档写清楚“一键启动”与“重置环境”（删除卷）命令。

**验收**

- 在干净环境下 `docker compose up -d` 后，能稳定启动并通过 `/api/ping` 或 actuator health。

---

## Day 193（周四）— Nginx 打磨：headers、路径与健康检查入口

**学什么**

- 反代常用 header：`Host`、`X-Forwarded-For`、`X-Forwarded-Proto`。
- 健康检查：Nginx 侧的 `/health` → 后端 `/actuator/health`（或 `/api/ping`）。

**做什么**

1. 更新 `deploy/nginx/boot-social.conf`：
   - 明确 upstream（compose 内 `app:8081` 或你实际端口）
   - 设置必要 headers
2. 统一入口：
   - `/` → app
   - `/health` → app health
3. （可选）加一个最小的访问日志格式，便于排查。

**验收**

- `curl http://localhost/health` 返回健康 JSON；`curl http://localhost/api/ping` 正常。

---

## Day 194（周五）— CI：让主干永远是绿的

**学什么**

- CI 的最低价值：别人一拉代码就能知道是不是坏的。

**做什么**

1. 选择平台：
   - GitHub：`/.github/workflows/ci.yml`
   - GitLab：`.gitlab-ci.yml`
2. 最小流水线（先跑起来就赢）：
   - `mvn -q -DskipTests=false test` 或 `mvn -q verify`
3. 缓存 Maven 仓库（可选，先不追求最优）。

**验收**

- 提交一次让 CI 跑起来并通过；README 写一行“CI 徽章/状态怎么看”（可选）。

---

## Day 195（周六）— 重构升级：挑 1–2 块最痛的代码动刀

**学什么**

- 重构原则：小步提交、保持行为不变、用测试/冒烟脚本兜底。

**做什么（建议二选一或都做，但别超时）**

1. **接口返回统一**：
   - 把分页返回、错误码、成功体结构统一（减少“每个接口不一样”）
2. **服务层收敛**：
   - 把 controller 里的业务判断下沉到 service
   - 把重复的“当前登录用户获取”抽成一个组件（例如 `SessionUser`）

**验收**

- `mvn -q test` 全绿；smoke 脚本全通；代码可读性明显提升（你自己能感受到）。

---

## Day 196（周日）— 协作收口：PR 模板 + 交付说明 + W29 预备

**学什么**

- 团队协作最重要的不是工具，而是“沟通契约”：怎么提需求、怎么 review、怎么回滚。

**做什么**

1. 建立 PR 模板（写在 `docs/pr-template.md` 或仓库的 PR 模板位置）：
   - 变更摘要
   - 风险点
   - 回滚方案
   - 测试清单（本地/CI）
2. 再走一遍 feature → PR/MR → review → merge（模拟团队流程）。
3. 写 `W28-notes.md` 总结：
   - 你解决了哪 2 个风险点
   - compose 一键启动命令
   - 缓存 key/TTL/失效策略
4. W29 预备（只做准备，不开新坑）：
   - 在 README 写下“接入分布式组件前的 baseline”：CI 绿、compose 可起、健康检查 OK。

**验收**

- 别人按 README 能启动并访问；你有一份 PR 模板；主干 CI 绿。

---

## 本周交付清单（必须）

| 交付物 | 说明 |
|--------|------|
| 缓存加固 | evict 覆盖写路径 + 最小穿透/击穿保护之一 |
| compose 打磨 | `.env` + healthcheck + init.sql + 文档 |
| Nginx 入口更稳 | `/health` + headers + 反代规则清晰 |
| CI | push 自动跑 `mvn test/verify` |
| 1–2 处重构 | 小步提交、行为不变、测试/冒烟兜底 |
| PR 模板 + notes | 让协作流程可复制 |

---

## 现在开始：Day 190

今天只做 2 件事：

1. 写下 5 个风险点，并选出 **本周必修的 2 个**。  
2. 跑一次全链路验证：`mvn -q test` + `docker compose up -d` + `curl /health`（如果你已接 actuator）。  

把你选的“2 个必修风险点”发我，我会按你的实际工程结构帮你把 Day 191～193 的改动落到具体文件（缓存 key/evict、compose healthcheck、nginx upstream）。  

