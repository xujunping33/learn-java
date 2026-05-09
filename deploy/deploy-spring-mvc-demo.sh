#!/usr/bin/env bash
# 将 spring-mvc-demo 打成 war 并部署到本仓库 tools 下的 Tomcat。
# 用法：在仓库根目录执行 ./deploy/deploy-spring-mvc-demo.sh
# 环境变量：CATALINA_HOME（默认 $REPO_ROOT/tools/apache-tomcat-10.1.54）
#           SKIP_BUILD=1  跳过 mvn，仅拷贝已有 target/spring-mvc-demo.war

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CATALINA_HOME="${CATALINA_HOME:-"$REPO_ROOT/tools/apache-tomcat-10.1.54"}"
DEMO_DIR="$REPO_ROOT/spring-mvc-demo"
WAR="$DEMO_DIR/target/spring-mvc-demo.war"

if [[ ! -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
  echo "错误: CATALINA_HOME 无效或缺少可执行的 bin/catalina.sh: $CATALINA_HOME" >&2
  exit 1
fi

echo "==> 停止 Tomcat（若未运行会忽略错误）"
"$CATALINA_HOME/bin/catalina.sh" stop 2>/dev/null || true
sleep 2

if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
  echo "==> 构建 war: mvn -f $DEMO_DIR/pom.xml package -DskipTests"
  (cd "$DEMO_DIR" && mvn -q package -DskipTests)
else
  echo "==> SKIP_BUILD=1，跳过 mvn"
fi

if [[ ! -f "$WAR" ]]; then
  echo "错误: 找不到 $WAR" >&2
  exit 1
fi

echo "==> 清理旧部署: webapps/spring-mvc-demo 与 spring-mvc-demo.war"
rm -rf "$CATALINA_HOME/webapps/spring-mvc-demo" "$CATALINA_HOME/webapps/spring-mvc-demo.war"

echo "==> 拷贝 $WAR -> $CATALINA_HOME/webapps/"
cp "$WAR" "$CATALINA_HOME/webapps/spring-mvc-demo.war"

echo "==> 启动 Tomcat"
"$CATALINA_HOME/bin/catalina.sh" start

echo "完成。验证: curl -s http://localhost:8080/spring-mvc-demo/api/ping"
echo "日志: tail -f $CATALINA_HOME/logs/catalina.out"
