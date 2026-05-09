#!/usr/bin/env bash
# W30 Day210：可靠性收口 — 注册→发帖→评论→点赞→通知落库；幂等不变量；FAIL_CONSUME→DLQ→replay。
# 依赖：curl、jq；Compose MySQL（用于断言）；可选 RabbitMQ（DLQ 演示）。
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BASE="${BASE:-http://127.0.0.1:8081}"
PW="${PW:-p123456}"

if ! command -v jq >/dev/null 2>&1; then
    echo "ERROR: 需要 jq（解析 ApiResult）" >&2
    exit 1
fi

echo "BASE=$BASE"

trap 'curl -sS -m 2 -X POST "${BASE}/api/dev/mq/failure?enabled=false" >/dev/null 2>&1 || true' EXIT

if ! curl -sS --connect-timeout 3 --max-time 5 -o /dev/null "$BASE/api/ping"; then
    echo "ERROR: 无法访问 $BASE/api/ping（请先 docker compose up -d 或 spring-boot:run）" >&2
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

UA="w30a_$RANDOM$RANDOM"
UB="w30b_$RANDOM$RANDOM"

echo "==> register A/B"
RA="$(curl -sS -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' -d "{\"username\":\"$UA\",\"password\":\"$PW\"}")"
RB="$(curl -sS -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' -d "{\"username\":\"$UB\",\"password\":\"$PW\"}")"

echo "$RA" | jq -e '.data.tokenValue' >/dev/null
echo "$RB" | jq -e '.data.tokenValue' >/dev/null

A_TV="$(echo "$RA" | jq -r '.data.tokenValue')"
B_TV="$(echo "$RB" | jq -r '.data.tokenValue')"
B_ID="$(echo "$RB" | jq -r '.data.id')"

echo "==> A creates post"
RP="$(curl -sS -X POST "$BASE/api/posts" -H "Authorization: Bearer $A_TV" -H 'Content-Type: application/json' \
    -d '{"title":"w30 reliability","content":"smoke"}')"
POST_ID="$(echo "$RP" | jq -r '.data.id')"
echo "POST_ID=$POST_ID"

echo "==> B comments + likes (happy path)"
RC="$(curl -sS -X POST "$BASE/api/posts/$POST_ID/comments" -H "Authorization: Bearer $B_TV" -H 'Content-Type: application/json' \
    -d "{\"content\":\"w30 happy $RANDOM\"}")"
COMMENT_OK="$(echo "$RC" | jq -r '.data.id')"
echo "COMMENT_OK=$COMMENT_OK"

HTTP_LIKE="$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$BASE/api/posts/$POST_ID/like" -H "Authorization: Bearer $B_TV")"
if [ "$HTTP_LIKE" != "204" ]; then
    echo "ERROR: like 期望 HTTP 204，实际 $HTTP_LIKE" >&2
    exit 1
fi

echo "==> wait outbox + consumer"
sleep 5

SQL_HAPPY="SELECT COUNT(*) FROM notifications WHERE dedup_key IN ('comment:${COMMENT_OK}','like:${POST_ID}:${B_ID}')"
CNT="$(mysql_sel "$SQL_HAPPY")"
echo "notifications(happy dedup_keys): count=$CNT (expect 2)"
if [ "$CNT" != "2" ]; then
    echo "ERROR: happy path 期望 notifications 2 行" >&2
    exit 1
fi

echo "==> idle DLQ replay x2（队列空则计数应保持不变，幂等不变量）"
C_BEFORE="$CNT"
curl -sS -X POST "$BASE/api/dev/dlq/replay?limit=10" >/dev/null || true
curl -sS -X POST "$BASE/api/dev/dlq/replay?limit=10" >/dev/null || true
sleep 2
CNT="$(mysql_sel "$SQL_HAPPY")"
echo "notifications after idle replay: count=$CNT"
if [ "$CNT" != "$C_BEFORE" ]; then
    echo "ERROR: 空闲 replay 不应改变已有 dedup 行数" >&2
    exit 1
fi

echo "==> wait rate-limit window（避免第 3 条评论触发 429）"
sleep 11

echo "==> simulated consume failure → DLQ → replay（故意失败 → 可重放）"
curl -sS -X POST "$BASE/api/dev/mq/failure?enabled=true" >/dev/null
FAIL_ON="$(curl -sS "$BASE/api/dev/mq/failure" | jq -r '.data')"
if [ "$FAIL_ON" != "true" ]; then
    echo "ERROR: 模拟失败开关应为 true，实际 $FAIL_ON" >&2
    exit 1
fi

RFC="$(curl -sS -X POST "$BASE/api/posts/$POST_ID/comments" -H "Authorization: Bearer $B_TV" -H 'Content-Type: application/json' \
    -d '{"content":"hello FAIL_CONSUME replay demo"}')"
CID_FAIL="$(echo "$RFC" | jq -r '.data.id')"
echo "CID_FAIL=$CID_FAIL"

SQL_FAIL="SELECT COUNT(*) FROM notifications WHERE dedup_key='comment:${CID_FAIL}'"

echo "==> wait retries + DLQ（Spring AMQP 重试 + 进入 DLQ 常需 >8s）"
sleep 15

curl -sS -X POST "$BASE/api/dev/mq/failure?enabled=false" >/dev/null
FAIL_OFF="$(curl -sS "$BASE/api/dev/mq/failure" | jq -r '.data')"
if [ "$FAIL_OFF" != "false" ]; then
    echo "ERROR: replay 前应关闭模拟失败，当前 $FAIL_OFF" >&2
    exit 1
fi

echo "==> replay DLQ"
curl -sS -X POST "$BASE/api/dev/dlq/replay?limit=20" | jq

echo "==> wait consumer after replay（DB 轮询）"
FC=""
for _ in $(seq 1 25); do
    FC="$(mysql_sel "$SQL_FAIL")"
    if [ "$FC" = "1" ]; then
        break
    fi
    sleep 2
done
echo "notifications(FAIL comment dedup): count=$FC (expect 1)"
if [ "$FC" != "1" ]; then
    echo "ERROR: replay 后期望 comment:${CID_FAIL} 恰好 1 行" >&2
    exit 1
fi

SQL_ALL="SELECT COUNT(*) FROM notifications WHERE dedup_key IN ('comment:${COMMENT_OK}','like:${POST_ID}:${B_ID}','comment:${CID_FAIL}')"
TOT="$(mysql_sel "$SQL_ALL")"
echo "notifications(all three dedup_keys): count=$TOT (expect 3)"
if [ "$TOT" != "3" ]; then
    echo "ERROR: 期望三条 tracked dedup 各一行" >&2
    exit 1
fi

echo "==> sample rows"
docker compose exec -T mysql sh -lc "mysql -u\"\$MYSQL_USER\" -p\"\$MYSQL_PASSWORD\" \"\$MYSQL_DATABASE\" -e \"
SELECT id,user_id,type,ref_id,dedup_key,created_at FROM notifications
WHERE dedup_key IN ('comment:${COMMENT_OK}','like:${POST_ID}:${B_ID}','comment:${CID_FAIL}')
ORDER BY id;
\""

echo "ALL OK — W30 Day210 smoke passed"
