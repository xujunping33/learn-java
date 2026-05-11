# Redis 笔记（Day 139）

## 1. Ubuntu 安装与启动

```bash
sudo apt update
sudo apt install -y redis-server
sudo systemctl enable redis-server
sudo systemctl start redis-server
sudo systemctl status redis-server --no-pager
```

验证：

```bash
redis-cli ping
# 期望: PONG
```

默认监听 **`127.0.0.1:6379`**（本机开发够用）。生产需改 **`bind`**、**`requirepass`**、防火墙等，见官方文档。

---

## 2. 常用配置（备忘）

| 项 | 含义（简述） |
|----|----------------|
| **`bind`** | 监听地址；仅本机则 **`127.0.0.1`** |
| **`protected-mode yes`** | 无密码时禁止外网随意连（与 bind 配合） |
| **`requirepass`** | 客户端 **`AUTH`** 密码（有密码务必配强口令 + TLS 生产环境） |

配置文件常见路径：**`/etc/redis/redis.conf`**（包管理安装）。

---

## 3. 常用命令（String + Hash，CLI 亲自敲）

连接：**`redis-cli`**（有密码时 **`redis-cli -a 'secret'`** 或进入后 **`AUTH secret`**）。

### String

| 命令 | 作用 |
|------|------|
| **`SET key value`** | 设字符串 |
| **`GET key`** | 取字符串 |
| **`DEL key [key …]`** | 删键 |
| **`EXISTS key`** | 是否存在（1/0） |
| **`TTL key`** | 剩余过期秒数（-1 永不过期，-2 不存在） |
| **`SETEX key seconds value`** | 设值并带过期时间 |

### Hash

| 命令 | 作用 |
|------|------|
| **`HSET key field value`** | 设字段 |
| **`HGET key field`** | 取字段 |
| **`HGETALL key`** | 取全部 field/value |
| **`HEXISTS key field`** | 字段是否存在 |

### 其它（开发机慎用）

| 命令 | 说明 |
|------|------|
| **`KEYS pattern`** | 匹配键名；**生产避免**（阻塞），用 **`SCAN`** |
| **`FLUSHDB`** | 清空当前库；**仅开发环境** |

---

## 4. Redis 与本机 `HashMap` 的差别（口述验收）

- **跨进程 / 可共享**：多实例、多语言客户端连同一 Redis，内存中的 **`HashMap`** 只在单个 JVM 内可见。  
- **持久化（可选）**：Redis 可 **RDB / AOF** 落盘；普通 **`HashMap`** 进程退出即无（除非你自己序列化）。  
- **网络与容量模型**：Redis 是独立服务，可集中内存与淘汰策略；本地 Map 受单进程堆限制。

---

## 5. 本仓库 Jedis 示例

目录 **`redis/jedis-demo`**：**`JedisPooled`**、**`SET`/`GET`**、**`HSET`/`HGET`**、**Gson** 把对象打成 JSON 字符串写入 String 键。

```bash
cd redis/jedis-demo
cp src/main/resources/redis.properties.example src/main/resources/redis.properties
mvn -q exec:java
```

（需本机 **`redis-server`** 已启动且 **`redis-cli ping`** 为 **PONG**。）

---

## 6. 与 `oa-demo` 联调（Day 140）

**`oa-demo`** 在本路径根 **`java-35-weeks/`** 下，与 **`redis/`** 并列；若在 **`redis/`** 里执行复制，请用 **`../oa-demo/...`**，或先 **`cd ..`** 到 **`java-35-weeks/`**。

```bash
# 在 java-35-weeks/（Git 仓库根则先 cd java-35-weeks）
cp oa-demo/src/main/resources/redis.properties.example oa-demo/src/main/resources/redis.properties
# 编辑 oa-demo/src/main/resources/redis.properties：redis.enabled=true
```

- 部署后连打两次 **`GET /oa-demo/api/leaves/me`**（已登录 **Cookie**），在 **`logs/catalina.out`** 看 **`HIT`/`MISS`**；**`redis-cli KEYS 'oa:v1:leaves:me:*'`** 查看键（键名随 **`userId`** 与查询参数变化）。
