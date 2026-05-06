#!/usr/bin/env bash
# W25 / Day173: posts smoke (register + create + list + detail)
set -euo pipefail

BASE="${BASE:-http://localhost:8081}"
COOKIE_JAR="${COOKIE_JAR:-/tmp/boot_social_cookie.txt}"

U="u$RANDOM"
P="p$RANDOM$RANDOM"
TITLE="t$RANDOM"
CONTENT="hello post $RANDOM"

echo "BASE=$BASE"
echo "COOKIE_JAR=$COOKIE_JAR"
echo

rm -f "$COOKIE_JAR"

echo "==> register"
curl -sSi -c "$COOKIE_JAR" -X POST "$BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\",\"password\":\"$P\"}" \
  -o /tmp/boot_social_register.txt
head -c 500 /tmp/boot_social_register.txt
echo
echo

echo "==> create post (201)"
curl -sSi -b "$COOKIE_JAR" -X POST "$BASE/api/posts" \
  -H 'Content-Type: application/json' \
  -d "{\"title\":\"$TITLE\",\"content\":\"$CONTENT\"}" \
  -o /tmp/boot_social_post_create.txt
head -c 800 /tmp/boot_social_post_create.txt
echo
echo

POST_ID="$(grep -oE '\"id\":[0-9]+' /tmp/boot_social_post_create.txt | head -n 1 | cut -d: -f2 || true)"
if [[ -z "${POST_ID:-}" ]]; then
  echo "ERROR: cannot parse post id from body. Response saved at /tmp/boot_social_post_create.txt" >&2
  exit 1
fi
echo "POST_ID=$POST_ID"
echo

echo "==> list posts (page/size)"
curl -sSi "$BASE/api/posts?page=1&size=5" -o /tmp/boot_social_posts_list.txt
head -c 1200 /tmp/boot_social_posts_list.txt
echo
echo

echo "==> post detail"
curl -sSi "$BASE/api/posts/$POST_ID" -o /tmp/boot_social_post_detail.txt
head -c 900 /tmp/boot_social_post_detail.txt
echo
