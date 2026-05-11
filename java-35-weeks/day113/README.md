# Day 113：Tomcat 安装与启动

## 目标

- 能启动 Tomcat，浏览器打开首页无致命错误。
- 能口述：**`webapps/`** 下放 **`.war`** 为何会**自动解压并部署**（见根目录 **`W17-notes.md` → Day 113**）。

## 推荐：官方解压版（与笔记一致）

在仓库根目录 **`learn/java/`** 下：

```bash
mkdir -p tools && cd tools
TOMCAT_VER=10.1.54
ARCHIVE="apache-tomcat-${TOMCAT_VER}.tar.gz"
curl -fsSL -O "https://dlcdn.apache.org/tomcat/tomcat-10/v${TOMCAT_VER}/bin/${ARCHIVE}"
tar xf "$ARCHIVE"
export CATALINA_HOME="$PWD/apache-tomcat-${TOMCAT_VER}"
chmod +x "$CATALINA_HOME/bin/"*.sh
"$CATALINA_HOME/bin/catalina.sh" start
```

浏览器访问：**`http://127.0.0.1:8080/`**（默认 **ROOT** 欢迎页）。

停止：

```bash
"$CATALINA_HOME/bin/catalina.sh" stop
```

## 目录速记

- **`bin/`**：启停脚本（**`catalina.sh`**）。
- **`webapps/`**：部署 **`war`** 或应用目录；**`ROOT`** 对应 **`/`**。
- **`logs/`**：**`catalina.out`**、**`localhost.*.log`**。

## 备选：包管理器

若更倾向系统级安装（Ubuntu 示例）：

```bash
sudo apt update
sudo apt install -y tomcat10
sudo systemctl status tomcat10
```

具体包名、端口与 **`webapps`** 路径以 **`dpkg -L tomcat10`** / 发行版文档为准。
