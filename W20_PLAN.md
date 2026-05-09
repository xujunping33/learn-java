## 第20周学习计划：OA收尾 + Linux/部署 + Redis/Jedis（按原路径第19–20周后半段）

对应原路径：第19–20周《MyBatis实现OA系统项目实战 + Linux基础》中 **第20周（偏交付、部署、缓存、联调）**。  
学习时长：每天约 2 小时。

本周核心目标
- 把 `oa-demo` 从“接口可用”推进到“**可演示的产品形态**”（最小前端联调）
- 按课表补齐：**Linux 常用命令 + 文本编辑 + 压缩 + 部署 Web 应用**
- 引入 **Redis**：安装/配置/常用命令 + **Jedis** 基本读写
- 把课表里的“动态 SQL / 数据范围控制”落到 **可解释、可演示** 的 SQL/接口上（不要求一次做到完美）

本周交付物（必须完成）
- `oa-demo/web/login.html` + `oa-demo/web/static/*`：Vue3 登录页（CDN 版即可），能调用 `POST /api/login` 并跳转到简单首页
- `oa-demo/web/app.html`：登录后最小工作台（展示当前用户、角色、入口按钮）
- `deploy/linux-notes.md`：你自己的 Linux 操作笔记（命令可复制）
- `redis/redis-notes.md`：Redis 安装配置 + 常用命令 + Jedis demo 说明
- `W20-notes.md`

目录建议
- `day134` ~ `day140`

每天固定节奏（2小时）
- 20min：复盘（昨天部署是否可重复成功）
- 70min：写页面/写脚本/写 Redis demo
- 20min：总结入 `W20-notes.md`
- 10min：口述复盘（解释：为什么要 Redis、部署时最容易踩的坑）

---

## Day 134（Vue3 登录页：对接 Session Cookie）

学习要点
- 浏览器 `fetch`：`credentials: 'include'`（携带 Cookie）
- Session 登录：服务端 `Set-Cookie: JSESSIONID`

任务卡（70min）
- 在 `oa-demo/web/` 增加登录页（Vue3 CDN）
- 调 `POST /oa-demo/api/login`
- 登录成功后跳转 `app.html`（或同页切换视图）

验收标准
- Network 能看到 `JSESSIONID`，后续请求带 Cookie 能访问受保护接口

---

## Day 135（最小工作台：列表页雏形 + 权限入口）

学习要点
- 前端路由可以先用“多页面”代替（`login.html` / `app.html` / `leaves.html`）
- 按钮级权限：根据角色显示/隐藏入口（先做 UI 级）

任务卡（70min）
- `leaves.html`：员工提交请假表单 + 本人列表（调用已有 API）
- `pending.html`：经理待审列表（若后端接口未齐，先列 TODO 并在本周补齐）

验收标准
- 三个角色至少各有一条“可点路径”能走通主流程

---

## Day 136（动态 SQL 与数据范围：把“课表点”落成可讲清的实现）

学习要点
- 动态 SQL 的典型价值：条件组合、列表筛选、权限拼接
- 数据范围：员工只看本人；经理看本部门待审（先实现最小正确版本）

任务卡（70min）
- 输出 `sql/oa_dynamic_queries.sql`：至少 5 条“条件可变”的查询模板
- 后端补齐（若尚未实现）：`GET /api/leaves/pending`（经理）
- 在 `W20-notes.md` 写清楚：SQL 如何体现数据范围（where 条件）

验收标准
- 你能用 90 秒讲清楚：为什么经理查询必须带 dept 条件

---

## Day 137（Linux-1：目录结构 + 权限 + 进程与端口）

学习要点
- `/var/log`、`/etc`、`/opt` 常见用途
- `ls/chmod/chown`、`ps/ss/netstat(或ss)`、`journalctl`（按你系统可用工具）

任务卡（70min）
- 写 `deploy/linux-notes.md`
- 练习：定位 Tomcat 端口占用、查看 catalina 日志路径

验收标准
- 你能独立找到“应用起不来”时最先看的 3 个信息：端口、日志、权限

---

## Day 138（Linux-2：vim + tar + 简单 bash 脚本）

学习要点
- vim 基础：插入/保存/退出/搜索（够用即可）
- `tar`：打包/解压（`.tar.gz`）
- bash：把部署步骤脚本化（减少人为遗漏）

任务卡（70min）
- 写一个 `deploy/deploy-oa.sh`（可很简单）
  - 停止 tomcat（或提示手动）
  - 拷贝 war
  - 启动 tomcat
- 用 `tar` 打包 `oa-demo` 的 `logs/` 或 `webapps/` 备份（演示即可）

验收标准
- 脚本能重复执行（至少不会误删关键目录）

---

## Day 139（Redis：安装配置 + 常用命令 + Jedis 读写）

学习要点
- Redis 数据结构先掌握 **String + Hash** 即可
- Jedis：连接池、try-with-resources、序列化 JSON 字符串

任务卡（70min）
- 安装并启动 Redis（Ubuntu）
- 写 `redis/jedis-demo`（小 Java 工程或挂在 `oa-demo` 里二选一）
  - set/get
  - hset/hget
- 写 `redis/redis-notes.md`：常用命令清单（你亲自敲过）

验收标准
- 你能解释：Redis 与本机 HashMap 的本质差别（持久化/跨进程/可共享）

---

## Day 140（Redis 应用场景：把“热点读”接进 OA）

学习要点
- 缓存的典型模式：cache-aside（旁路缓存）
- 缓存一致性：更新后删除缓存（先掌握朴素做法）

任务卡（70min）
- 选一个读多写少的接口做缓存（任选其一）
  - `GET /api/leaves/me` 的结果缓存 30–60 秒
  - 或 `GET /api/health` 不适合缓存，改缓存部门列表（更合理）
- 审批/提交后删除相关缓存 key

验收标准
- 用日志或 Redis CLI 能证明：命中缓存 vs 回源数据库
