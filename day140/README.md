# Day 140 — Redis 旁路缓存接进 OA

## 目标

- **Cache-aside**：读路径先查 Redis，未命中再查 MySQL，回写缓存并设 **TTL**。  
- **写后删**：提交请假、审批通过/驳回后，删除**申请人**名下所有 `GET /api/leaves/me` 相关键（不同 `?status=` / `?leaveType=` 各一把键）。

## 行为说明

| 配置 | `GET /api/leaves/me` |
|------|----------------------|
| 无 **`redis.properties`** 或 **`redis.enabled=false`** | 与 Day139 前一致，**只查库** |
| **`redis.enabled=true`** 且能 **`PING`** | 旁路缓存，TTL 默认 **45s**（**`redis.leavesMeTtlSeconds`**） |

## 启用步骤

1. 本机 **Redis** 已启动（**`redis-cli ping`** → **PONG**），见 **`redis/redis-notes.md`**。  
2. 复制 **`redis.properties.example`** → **`redis.properties`**（路径相对**仓库根** `learn/java`，不要在 **`redis/`** 子目录里用 **`oa-demo/...`**）：  
   - 在 **`oa-demo`** 目录：`cp src/main/resources/redis.properties.example src/main/resources/redis.properties`  
   - 或在仓库根：`cp oa-demo/src/main/resources/redis.properties.example oa-demo/src/main/resources/redis.properties`  
   然后编辑 **`redis.properties`**，设 **`redis.enabled=true`**。  
3. 打 **war**、部署 Tomcat，用 **`emp`** 登录后连续 **`GET /api/leaves/me`** 两次。  
4. 看 **`logs/catalina.out`**：第一次 **MISS**，第二次 **HIT**（在 TTL 内）。  
5. **`redis-cli`**：**`KEYS oa:v1:leaves:me:*`** 可看到键；提交或审批后键被删掉或 **`INVALIDATE`** 日志出现。

## 代码入口

- **`learn.java.oa.cache.LeavesMeRedisCache`**：键前缀、**`SCAN`** 批量删。  
- **`LeavesMeServlet`**：命中则直接写缓存 JSON。  
- **`LeaveSubmitServlet` / `LeaveDecisionServlet`**：事务 **`commit`** 后 **`invalidateUser`**。

## 验收

- 日志或 **Redis CLI** 能区分 **命中缓存** 与 **回源数据库**；写操作后列表与库一致（旧缓存被删）。
