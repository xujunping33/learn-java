#!/usr/bin/env bash
# Day161：TrafficStatsInterceptor + GET /api/stats
set -euo pipefail

BASE="${BASE:-http://localhost:8080/spring-mvc-demo}"
KEY_HEADER=(-H 'X-Api-Key: w23-demo-key')

echo "==> warm up: ping (no key)"
curl -sS "$BASE/api/ping"
echo

echo "==> warm up: students list (need key)"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/students" >/dev/null

echo "==> stats (no key -> 401)"
curl -sS -w '\nHTTP %{http_code}\n' "$BASE/api/stats"
echo

echo "==> stats"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/stats"
echo

