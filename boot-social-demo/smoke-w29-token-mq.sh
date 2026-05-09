#!/usr/bin/env bash
# W29 Day201-203：Sa-Token + MQ 通知 + DLQ/replay 最小闭环
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BASE="${BASE:-http://127.0.0.1:8081}"
PW="${PW:-p123456}"

echo "BASE=$BASE"

if ! curl -sS --connect-timeout 3 --max-time 5 -o /dev/null "$BASE/api/ping"; then
  echo "ERROR: 无法连上 $BASE（应用未启动？可先 docker compose up -d --build）" >&2
  exit 1
fi

echo "==> enable simulated consumer failure"
curl -sS -X POST "$BASE/api/dev/mq/failure?enabled=true" >/dev/null

UA="w29a_$RANDOM$RANDOM"
UB="w29b_$RANDOM$RANDOM"

echo "==> register A/B"
RA="$(curl -sS -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' -d "{\"username\":\"$UA\",\"password\":\"$PW\"}")"
RB="$(curl -sS -X POST "$BASE/api/auth/register" -H 'Content-Type: application/json' -d "{\"username\":\"$UB\",\"password\":\"$PW\"}")"

A_TOKEN="$(echo "$RA" | jq -r '.data.tokenPrefix+" "+.data.tokenValue')"
B_TOKEN="$(echo "$RB" | jq -r '.data.tokenPrefix+" "+.data.tokenValue')"
A_ID="$(echo "$RA" | jq -r '.data.id')"
B_ID="$(echo "$RB" | jq -r '.data.id')"

echo "==> create post by A"
RP="$(curl -sS -X POST "$BASE/api/posts" -H "Authorization: $A_TOKEN" -H 'Content-Type: application/json' -d '{"title":"w29","content":"mq smoke"}')"
POST_ID="$(echo "$RP" | jq -r '.data.id')"
OWNER_ID="$(echo "$RP" | jq -r '.data.userId')"
echo "POST_ID=$POST_ID OWNER_ID=$OWNER_ID"

echo "==> add comment by B (FAIL_CONSUME -> DLQ expected)"
RC="$(curl -sS -X POST "$BASE/api/posts/$POST_ID/comments" -H "Authorization: $B_TOKEN" -H 'Content-Type: application/json' -d '{"content":"hello FAIL_CONSUME"}')"
COMMENT_ID="$(echo "$RC" | jq -r '.data.id')"
echo "COMMENT_ID=$COMMENT_ID"

echo "==> like by B (should succeed)"
curl -sS -X POST "$BASE/api/posts/$POST_ID/like" -H "Authorization: $B_TOKEN" >/dev/null

echo "==> wait for retries + DLQ"
sleep 5

echo "==> queues"
docker compose exec -T rabbitmq rabbitmqctl list_queues name messages consumers | egrep "bootsocial.notify.queue|bootsocial.notify.dlq|name"

echo "==> disable simulated failure"
curl -sS -X POST "$BASE/api/dev/mq/failure?enabled=false" >/dev/null

echo "==> replay DLQ"
curl -sS -X POST "$BASE/api/dev/dlq/replay?limit=10" | jq

echo "==> check notifications in DB"
docker compose exec -T mysql sh -lc "mysql -uadmin -p\"\$MYSQL_PASSWORD\" -D\"\$MYSQL_DATABASE\" -e \"
SELECT id,user_id,type,ref_id,dedup_key,created_at
FROM notifications
WHERE dedup_key IN ('comment:$COMMENT_ID','like:$POST_ID:$B_ID')
ORDER BY id DESC;
\""

echo "ALL OK (if rows present above)"

