#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
USER_URL="${USER_URL:-http://localhost:8082}"
POST_URL="${POST_URL:-http://localhost:8083}"
NACOS_URL="${NACOS_URL:-http://localhost:8848}"

echo "[smoke] checking endpoints..."

curl -fsS "$NACOS_URL/nacos/" >/dev/null
echo "[ok] nacos console reachable: $NACOS_URL/nacos/"

curl -fsS "$GATEWAY_URL/ping" >/dev/null
echo "[ok] gateway ping: $GATEWAY_URL/ping"

curl -fsS "$USER_URL/ping" >/dev/null
echo "[ok] user-service ping: $USER_URL/ping"

curl -fsS "$POST_URL/ping" >/dev/null
echo "[ok] post-service ping: $POST_URL/ping"

echo
echo "[smoke] gateway routes..."

u="$GATEWAY_URL/api/users/1"
curl -fsS "$u" | grep -q "\"username\""
echo "[ok] route /api/users/** -> user-service"

p="$GATEWAY_URL/api/posts/1"
curl -fsS "$p" | grep -q "\"authorUsername\""
echo "[ok] route /api/posts/** -> post-service (and Feign->user-service)"

echo
echo "[smoke] sentinel rate limit (expect 429 appears)..."
echo "[hint] If you don't use dashboard rules, restart post-service with: SENTINEL_LOCAL_QPS=1"

tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

# Send a short burst of concurrent requests to exceed QPS=1 reliably.
has_429=0
for round in 1 2 3; do
  : >"$tmp"
  seq 1 200 | xargs -P20 -I{} bash -c \
    "curl -sS -o /dev/null -w \"%{http_code}\n\" \"$POST_URL/api/posts/1\" >>\"$tmp\" || true"

  if grep -q "^429$" "$tmp"; then
    has_429=1
    break
  fi
  sleep 1
done

if [[ "$has_429" != "1" ]]; then
  cat <<'EOF'
[warn] did not observe HTTP 429.

This can happen if the Sentinel rule is not applied, or traffic is not exceeding the threshold.
Please ensure:
- In Sentinel dashboard, app is boot-social-post-service
- post-service is started with SENTINEL_DASHBOARD=127.0.0.1:8858 (so it can receive rules)
- In dashboard top-right "机器列表/机器" dropdown, select the current instance (host:8719)
- Resource is "getPostById"
- Metric type is QPS, threshold is 1, direct / fast-fail
EOF
  exit 0
fi

echo "[ok] observed HTTP 429 (RATE_LIMITED) from post-service"
echo
echo "[done] smoke passed"

