#!/usr/bin/env bash
# W25 Day174: comments + idempotent likes + aggregated post detail
set -euo pipefail

BASE="${BASE:-http://localhost:8081}"
COOKIE1="${COOKIE_JAR:-/tmp/boot_social_day174_u1.txt}"
COOKIE2="${COOKIE_JAR2:-/tmp/boot_social_day174_u2.txt}"

U1="u1_$RANDOM"
U2="u2_$RANDOM"
P="p$RANDOM$RANDOM"
TITLE="t$RANDOM"
CONTENT="hello post $RANDOM"
COMMENT="comment $RANDOM"

echo "BASE=$BASE"
rm -f "$COOKIE1" "$COOKIE2"

echo "==> register user1"
curl -sS -c "$COOKIE1" -X POST "$BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U1\",\"password\":\"$P\"}" \
  -o /tmp/day174_reg1.json

echo "==> create post"
curl -sS -b "$COOKIE1" -X POST "$BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\",\"content\":\"$CONTENT\"}" \
  -o /tmp/day174_post.json
POST_ID="$(python3 -c "import json; print(json.load(open('/tmp/day174_post.json'))['data']['id'])")"
echo "POST_ID=$POST_ID"

echo "==> add comment"
curl -sS -b "$COOKIE1" -X POST "$BASE/api/posts/$POST_ID/comments" \
  -H 'Content-Type: application/json' \
  -d "{\"content\":\"$COMMENT\"}" \
  -o /tmp/day174_comment.json

echo "==> list comments (subset)"
curl -sS "$BASE/api/posts/$POST_ID/comments" -o /tmp/day174_comments_list.json

echo "==> register user2 (second session)"
curl -sS -c "$COOKIE2" -X POST "$BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U2\",\"password\":\"$P\"}" \
  -o /tmp/day174_reg2.json

echo "==> user2 likes twice (idempotent)"
curl -sS -o /dev/null -w "%{http_code}\n" -b "$COOKIE2" -X POST "$BASE/api/posts/$POST_ID/like"
curl -sS -o /dev/null -w "%{http_code}\n" -b "$COOKIE2" -X POST "$BASE/api/posts/$POST_ID/like"

echo "==> post detail (expect likeCount=1, one comment)"
curl -sS "$BASE/api/posts/$POST_ID" -o /tmp/day174_detail.json
python3 - <<'PY'
import json
with open("/tmp/day174_detail.json") as f:
    d = json.load(f)
body = d["data"]
assert body["likeCount"] == 1, body
assert len(body["comments"]) == 1, body
assert body["comments"][0]["content"], body
print("OK detail likeCount=", body["likeCount"], "comments=", len(body["comments"]))
PY

echo "==> user1 likes once (second distinct liker)"
curl -sS -o /dev/null -w "%{http_code}\n" -b "$COOKIE1" -X POST "$BASE/api/posts/$POST_ID/like"

curl -sS "$BASE/api/posts/$POST_ID" -o /tmp/day174_detail2.json
python3 - <<'PY'
import json
with open("/tmp/day174_detail2.json") as f:
    d = json.load(f)
body = d["data"]
assert body["likeCount"] == 2, body
print("OK likeCount after user1 like=", body["likeCount"])
PY

echo "==> user2 unlike"
curl -sS -o /dev/null -w "%{http_code}\n" -b "$COOKIE2" -X DELETE "$BASE/api/posts/$POST_ID/like"

curl -sS "$BASE/api/posts/$POST_ID" -o /tmp/day174_detail3.json
python3 - <<'PY'
import json
with open("/tmp/day174_detail3.json") as f:
    d = json.load(f)
body = d["data"]
assert body["likeCount"] == 1, body
print("OK likeCount after unlike=", body["likeCount"])
PY

echo "ALL OK"
