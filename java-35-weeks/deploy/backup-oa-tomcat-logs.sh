#!/usr/bin/env bash
# 演示 tar：将 Tomcat logs 打成 .tar.gz 备份到 deploy/backups/（不删源目录）。
# 用法：在 java-35-weeks/ 下执行 ./deploy/backup-oa-tomcat-logs.sh（或从 Git 仓库根：./java-35-weeks/deploy/backup-oa-tomcat-logs.sh）
# 环境变量：CATALINA_HOME、OUT_DIR（默认 $REPO_ROOT/deploy/backups）

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CATALINA_HOME="${CATALINA_HOME:-"$REPO_ROOT/tools/apache-tomcat-10.1.54"}"
OUT_DIR="${OUT_DIR:-"$REPO_ROOT/deploy/backups"}"
LOGS="$CATALINA_HOME/logs"

if [[ ! -d "$LOGS" ]]; then
  echo "错误: 无 logs 目录: $LOGS" >&2
  exit 1
fi

mkdir -p "$OUT_DIR"
TS="$(date +%Y%m%d-%H%M%S)"
ARCHIVE="$OUT_DIR/tomcat-logs-$TS.tar.gz"

echo "==> 打包: $LOGS -> $ARCHIVE"
tar -czf "$ARCHIVE" -C "$CATALINA_HOME" logs
echo "完成: $ARCHIVE"
echo "解压示例: tar -xzf $ARCHIVE -C /tmp"
