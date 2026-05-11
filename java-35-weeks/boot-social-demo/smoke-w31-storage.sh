#!/usr/bin/env bash
# W31 Day215：对象存储读取（presigned GET）冒烟 — 注册→发帖→上传封面→详情拿 coverUrl→curl 直读→DB 断言。
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BASE="${BASE:-http://127.0.0.1:8081}"
PW="${PW:-p123456}"
COVER_FILE="${COVER_FILE:-./temp/download.jpeg}"

if ! command -v jq >/dev/null 2>&1; then
  echo "ERROR: 需要 jq（解析 ApiResult）" >&2
  exit 1
fi

if [ ! -f "$COVER_FILE" ]; then
  echo "ERROR: COVER_FILE 不存在：$COVER_FILE" >&2
  echo "例如：COVER_FILE=./temp/download.jpeg" >&2
  exit 1
fi

if ! curl -sS --connect-timeout 3 --max-time 5 -o /dev/null "$BASE/api/ping"; then
  echo "ERROR: 无法访问 $BASE/api/ping（请先 docker compose up -d --build）" >&2
  exit 1
fi

if ! docker compose exec -T mysql true >/dev/null 2>&1; then
  echo "ERROR: 当前目录下 Compose MySQL 不可用；DB 断言需要：cd boot-social-demo && docker compose up -d mysql app …" >&2
  exit 1
fi

mysql_sel() {
  local q="$1"
  docker compose exec -T mysql sh -lc "mysql -u\"\$MYSQL_USER\" -p\"\$MYSQL_PASSWORD\" \"\$MYSQL_DATABASE\" -N -e \"${q}\""
}

U="w31u_$RANDOM$RANDOM"

echo "BASE=$BASE"
echo "COVER_FILE=$COVER_FILE"

echo "==> register"
RA="$(curl -sS -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' -d "{\"username\":\"$U\",\"password\":\"$PW\"}")"
echo "$RA" | jq -e '.data.tokenValue' >/dev/null
A_TV="$(echo "$RA" | jq -r '.data.tokenValue')"

echo "==> create post"
RP="$(curl -sS -X POST "$BASE/api/posts" -H "Authorization: Bearer $A_TV" -H 'Content-Type: application/json' \
  -d '{"title":"w31 cover demo","content":"hello minio"}')"
POST_ID="$(echo "$RP" | jq -r '.data.id')"
echo "POST_ID=$POST_ID"

echo "==> upload cover"
RUP="$(curl -sS -X POST "$BASE/api/posts/$POST_ID/cover" -H "Authorization: Bearer $A_TV" \
  -F "file=@${COVER_FILE}")"
echo "$RUP" | jq -e '.data.objectKey' >/dev/null
echo "$RUP" | jq -e '.data.coverUrl' >/dev/null
OBJ_KEY="$(echo "$RUP" | jq -r '.data.objectKey')"
COVER_URL="$(echo "$RUP" | jq -r '.data.coverUrl')"
echo "OBJ_KEY=$OBJ_KEY"
echo "COVER_URL=$COVER_URL"

echo "==> assert DB cover_object_key"
SQL="SELECT cover_object_key FROM posts WHERE id=${POST_ID}"
DB_KEY="$(mysql_sel "$SQL")"
echo "DB cover_object_key=$DB_KEY"
if [ "$DB_KEY" != "$OBJ_KEY" ]; then
  echo "ERROR: DB cover_object_key 与返回 objectKey 不一致" >&2
  exit 1
fi

echo "==> detail should contain coverUrl"
RD="$(curl -sS "$BASE/api/posts/$POST_ID")"
echo "$RD" | jq -e '.data.coverUrl' >/dev/null
DETAIL_URL="$(echo "$RD" | jq -r '.data.coverUrl')"
if [ -z "$DETAIL_URL" ] || [ "$DETAIL_URL" = "null" ]; then
  echo "ERROR: detail.coverUrl 为空" >&2
  exit 1
fi

echo "==> curl presigned GET (must be quoted)"
BODY="$(curl -sS "$DETAIL_URL")"
if [ -z "$BODY" ]; then
  echo "ERROR: presigned GET 返回为空（可能 URL 过期或 public-endpoint 不可达）" >&2
  exit 1
fi

echo "ALL OK — W31 Day215 storage smoke passed"

