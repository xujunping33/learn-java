#!/usr/bin/env bash
# W34 Day238 收口：全流程 + MQ→订单 PAID + 报表 B 端 key +（支付）重复确认幂等 + 固定 X-Request-Id 便于日志 grep
set -euo pipefail

GATEWAY_BASE="${GATEWAY_BASE:-http://localhost:9080}"
DATE_UTC="${DATE_UTC:-$(date -u +%F)}"
REPORT_API_KEY="${REPORT_API_KEY:-dev-report-key}"
REQ_ID="${SMOKE_REQUEST_ID:-w34-smoke-$(date +%s)-$$}"
CURL_HDR=( -H "X-Request-Id: ${REQ_ID}" )

echo "[smoke-w34] base=${GATEWAY_BASE}"
echo "[smoke-w34] date=${DATE_UTC} (UTC)"
echo "[smoke-w34] X-Request-Id=${REQ_ID}  (grep this in order/payment/report logs: [req=${REQ_ID}] or gateway DEBUG)"

echo "[smoke-w34] ping gateway..."
curl -fsS "${GATEWAY_BASE}/actuator/health" >/dev/null || true

echo "[smoke-w34] create order..."
order_resp="$(
  curl -fsS "${CURL_HDR[@]}" -X POST "${GATEWAY_BASE}/api/orders" \
    -H 'Content-Type: application/json' \
    -d '{"userId":1,"amount":12.34}'
)"
echo "[smoke-w34] order: ${order_resp}"
order_id="$(echo "${order_resp}" | sed -n 's/.*"orderId":[ ]*\([0-9]\+\).*/\1/p')"
if [[ -z "${order_id}" ]]; then
  echo "[smoke-w34] failed to parse orderId"
  exit 1
fi

echo "[smoke-w34] create payment..."
pay_resp="$(
  curl -fsS "${CURL_HDR[@]}" -X POST "${GATEWAY_BASE}/api/payments" \
    -H 'Content-Type: application/json' \
    -d "{\"orderId\":${order_id},\"userId\":1,\"amount\":12.34}"
)"
echo "[smoke-w34] payment: ${pay_resp}"
payment_id="$(echo "${pay_resp}" | sed -n 's/.*"paymentId":[ ]*\([0-9]\+\).*/\1/p')"
if [[ -z "${payment_id}" ]]; then
  echo "[smoke-w34] failed to parse paymentId"
  exit 1
fi

echo "[smoke-w34] confirm payment 3x (same id; payment + MQ idempotent paths)..."
curl -fsS "${CURL_HDR[@]}" -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null
curl -fsS "${CURL_HDR[@]}" -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null
curl -fsS "${CURL_HDR[@]}" -X POST "${GATEWAY_BASE}/api/payments/${payment_id}/confirm" >/dev/null

echo "[smoke-w34] verify order PAID (poll: outbox/MQ consumer may lag)..."
detail=""
deadline=$((SECONDS + 45))
tmp="$(mktemp)"
trap 'rm -f "${tmp}"' EXIT
while [[ "${SECONDS}" -lt "${deadline}" ]]; do
  http="$(curl -sS "${CURL_HDR[@]}" -o "${tmp}" -w "%{http_code}" "${GATEWAY_BASE}/api/orders/${order_id}")"
  if [[ "${http}" == "429" ]]; then
    sleep 2
    continue
  fi
  if [[ "${http}" != "200" ]]; then
    echo "[smoke-w34] unexpected HTTP ${http} when GET order"
    cat "${tmp}" >&2 || true
    exit 1
  fi
  detail="$(cat "${tmp}")"
  if echo "${detail}" | grep -q '"status":"PAID"' && echo "${detail}" | grep -q "\"paidPaymentId\":${payment_id}"; then
    echo "[smoke-w34] order detail: ${detail}"
    break
  fi
  sleep 1.2
done
trap - EXIT
rm -f "${tmp}"
if ! echo "${detail}" | grep -q '"status":"PAID"'; then
  echo "[smoke-w34] order detail: ${detail}"
  echo "[smoke-w34] timeout: order still not PAID."
  exit 1
fi
echo "${detail}" | grep -q "\"paidPaymentId\":${payment_id}"

echo "[smoke-w34] GET order again — final state unchanged after duplicate confirms..."
# Sentinel SENTINEL_LOCAL_QPS=1 时隔得太近会 429；与 poll 段一致做 429 重试（勿用 curl -f）
detail2=""
tmp2="$(mktemp)"
cleanup_tmp2() { rm -f "${tmp2}"; }
trap cleanup_tmp2 EXIT
for _ in 1 2 3 4 5 6 7 8; do
  http2="$(curl -sS "${CURL_HDR[@]}" -o "${tmp2}" -w "%{http_code}" "${GATEWAY_BASE}/api/orders/${order_id}")"
  if [[ "${http2}" == "429" ]]; then
    sleep 2
    continue
  fi
  if [[ "${http2}" != "200" ]]; then
    echo "[smoke-w34] unexpected HTTP ${http2} on second GET order"
    cat "${tmp2}" >&2 || true
    exit 1
  fi
  detail2="$(cat "${tmp2}")"
  break
done
trap - EXIT
rm -f "${tmp2}"
if [[ -z "${detail2}" ]]; then
  echo "[smoke-w34] second GET order failed (repeated 429 or empty body)"
  exit 1
fi
echo "${detail}" | grep -q '"status":"PAID"'
echo "${detail2}" | grep -q "\"paidPaymentId\":${payment_id}"

echo "[smoke-w34] report daily (B-key)..."
report="$(curl -fsS "${CURL_HDR[@]}" -H "X-Report-Key: ${REPORT_API_KEY}" "${GATEWAY_BASE}/api/reports/daily?date=${DATE_UTC}")"
echo "[smoke-w34] report: ${report}"
echo "${report}" | grep -q "\"date\":\"${DATE_UTC}\""

report_no_hdr="$(curl -sS -o /dev/null -w "%{http_code}" "${GATEWAY_BASE}/api/reports/daily?date=${DATE_UTC}")"
if [[ "${report_no_hdr}" == "401" ]]; then
  echo "[smoke-w34] report auth: omit X-Report-Key -> 401 (expected when REPORT_API_KEY set on gateway)"
elif [[ "${report_no_hdr}" == "200" ]]; then
  echo "[smoke-w34] report auth: gateway key empty -> 200 without header (local dev)"
fi

echo "[smoke-w34] sentinel burst (optional)..."
codes="$(
  seq 1 30 | xargs -P20 -I{} curl -s -o /dev/null -w '%{http_code}\n' "${GATEWAY_BASE}/api/orders/${order_id}" \
    | sort | uniq -c
)"
echo "${codes}"

echo "[smoke-w34] OK"
