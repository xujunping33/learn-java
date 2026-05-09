# Linux 操作笔记（W20 Day137）

> 环境以 **Ubuntu / Debian 系** 为主；命令在终端逐条执行。路径请按本机修改。

---

## 1. 常见目录在干什么

| 路径 | 用途（口述版） |
|------|----------------|
| **`/var/log`** | 系统与服务日志（**`syslog`**、**`auth.log`**、各软件子目录）。应用排障常来这里「找证据」。 |
| **`/etc`** | 配置文件（**`apt`**、**`nginx`**、**`mysql`** 等）。改前建议备份；权限多为 **root**。 |
| **`/opt`** | 第三方大型软件安装目录（部分厂商习惯放这里）。本课 **Tomcat** 也可解压到 **`/opt/tomcat`**，与仓库内 **`tools/`** 二选一即可。 |

---

## 2. 列表与权限（必会）

```bash
# 长列表：权限位、属主、时间
ls -la

# 仅看目录本身
ls -ld /path/to/dir

# 给脚本执行权限（Tomcat 的 catalina.sh 等）
chmod +x ./tools/apache-tomcat-10.1.54/bin/*.sh

# 改属主（示例：把某目录交给 tomcat 用户；需 sudo）
# sudo chown -R tomcat:tomcat /opt/tomcat
```

**读权限位（最简）**：**`rwx`** 三段分别对应 **拥有者 / 组 / 其他**；目录无 **`x`** 则 **`cd`** 不进去。

---

## 3. 进程与端口（应用起不来先看这）

```bash
# 看谁占用了 8080（本课 Tomcat 默认 HTTP）
ss -tlnp | grep 8080
# 或
sudo ss -tlnp | grep ':8080'

# 查 java / tomcat 相关进程
ps aux | grep -E '[j]ava|[t]omcat'

# 若用 systemd 装的 tomcat10（包名因发行版而异）
systemctl status tomcat10 2>/dev/null || true
sudo journalctl -u tomcat10 -e --no-pager 2>/dev/null | tail -30
```

**说明**：新系统多用 **`ss`**；老文档里的 **`netstat -tlnp`** 若未安装可 **`sudo apt install net-tools`**，不必强求。

---

## 4. 本仓库自带 Tomcat：日志在哪

假设 **`CATALINA_HOME`** 为仓库下的（与 **`oa-demo/README.md`** 一致）：

```bash
export CATALINA_HOME="$PWD/tools/apache-tomcat-10.1.54"
ls -la "$CATALINA_HOME/logs"
```

重点文件：

- **`logs/catalina.out`**：控制台与异常栈常在这里（**`grep -i error`**、**`tail -f`**）。
- **`logs/localhost_access_log.*.txt`**：HTTP 访问日志（可选）。

实时盯日志：

```bash
tail -f "$CATALINA_HOME/logs/catalina.out"
```

---

## 5. Day137 练习清单（建议亲手做一遍）

1. **`ss`** 确认 **8080** 是否在监听；若被占用，记下 **PID** 与程序名。  
2. 打开 **`catalina.out`**，找到一次 **Tomcat 启动完成** 与一次 **访问 `/oa-demo/api/health`** 的痕迹。  
3. 对 **`bin/catalina.sh`** 试 **`ls -l`**，若无执行位则 **`chmod +x`** 再 **`start`**。

---

## 6. 「应用起不来」最先看的 3 件事（验收口径）

1. **端口**：监听有没有、是否被别的进程占满（**`ss -tlnp`**）。  
2. **日志**：**`catalina.out`**（或 **`journalctl`**）里 **ERROR / Exception / Caused by** 最后一屏。  
3. **权限**：脚本是否可执行、**`webapps`** 是否可写、运行用户能否读 **JDBC 配置** 与连 **MySQL**（本课 **`db.properties`** 在 war 内，更多见数据库侧 **1698 / 账号**）。

---

## 7. 可选：本机 MySQL 与 systemd（备忘）

```bash
# MySQL 是否起来（视安装方式）
systemctl status mysql 2>/dev/null || systemctl status mariadb 2>/dev/null || true

# Ubuntu root 常见 auth_socket，需 sudo 进 mysql
sudo mysql -e "SELECT 1"
```

（与 **`sql/oa_schema.sql`** 头注释中的 **1698** 说明一致。）

---

## 8. vim 最小够用（Day 138）

| 操作 | 按键 / 命令 |
|------|----------------|
| 进入插入 | **`i`**（光标前）**`a`**（光标后）**`o`**（下一行新行） |
| 退出插入 | **`Esc`** |
| 保存退出 | **`:wq`** 回车 |
| 不保存退出 | **`:q!`** 回车 |
| 搜索 | **`/关键字`** 回车，**`n`** 下一个 **`N`** 上一个 |

---

## 9. tar 打包 / 解压（Day 138）

```bash
# 将某目录打成 gzip 包（常见：备份 logs）
tar -czf backup.tar.gz -C /path/to/parent dirname_inside

# 解压到当前目录
tar -xzf backup.tar.gz

# 仅列出包内文件
tar -tzf backup.tar.gz
```

本仓库演示：**`deploy/backup-oa-tomcat-logs.sh`** 会把 **`$CATALINA_HOME/logs`** 打成 **`deploy/backups/tomcat-logs-时间戳.tar.gz`**。

---

## 10. 部署脚本化（Day 138）

- **`deploy/deploy-oa.sh`**：停 Tomcat →（可选）**`mvn package`** → 仅删除 **`webapps/oa-demo`** 与 **`oa-demo.war`** → 拷贝 **`oa-demo/target/oa-demo.war`** → **`catalina.sh start`**。  
- **`CATALINA_HOME`** 默认 **`$REPO_ROOT/tools/apache-tomcat-10.1.54`**，可环境变量覆盖。  
- 已有 war、不想每次构建：**`SKIP_BUILD=1 ./deploy/deploy-oa.sh`**。  
- 验收：**可重复执行**；不误删 Tomcat 根目录，只动本应用 war/解压目录。
