# Day 138 — vim + tar + 部署脚本（bash）

## 目标

- **vim**：插入、保存、退出、搜索（够用即可）。
- **tar**：`.tar.gz` 打包与解压。
- **bash**：用脚本固化「停 Tomcat → 拷 war → 启 Tomcat」，减少遗漏。

## 仓库内脚本

| 脚本 | 作用 |
|------|------|
| `deploy/deploy-oa.sh` | 停 Tomcat、`mvn package`（可用 `SKIP_BUILD=1` 跳过）、只删 `webapps/oa-demo*`、拷 `oa-demo.war`、启动 |
| `deploy/backup-oa-tomcat-logs.sh` | 将 `$CATALINA_HOME/logs` 打成 `deploy/backups/tomcat-logs-时间戳.tar.gz` |

在仓库根目录：

```bash
chmod +x deploy/deploy-oa.sh deploy/backup-oa-tomcat-logs.sh
./deploy/deploy-oa.sh
# 或仅备份日志
./deploy/backup-oa-tomcat-logs.sh
```

## 笔记

- **vim / tar / 验收口径**：`deploy/linux-notes.md` 第 8～10 节。

## 验收

- 脚本可重复执行；**不会** `rm -rf` 整个 Tomcat，只清理 **`webapps/oa-demo`** 与 **`oa-demo.war`**。
