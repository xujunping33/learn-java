#!/usr/bin/env bash
# 将 oa-demo 打成 war 并部署到本路径 tools 下的 Tomcat（可重复执行）。
# 用法：在 java-35-weeks/ 下执行 ./deploy/deploy-oa.sh；或在 Git 仓库根执行 ./java-35-weeks/deploy/deploy-oa.sh
# 环境变量：CATALINA_HOME（默认 $REPO_ROOT/tools/apache-tomcat-10.1.54）
#           SKIP_BUILD=1  跳过 mvn，仅拷贝已有 target/oa-demo.war

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CATALINA_HOME="${CATALINA_HOME:-"$REPO_ROOT/tools/apache-tomcat-10.1.54"}"
OADEMO_DIR="$REPO_ROOT/oa-demo"
WAR="$OADEMO_DIR/target/oa-demo.war"

if [[ ! -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
  echo "错误: CATALINA_HOME 无效或缺少可执行的 bin/catalina.sh: $CATALINA_HOME" >&2
  exit 1
fi

echo "==> 停止 Tomcat（若未运行会忽略错误）"
"$CATALINA_HOME/bin/catalina.sh" stop 2>/dev/null || true
sleep 2

if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
  echo "==> 构建 war: mvn -f $OADEMO_DIR/pom.xml package -DskipTests"
  (cd "$OADEMO_DIR" && mvn -q package -DskipTests)
else
  echo "==> SKIP_BUILD=1，跳过 mvn"
fi

if [[ ! -f "$WAR" ]]; then
  echo "错误: 找不到 $WAR（请先构建或去掉 SKIP_BUILD）" >&2
  exit 1
fi

# 仅删除本应用相关目录与 war，不误删整个 webapps
echo "==> 清理旧部署: webapps/oa-demo 与 oa-demo.war"
rm -rf "$CATALINA_HOME/webapps/oa-demo" "$CATALINA_HOME/webapps/oa-demo.war"

echo "==> 拷贝 $WAR -> $CATALINA_HOME/webapps/"
cp "$WAR" "$CATALINA_HOME/webapps/oa-demo.war"

echo "==> 启动 Tomcat"
"$CATALINA_HOME/bin/catalina.sh" start

echo "完成。日志: tail -f $CATALINA_HOME/logs/catalina.out"
