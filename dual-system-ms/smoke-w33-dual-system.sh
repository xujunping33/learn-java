#!/usr/bin/env bash
set -euo pipefail

GATEWAY_BASE="${GATEWAY_BASE:-http://localhost:9080}"
DATE_UTC="${DATE_UTC:-$(date -u +%F)}"
# Day237：与 docker compose 默认 REPORT_API_KEY 对齐；网关未配置 key 时多带此头无副作用
REPORT_API_KEY="${REPORT_API_KEY:-dev-report-key}"

echo "[smoke] base=${GATEWAY_BASE}"
echo "[smoke] date=${DATE_UTC} (UTC)"

echo "[smoke] ping gateway..."
curl -fsS "${GATEWAY_BASE}/actuator/health" >/dev/null || true

echo "[smoke] create order..."
order_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":12.34}'
)"
echo "[smoke] order: ${order_resp}"
order_id="$(echo "${order_resp}" | sed -n 's/.*"orderId":[ ]*\([0-9]\+\).*/\1/p')"
if [[ -z "${order_id}" ]]; then
  echo "[smoke] failed to parse orderId"
  exit 1
fi

echo "[smoke] create payment..."
pay_resp="$(
  curl -fsS -X POST "${GATEWAY_BASE}/api/payments" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${order_id},\"userId\":1,\"amount\":12.34}"
)"
echo "[smoke] payment: ${pay_resp}"
payment_id="$(echo "${pay_resp}" | sed -n 's/.*"paymentId":[ ]*\([0-9]\+\).*/\1/p')"
if [[ -z "${payment_id}" ]]; then
  echo "[smoke] failed to parse paymentId"
  exit 1
fi

echo "[smoke] confirm payment twice (idempotent)..."
curl -fsS -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null
curl -fsS -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null

echo "[smoke] verify order PAID (poll: MQ consumer may lag; 429 from Sentinel is retried)..."
detail=""
deadline=$((SECONDS + 45))
tmp="$(mktemp)"
trap 'rm -f "${tmp}"' EXIT
while [[ "${SECONDS}" -lt "${deadline}" ]]; do
  http="$(curl -sS -o "${tmp}" -w "%{http_code}" "${GATEWAY_BASE}/api/orders/${order_id}")"
  if [[ "${http}" == "429" ]]; then
    sleep 2
    continue
  fi
  if [[ "${http}" != "200" ]]; then
    echo "[smoke] unexpected HTTP ${http} when GET order"
    cat "${tmp}" >&2 || true
    exit 1
  fi
  detail="$(cat "${tmp}")"
  if echo "${detail}" | grep -q '"status":"PAID"' && echo "${detail}" | grep -q "\"paidPaymentId\":${payment_id}"; then
    echo "[smoke] order detail: ${detail}"
    break
  fi
  # stay under Sentinel QPS=1 if enabled
  sleep 1.2
done
trap - EXIT
rm -f "${tmp}"
if ! echo "${detail}" | grep -q '"status":"PAID"'; then
  echo "[smoke] order detail: ${detail}"
  echo "[smoke] timeout: order still not PAID. Check: ds-order-service logs, RabbitMQ queue dual.order.payment-succeeded,"
  echo "[smoke]   MySQL table ds_order.processed_payment_events exists, RABBITMQ_HOST/PORT for order-service."
  exit 1
fi
echo "${detail}" | grep -q "\"paidPaymentId\":${payment_id}"

echo "[smoke] report daily..."
report="$(curl -fsS -H "X-Report-Key: ${REPORT_API_KEY}" "${GATEWAY_BASE}/api/reports/daily?date=${DATE_UTC}")"
echo "[smoke] report: ${report}"
echo "${report}" | grep -q "\"date\":\"${DATE_UTC}\""

report_no_hdr="$(curl -sS -o /dev/null -w "%{http_code}" "${GATEWAY_BASE}/api/reports/daily?date=${DATE_UTC}")"
if [[ "${report_no_hdr}" == "401" ]]; then
  echo "[smoke] report auth: omitting X-Report-Key -> 401 (expected when gateway REPORT_API_KEY is set)"
elif [[ "${report_no_hdr}" == "200" ]]; then
  echo "[smoke] report auth: gateway report.api-key empty -> 200 without header (local dev)"
fi

echo "[smoke] sentinel check (optional; requires SENTINEL_LOCAL_QPS=1 on order-service)..."
codes="$(
  seq 1 30 | xargs -P20 -I{} curl -s -o /dev/null -w '%{http_code}\n' "${GATEWAY_BASE}/api/orders/${order_id}" \
    | sort | uniq -c
)"
echo "${codes}"

echo "[smoke] OK"

