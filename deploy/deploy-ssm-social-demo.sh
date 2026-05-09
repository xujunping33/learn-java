#!/usr/bin/env bash
# 将 ssm-social-demo 打成 war 并部署到本仓库 tools 下的 Tomcat。
# 用法：在仓库根目录执行 ./deploy/deploy-ssm-social-demo.sh
# 环境变量：CATALINA_HOME（默认 $REPO_ROOT/tools/apache-tomcat-10.1.54）
#           SKIP_BUILD=1  跳过 mvn，仅拷贝已有 target/ssm-social-demo.war

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CATALINA_HOME="${CATALINA_HOME:-"$REPO_ROOT/tools/apache-tomcat-10.1.54"}"
DEMO_DIR="$REPO_ROOT/ssm-social-demo"
WAR="$DEMO_DIR/target/ssm-social-demo.war"

if [[ ! -x "$CATALINA_HOME/bin/catalina.sh" ]]; then
  echo "错误: CATALINA_HOME 无效或缺少可执行的 bin/catalina.sh: $CATALINA_HOME" >&2
  exit 1
fi

echo "==> 停止 Tomcat（若未运行会忽略错误）"
"$CATALINA_HOME/bin/catalina.sh" stop 2>/dev/null || true
sleep 2

# 有时 stop 失败会留下旧的 Tomcat 进程占用 8080，导致“看似启动成功但访问到旧实例”的诡异问题。
# 这里基于 -Dcatalina.base 精准清理本仓库的 Tomcat 进程（仅影响本脚本管理的 Tomcat）。
echo "==> 清理残留 Tomcat 进程（若有）"
PIDS="$(ps -ef | awk -v base=\"-Dcatalina.base=$CATALINA_HOME\" '$0 ~ base && $0 ~ /org\\.apache\\.catalina\\.startup\\.Bootstrap/ {print $2}')"
if [[ -n "${PIDS:-}" ]]; then
  echo "发现残留 Tomcat PID: $PIDS"
  kill $PIDS 2>/dev/null || true
  sleep 1
  # 仍未退出则强杀
  PIDS2="$(ps -ef | awk -v base=\"-Dcatalina.base=$CATALINA_HOME\" '$0 ~ base && $0 ~ /org\\.apache\\.catalina\\.startup\\.Bootstrap/ {print $2}')"
  if [[ -n "${PIDS2:-}" ]]; then
    echo "强制 kill -9: $PIDS2"
    kill -9 $PIDS2 2>/dev/null || true
  fi
else
  echo "无残留进程"
fi

if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
  echo "==> 构建 war: mvn -f $DEMO_DIR/pom.xml package -DskipTests"
  # 默认使用用户 ~/.m2（更稳定、可复用缓存）；如需强制使用项目内仓库，设置 USE_PROJECT_M2=1。
  if [[ "${USE_PROJECT_M2:-0}" == "1" ]]; then
    echo "==> USE_PROJECT_M2=1：使用 $DEMO_DIR/.m2/repository"
    (cd "$DEMO_DIR" && mvn -q -Dmaven.repo.local=.m2/repository package -DskipTests)
  else
    (cd "$DEMO_DIR" && mvn -q package -DskipTests)
  fi
else
  echo "==> SKIP_BUILD=1，跳过 mvn"
fi

if [[ ! -f "$WAR" ]]; then
  echo "错误: 找不到 $WAR" >&2
  exit 1
fi

echo "==> 清理旧部署: webapps/ssm-social-demo 与 ssm-social-demo.war"
rm -rf "$CATALINA_HOME/webapps/ssm-social-demo" "$CATALINA_HOME/webapps/ssm-social-demo.war"

echo "==> 拷贝 $WAR -> $CATALINA_HOME/webapps/"
cp "$WAR" "$CATALINA_HOME/webapps/ssm-social-demo.war"

echo "==> 启动 Tomcat"
"$CATALINA_HOME/bin/catalina.sh" start

echo "==> 等待应用就绪: GET /ssm-social-demo/api/ping"
PING_URL="http://localhost:8080/ssm-social-demo/api/ping"
ok=0
for i in {1..30}; do
  # -sS: silence progress, show errors; -m: max seconds; -o: discard body
  # -f: treat 4xx/5xx as failure
  # 重试期间不刷 curl 错误，避免“看起来失败但其实后面成功了”的误解。
  if curl -fsS -m 1 -o /dev/null "$PING_URL" 2>/dev/null; then
    ok=1
    break
  fi
  sleep 1
done

if [[ "$ok" != "1" ]]; then
  echo "警告: 未在 30 秒内探活成功：$PING_URL"
  echo "请查看日志: tail -n 200 \"$CATALINA_HOME/logs/catalina.out\""
else
  echo "OK: $PING_URL"
fi

echo "完成。验证: curl -s $PING_URL"
echo "日志: tail -f $CATALINA_HOME/logs/catalina.out"

