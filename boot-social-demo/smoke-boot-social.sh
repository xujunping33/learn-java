#!/usr/bin/env bash
# W25：注册 → 发帖 → 列表/详情 → 评论 → 点赞 → 聚合详情
# W28 Day191：幽灵 id ×2（负缓存 __NULL__）+ 可选 redis-cli 校验
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

BASE="${BASE:-http://127.0.0.1:8081}"
COOKIE="${COOKIE_JAR:-/tmp/boot_social_smoke_cookie.txt}"
# 与应用的 Redis 对齐：Compose 未映射 6379 到宿主机时，必须用「compose exec redis」而非本机 redis-cli
REDIS_CLI_HOST="${REDIS_CLI_HOST:-127.0.0.1}"
REDIS_CLI_PORT="${REDIS_CLI_PORT:-6379}"

compose_redis_running() {
    [ -f "$SCRIPT_DIR/compose.yaml" ] || [ -f "$SCRIPT_DIR/docker-compose.yml" ] || return 1
    (cd "$SCRIPT_DIR" && docker compose ps -q redis 2>/dev/null | grep -q .)
}

pick_redis_verify_backend() {
    if compose_redis_running; then
        echo "compose"
        return 0
    fi
    if command -v redis-cli >/dev/null 2>&1; then
        echo "host"
        return 0
    fi
    echo "skip"
    return 0
}

U="smoke_$RANDOM"
P="p$RANDOM$RANDOM"
TITLE="title_$RANDOM"
CONTENT="post body $RANDOM"
COMMENT="nice $RANDOM"

echo "BASE=$BASE"
rm -f "$COOKIE"

if ! curl -sS --connect-timeout 3 --max-time 5 -o /dev/null "$BASE/api/ping"; then
    echo "ERROR: 无法连上 $BASE（应用未监听？可先: mvn spring-boot:run ，或 docker compose up -d 后再执行本脚本）" >&2
    exit 1
fi

BACKEND="$(pick_redis_verify_backend)"
case "$BACKEND" in
compose) REDIS_HINT="docker compose exec redis（与应用同一 Redis）" ;;
host)    REDIS_HINT="${REDIS_CLI_HOST}:${REDIS_CLI_PORT}（本机 redis-cli；若连错实例请改用 compose 或设 REDIS_CLI_*）" ;;
*)       REDIS_HINT="跳过 Redis 断言" ;;
esac

echo "==> absent post detail (ghost id → Redis __NULL__ 负缓存; $REDIS_HINT)"
FAKE_ID=999999997
KEY="post:detail:$FAKE_ID"
HTTP_A="$(curl -sS "$BASE/api/posts/$FAKE_ID" -o /tmp/smoke_absent_a.json -w "%{http_code}")"
if [ "$HTTP_A" != "404" ]; then
  echo "ERROR: 第一次 GET /api/posts/$FAKE_ID 期望 HTTP 404，实际 $HTTP_A（body 见 /tmp/smoke_absent_a.json）" >&2
  exit 1
fi
python3 -c "import json; d=json.load(open('/tmp/smoke_absent_a.json')); assert d.get('code')=='NOT_FOUND', d"

HTTP_B="$(curl -sS "$BASE/api/posts/$FAKE_ID" -o /tmp/smoke_absent_b.json -w "%{http_code}")"
if [ "$HTTP_B" != "404" ]; then
  echo "ERROR: 第二次 GET /api/posts/$FAKE_ID 期望 HTTP 404，实际 $HTTP_B" >&2
  exit 1
fi
python3 -c "import json; d=json.load(open('/tmp/smoke_absent_b.json')); assert d.get('code')=='NOT_FOUND', d"

if [ "$BACKEND" = "compose" ]; then
  GOT="$(cd "$SCRIPT_DIR" && docker compose exec -T redis redis-cli GET "$KEY")"
  TTL_OUT="$(cd "$SCRIPT_DIR" && docker compose exec -T redis redis-cli TTL "$KEY")"
  if [ "$GOT" != "__NULL__" ]; then
    echo "ERROR: Compose Redis GET $KEY 期望 __NULL__，实际 '${GOT:-<empty>}'" >&2
    exit 1
  fi
  python3 -c "ttl=int('$TTL_OUT'); assert ttl > 0, ttl"
  echo "redis (compose): GET $KEY => __NULL__, TTL=$TTL_OUT"
elif [ "$BACKEND" = "host" ]; then
  GOT="$(redis-cli -h "$REDIS_CLI_HOST" -p "$REDIS_CLI_PORT" GET "$KEY")"
  TTL_OUT="$(redis-cli -h "$REDIS_CLI_HOST" -p "$REDIS_CLI_PORT" TTL "$KEY")"
  if [ "$GOT" != "__NULL__" ]; then
    echo "ERROR: redis-cli GET $KEY 期望 __NULL__，实际 '${GOT:-<empty>}'（应用在 Compose 内时请在本脚本目录执行且不要用宿主 redis-cli 连错库）" >&2
    exit 1
  fi
  python3 -c "ttl=int('$TTL_OUT'); assert ttl > 0, ttl"
  echo "redis (host): GET $KEY => __NULL__, TTL=$TTL_OUT"
else
  echo "SKIP: 未检测到 compose redis，且无 redis-cli — 仅校验两次 HTTP 404"
fi

echo "==> register"
curl -sS -c "$COOKIE" -X POST "$BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\",\"password\":\"$P\"}" \
  -o /tmp/smoke_register.json
python3 -c "import json; d=json.load(open('/tmp/smoke_register.json')); assert d.get('ok')==True, d; print('user id', d['data']['id'])"

echo "==> create post"
curl -sS -b "$COOKIE" -X POST "$BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\",\"content\":\"$CONTENT\"}" \
  -o /tmp/smoke_post.json
POST_ID="$(python3 -c "import json; print(json.load(open('/tmp/smoke_post.json'))['data']['id'])")"
echo "POST_ID=$POST_ID"

echo "==> list posts"
curl -sS "$BASE/api/posts?page=1&size=5" -o /tmp/smoke_list.json
python3 -c "import json; d=json.load(open('/tmp/smoke_list.json')); assert d.get('ok')==True; items=d['data']['items']; assert any(x.get('id')==$POST_ID for x in items), d"

echo "==> post detail (before comment)"
curl -sS "$BASE/api/posts/$POST_ID" -o /tmp/smoke_detail1.json

echo "==> add comment"
curl -sS -b "$COOKIE" -X POST "$BASE/api/posts/$POST_ID/comments" \
  -H 'Content-Type: application/json' \
  -d "{\"content\":\"$COMMENT\"}" \
  -o /tmp/smoke_comment.json

echo "==> like"
curl -sS -o /dev/null -w "like HTTP %{http_code}\n" -b "$COOKIE" -X POST "$BASE/api/posts/$POST_ID/like"

echo "==> post detail (after comment + like)"
curl -sS "$BASE/api/posts/$POST_ID" -o /tmp/smoke_detail2.json
python3 - <<PY
import json
with open("/tmp/smoke_detail2.json") as f:
    d = json.load(f)
body = d["data"]
assert d["ok"] is True
assert body["likeCount"] >= 1
assert len(body["comments"]) >= 1
assert any(c.get("content") == "$COMMENT" for c in body["comments"]), body
print("OK likeCount=", body["likeCount"], "comments=", len(body["comments"]))
PY

echo "ALL OK"
