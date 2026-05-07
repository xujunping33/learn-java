## Summary
- 

## Risk / Rollback
- **Risk**:
- **Rollback**: revert this PR

## Test plan
- [ ] `cd boot-social-demo && mvn -q test`
- [ ] `cd boot-social-demo && mvn -B -ntp verify`（含 Testcontainers / Docker）
- [ ] `cd boot-social-demo && docker compose up -d --build`
- [ ] `curl http://127.0.0.1/health`（或 `:<NGINX_HTTP_PORT>`）
- [ ] `cd boot-social-demo && BASE=http://127.0.0.1:<APP_PORT> ./smoke-boot-social.sh`

