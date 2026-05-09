#!/usr/bin/env bash
# Day133：假设 oa-demo 已部署且 DB 可用；从健康检查到「员工提交 → 经理通过 → 员工列表见 APPROVED」。
set -euo pipefail
BASE="${BASE:-http://127.0.0.1:8080/oa-demo}"
DIR="$(mktemp -d)"
trap 'rm -rf "$DIR"' EXIT

echo "== GET /api/health =="
curl -sS "$BASE/api/health" | python3 -m json.tool

echo "== emp login =="
curl -sS -c "$DIR/emp.jar" -X POST "$BASE/api/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"emp","password":"emp123"}' | python3 -m json.tool

echo "== emp POST /api/leaves =="
SUBMIT_JSON=$(curl -sS -b "$DIR/emp.jar" -X POST "$BASE/api/leaves" \
  -H "Content-Type: application/json" \
  -d '{"leaveType":"ANNUAL","startAt":"2026-07-01T09:00:00","endAt":"2026-07-02T18:00:00","reason":"Day133 smoke"}')
echo "$SUBMIT_JSON" | python3 -m json.tool
LEAVE_ID=$(echo "$SUBMIT_JSON" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['id'])")
echo "LEAVE_ID=$LEAVE_ID"

echo "== mgr login =="
curl -sS -c "$DIR/mgr.jar" -X POST "$BASE/api/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"mgr","password":"mgr123"}' | python3 -m json.tool

echo "== mgr POST .../approve =="
curl -sS -b "$DIR/mgr.jar" -X POST "$BASE/api/leaves/${LEAVE_ID}/approve" | python3 -m json.tool

echo "== emp GET /api/leaves/me (expect APPROVED for this id) =="
curl -sS -b "$DIR/emp.jar" "$BASE/api/leaves/me" | python3 -m json.tool

echo "OK smoke finished."
