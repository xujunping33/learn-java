#!/usr/bin/env bash
set -euo pipefail

GATEWAY_BASE="${GATEWAY_BASE:-http://localhost:9080}"

echo "[smoke] create order..."
order_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":12.34}'
)"
echo "[smoke] order: ${order_resp}"
order_id="$(echo "${order_resp}" | sed -n 's/.*"orderId":[ ]*\([0-9]\+\).*/\1/p')"
[[ -n "${order_id}" ]]

echo "[smoke] create payment..."
pay_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/payments" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${order_id},\"userId\":1,\"amount\":12.34}"
)"
echo "[smoke] payment: ${pay_resp}"
payment_id="$(echo "${pay_resp}" | sed -n 's/.*"paymentId":[ ]*\([0-9]\+\).*/\1/p')"
[[ -n "${payment_id}" ]]

echo "[smoke] confirm payment ${payment_id}..."
curl -fsS -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null

echo "[smoke] verify order paid..."
detail="$(curl -fsS "${GATEWAY_BASE}/api/orders/${order_id}")"
echo "[smoke] order detail: ${detail}"
echo "${detail}" | grep -q '"status":"PAID"'
echo "${detail}" | grep -q "\"paidPaymentId\":${payment_id}"

echo "[smoke] OK"

