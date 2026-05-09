#!/usr/bin/env bash
set -euo pipefail

GATEWAY_BASE="${GATEWAY_BASE:-http://localhost:9080}"

echo "[smoke] create order via gateway..."
resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":12.34}'
)"
echo "[smoke] create response: ${resp}"

order_id="$(echo "${resp}" | sed -n 's/.*"orderId":[ ]*\([0-9]\+\).*/\1/p')"
if [[ -z "${order_id}" ]]; then
  echo "[smoke] failed to parse orderId"
  exit 1
fi

echo "[smoke] get order ${order_id} via gateway..."
detail="$(curl -fsS "${GATEWAY_BASE}/api/orders/${order_id}")"
echo "[smoke] detail: ${detail}"

if ! echo "${detail}" | grep -q '"status":"CREATED"'; then
  echo "[smoke] expected status CREATED"
  exit 1
fi

echo "[smoke] OK"

