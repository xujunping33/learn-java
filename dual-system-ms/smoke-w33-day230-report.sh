#!/usr/bin/env bash
set -euo pipefail

GATEWAY_BASE="${GATEWAY_BASE:-http://localhost:9080}"
DATE_UTC="${DATE_UTC:-$(date -u +%F)}"
REPORT_API_KEY="${REPORT_API_KEY:-dev-report-key}"

echo "[smoke] date=${DATE_UTC} (UTC)"

echo "[smoke] create order..."
order_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":12.34}'
)"
order_id="$(echo "${order_resp}" | sed -n 's/.*"orderId":[ ]*\([0-9]\+\).*/\1/p')"
[[ -n "${order_id}" ]]

echo "[smoke] create payment..."
pay_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/payments" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${order_id},\"userId\":1,\"amount\":12.34}"
)"
payment_id="$(echo "${pay_resp}" | sed -n 's/.*"paymentId":[ ]*\([0-9]\+\).*/\1/p')"
[[ -n "${payment_id}" ]]

echo "[smoke] confirm payment..."
curl -fsS -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null

echo "[smoke] query report..."
report="$(curl -fsS -H "X-Report-Key: ${REPORT_API_KEY}" "${GATEWAY_BASE}/api/reports/daily?date=${DATE_UTC}")"
echo "[smoke] report: ${report}"

echo "${report}" | grep -q "\"date\":\"${DATE_UTC}\""
echo "${report}" | grep -Eq "\"paidCount\":[ ]*[1-9]"
echo "${report}" | grep -Eq "\"paidAmountSum\":[ ]*([1-9][0-9]*|0)\\."

echo "[smoke] OK"

