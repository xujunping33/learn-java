# Day 139 — Redis 安装 + 常用命令 + Jedis

## 目标

- 掌握 **String** 与 **Hash** 在 CLI 上的基本读写。  
- **Jedis**：**`JedisPooled`**、**try-with-resources**、把对象 **JSON 化** 后以字符串 **`SET`/`GET`**。

## 交付物

| 路径 | 说明 |
|------|------|
| **`redis/redis-notes.md`** | Ubuntu 安装启动、配置备忘、命令表、与 **`HashMap`** 对比 |
| **`redis/jedis-demo/`** | 小 Maven 工程，**`mvn exec:java`** 跑通演示 |

## 运行

1. 按 **`redis/redis-notes.md`** 安装并 **`redis-cli ping`**。  
2. **`cd redis/jedis-demo`**，复制 **`redis.properties.example`** → **`redis.properties`**。  
3. **`mvn -q exec:java`**（若需把依赖缓存在工程内：**`-Dmaven.repo.local=.m2/repository`**）。

## 验收

- 能口述：**Redis** 与 **本机 `HashMap`** 在 **共享范围、持久化、部署形态** 上的本质差别。
