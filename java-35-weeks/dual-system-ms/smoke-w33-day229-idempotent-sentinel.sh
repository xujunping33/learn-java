#!/usr/bin/env bash
set -euo pipefail

GATEWAY_BASE="${GATEWAY_BASE:-http://localhost:9080}"

echo "[smoke] create order..."
order_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":12.34}'
)"
order_id="$(echo "${order_resp}" | sed -n 's/.*"orderId":[ ]*\([0-9]\+\).*/\1/p')"
[[ -n "${order_id}" ]]
echo "[smoke] orderId=${order_id}"

echo "[smoke] create payment..."
pay_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/payments" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${order_id},\"userId\":1,\"amount\":12.34}"
)"
payment_id="$(echo "${pay_resp}" | sed -n 's/.*"paymentId":[ ]*\([0-9]\+\).*/\1/p')"
[[ -n "${payment_id}" ]]
echo "[smoke] paymentId=${payment_id}"

echo "[smoke] confirm payment twice (idempotent)..."
curl -fsS -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null
curl -fsS -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null

echo "[smoke] verify order paid..."
detail="$(curl -fsS "${GATEWAY_BASE}/api/orders/${order_id}")"
echo "${detail}" | grep -q '"status":"PAID"'
echo "${detail}" | grep -q "\"paidPaymentId\":${payment_id}"

echo "[smoke] verify sentinel rate limit (set SENTINEL_LOCAL_QPS=1 on order-service)..."
codes="$(
  seq 1 30 | xargs -P20 -I{} curl -s -o /dev/null -w '%{http_code}\n' "${GATEWAY_BASE}/api/orders/${order_id}" \
    | sort | uniq -c
)"
echo "${codes}"
echo "${codes}" | grep -Eq ' 429$' || {
  echo "[smoke] expected some 429 responses; did you set SENTINEL_LOCAL_QPS?"
  exit 1
}

echo "[smoke] OK"

