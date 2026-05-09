#!/usr/bin/env bash
# Day156～157：REST CRUD …；Day160：`X-Api-Key`
set -euo pipefail
BASE="${BASE:-http://localhost:8080/spring-mvc-demo}"
KEY_HEADER=(-H 'X-Api-Key: w23-demo-key')

echo "==> GET list (no key → 401)"
curl -sS -w '\nHTTP %{http_code}\n' "$BASE/api/students"
echo

echo "==> GET list"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/students" | head -c 500
echo

echo "==> GET filter name=ali"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/students?name=ali"
echo

echo "==> GET /1"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/students/1"
echo

echo "==> POST create (inspect Location)"
curl -sSi -X POST "$BASE/api/students" \
  "${KEY_HEADER[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"name":"carol","score":92}'
echo

echo "==> PUT /3（若尚无 id=3 请先确认 GET list）"
curl -sSi -X PUT "$BASE/api/students/3" \
  "${KEY_HEADER[@]}" \
  -H 'Content-Type: application/json' \
  -d '{"name":"carol","score":95}' || true
echo

echo "==> GET 404"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/students/404"
echo

echo "==> DELETE /3"
curl -sSi -o /tmp/del_body.txt -w 'HTTP %{http_code}\n' -X DELETE "$BASE/api/students/3" \
  "${KEY_HEADER[@]}" || true
cat /tmp/del_body.txt 2>/dev/null || true
echo

echo "==> Day158 GlobalExceptionHandler: 404 / bad JSON / BAD_REQUEST name"
curl -sS "${KEY_HEADER[@]}" "$BASE/api/students/99999"
echo
curl -sS -X POST "$BASE/api/students" "${KEY_HEADER[@]}" -H 'Content-Type: application/json' -d 'not-json'
echo
curl -sS -X POST "$BASE/api/students" "${KEY_HEADER[@]}" -H 'Content-Type: application/json' -d '{"name":"","score":1}'
echo
