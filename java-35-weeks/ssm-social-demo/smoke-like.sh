#!/usr/bin/env bash
# Day167: like (dedupe) + likeCount smoke
set -euo pipefail

SSM_BASE="${SSM_BASE:-http://localhost:8080/ssm-social-demo}"
COOKIE_JAR="${COOKIE_JAR:-/tmp/ssm_social_cookie.txt}"

U="u$RANDOM"
P="p$RANDOM$RANDOM"
TITLE="t$RANDOM"
CONTENT="hello post $RANDOM"

echo "SSM_BASE=$SSM_BASE"
echo "COOKIE_JAR=$COOKIE_JAR"
echo

rm -f "$COOKIE_JAR"

echo "==> register (login via session cookie)"
curl -sSi -c "$COOKIE_JAR" -X POST "$SSM_BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\",\"password\":\"$P\"}" \
  -o /tmp/ssm_social_like_register.txt
head -c 500 /tmp/ssm_social_like_register.txt
echo
echo

echo "==> create post"
curl -sSi -b "$COOKIE_JAR" -X POST "$SSM_BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\",\"content\":\"$CONTENT\"}" \
  -o /tmp/ssm_social_like_post_create.txt
head -c 650 /tmp/ssm_social_like_post_create.txt
echo
echo

POST_ID="$(grep -oE '\"id\":[0-9]+' /tmp/ssm_social_like_post_create.txt | head -n 1 | cut -d: -f2 || true)"
if [[ -z "${POST_ID:-}" ]]; then
  echo "ERROR: cannot parse post id" >&2
  exit 1
fi
echo "POST_ID=$POST_ID"
echo

echo "==> like without login (should be 401)"
curl -sSi -X POST "$SSM_BASE/api/posts/$POST_ID/like" -o /tmp/ssm_social_like_unauth.txt
head -n 20 /tmp/ssm_social_like_unauth.txt
echo

echo "==> like #1 (204)"
curl -sSi -b "$COOKIE_JAR" -X POST "$SSM_BASE/api/posts/$POST_ID/like" -o /tmp/ssm_social_like_1.txt
head -n 15 /tmp/ssm_social_like_1.txt
echo

echo "==> like #2 (still 204, should NOT increment)"
curl -sSi -b "$COOKIE_JAR" -X POST "$SSM_BASE/api/posts/$POST_ID/like" -o /tmp/ssm_social_like_2.txt
head -n 15 /tmp/ssm_social_like_2.txt
echo

echo "==> post detail (likeCount should be 1)"
curl -sSi "$SSM_BASE/api/posts/$POST_ID" -o /tmp/ssm_social_like_detail.txt
head -c 700 /tmp/ssm_social_like_detail.txt
echo
echo

LIKE_COUNT="$(grep -oE '\"likeCount\":[0-9]+' /tmp/ssm_social_like_detail.txt | head -n 1 | cut -d: -f2 || true)"
echo "LIKE_COUNT=$LIKE_COUNT"
if [[ "$LIKE_COUNT" != "1" ]]; then
  echo "ERROR: likeCount expected 1, got $LIKE_COUNT" >&2
  exit 1
fi

echo "OK: likeCount dedupe works"

