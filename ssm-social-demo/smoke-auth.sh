#!/usr/bin/env bash
# Day164：Session auth smoke
set -euo pipefail

SSM_BASE="${SSM_BASE:-http://localhost:8080/ssm-social-demo}"
COOKIE_JAR="${COOKIE_JAR:-/tmp/ssm_social_cookie.txt}"

U="u$RANDOM"
P="p$RANDOM$RANDOM"

echo "SSM_BASE=$SSM_BASE"
echo "COOKIE_JAR=$COOKIE_JAR"
echo

rm -f "$COOKIE_JAR"

echo "==> ping (ensure app is up)"
curl -sSi "$SSM_BASE/api/ping" -o /tmp/ssm_social_ping.txt
head -n 15 /tmp/ssm_social_ping.txt
echo
echo

echo "==> register"
curl -sSi -c "$COOKIE_JAR" -X POST "$SSM_BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$U\",\"password\":\"$P\"}" \
  -o /tmp/ssm_social_register.txt
head -c 600 /tmp/ssm_social_register.txt
echo
echo

echo "==> me (should be 200)"
curl -sSi -b "$COOKIE_JAR" "$SSM_BASE/api/me" -o /tmp/ssm_social_me_1.txt
head -c 400 /tmp/ssm_social_me_1.txt
echo
echo

echo "==> logout (204)"
curl -sSi -b "$COOKIE_JAR" -X POST "$SSM_BASE/api/auth/logout" -o /tmp/ssm_social_logout.txt
head -c 300 /tmp/ssm_social_logout.txt
echo
echo

echo "==> me after logout (401)"
curl -sSi -b "$COOKIE_JAR" "$SSM_BASE/api/me" -o /tmp/ssm_social_me_2.txt
head -c 400 /tmp/ssm_social_me_2.txt
echo

