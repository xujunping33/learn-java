#!/usr/bin/env bash
# Deploy ssm-social-demo to a dedicated Tomcat base on port 8081.
# This avoids conflicts when port 8080 Tomcat is stuck/multi-started.
#
# Usage: ./deploy/deploy-ssm-social-demo-8081.sh
#
# Default:
# - CATALINA_HOME: tools/apache-tomcat-10.1.54
# - CATALINA_BASE: tools/tomcat-ssm-base (server port 8105, http port 8081)

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CATALINA_HOME="${CATALINA_HOME:-"$REPO_ROOT/tools/apache-tomcat-10.1.54"}"
CATALINA_BASE="${CATALINA_BASE:-"$REPO_ROOT/tools/tomcat-ssm-base"}"
DEMO_DIR="$REPO_ROOT/ssm-social-demo"
WAR="$DEMO_DIR/target/ssm-social-demo.war"

if [[ ! -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
  echo "错误: CATALINA_HOME 无效或缺少 bin/catalina.sh: $CATALINA_HOME" >&2
  exit 1
fi
if [[ ! -d "$CATALINA_BASE/conf" ]]; then
  echo "错误: CATALINA_BASE 无效或缺少 conf/: $CATALINA_BASE" >&2
  exit 1
fi

echo "==> 停止 Tomcat (8081 base)"
CATALINA_BASE="$CATALINA_BASE" "$CATALINA_HOME/bin/catalina.sh" stop 2>/dev/null || true
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

echo "==> 清理旧部署 (8081 base)"
rm -rf "$CATALINA_BASE/webapps/ssm-social-demo" "$CATALINA_BASE/webapps/ssm-social-demo.war"

echo "==> 拷贝 WAR -> $CATALINA_BASE/webapps/"
cp "$WAR" "$CATALINA_BASE/webapps/ssm-social-demo.war"

echo "==> 启动 Tomcat (8081 base)"
CATALINA_BASE="$CATALINA_BASE" "$CATALINA_HOME/bin/catalina.sh" start

PING_URL="http://localhost:8081/ssm-social-demo/api/ping"
echo "==> 等待应用就绪: $PING_URL"
ok=0
for i in {1..30}; do
  if curl -fsS -m 1 -o /dev/null "$PING_URL"; then
    ok=1
    break
  fi
  sleep 1
done

if [[ "$ok" != "1" ]]; then
  echo "警告: 未在 30 秒内探活成功：$PING_URL"
  echo "日志: tail -n 200 \"$CATALINA_BASE/logs/catalina.out\""
else
  echo "OK: $PING_URL"
fi

echo "完成。验证: curl -s $PING_URL"
echo "日志: tail -f $CATALINA_BASE/logs/catalina.out"

