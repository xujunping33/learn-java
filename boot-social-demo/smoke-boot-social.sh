#!/usr/bin/env bash
# W25：注册 → 发帖 → 列表/详情 → 评论 → 点赞 → 聚合详情
set -euo pipefail

BASE="${BASE:-http://127.0.0.1:8081}"
COOKIE="${COOKIE_JAR:-/tmp/boot_social_smoke_cookie.txt}"

U="smoke_$RANDOM"
P="p$RANDOM$RANDOM"
TITLE="title_$RANDOM"
CONTENT="post body $RANDOM"
COMMENT="nice $RANDOM"

echo "BASE=$BASE"
rm -f "$COOKIE"

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
