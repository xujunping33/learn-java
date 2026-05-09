#!/usr/bin/env bash
# Day163：MyBatis + DataSource smoke
set -euo pipefail

SSM_BASE="${SSM_BASE:-http://localhost:8080/ssm-social-demo}"
U="u$RANDOM"

echo "SSM_BASE=$SSM_BASE"
echo

echo "==> POST create user: $U"
curl -sSi -X POST "$SSM_BASE/api/users" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\"}" | head -c 600
echo

echo "==> GET by username"
curl -sSi "$SSM_BASE/api/users?username=$U" | head -c 600
echo

