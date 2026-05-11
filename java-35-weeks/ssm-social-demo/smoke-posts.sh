#!/usr/bin/env bash
# Day165: posts (create/list/detail) smoke
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

echo "==> ping"
curl -sSi "$SSM_BASE/api/ping" -o /tmp/ssm_social_ping.txt
head -n 10 /tmp/ssm_social_ping.txt
echo

echo "==> create post without login (should be 401)"
curl -sSi -X POST "$SSM_BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\",\"content\":\"$CONTENT\"}" \
  -o /tmp/ssm_social_post_unauth.txt
head -n 20 /tmp/ssm_social_post_unauth.txt
echo

echo "==> register (login via session cookie)"
curl -sSi -c "$COOKIE_JAR" -X POST "$SSM_BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\",\"password\":\"$P\"}" \
  -o /tmp/ssm_social_register_post.txt
head -c 500 /tmp/ssm_social_register_post.txt
echo
echo

echo "==> create post (should be 201)"
curl -sSi -b "$COOKIE_JAR" -X POST "$SSM_BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\",\"content\":\"$CONTENT\"}" \
  -o /tmp/ssm_social_post_create.txt
head -c 600 /tmp/ssm_social_post_create.txt
echo
echo

POST_ID="$(grep -oE '\"id\":[0-9]+' /tmp/ssm_social_post_create.txt | head -n 1 | cut -d: -f2 || true)"
if [[ -z "${POST_ID:-}" ]]; then
  echo "ERROR: cannot parse post id" >&2
  exit 1
fi
echo "POST_ID=$POST_ID"
echo

echo "==> list posts"
curl -sSi "$SSM_BASE/api/posts?limit=5&offset=0" -o /tmp/ssm_social_posts_list.txt
head -c 900 /tmp/ssm_social_posts_list.txt
echo
echo

echo "==> post detail"
curl -sSi "$SSM_BASE/api/posts/$POST_ID" -o /tmp/ssm_social_post_detail.txt
head -c 700 /tmp/ssm_social_post_detail.txt
echo

