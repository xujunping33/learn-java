# PR 模板（W28 Day196）

## Summary
- 

## Why
- 

## Risk / Rollback
- **Risk**:
- **Rollback**: 回滚到上一个 tag/commit：`<sha>`；或 revert 本 PR。

## Test plan
（以下命令在 **`java-35-weeks/boot-social-demo/`** 下执行，或在 Git 仓库根先 **`cd`** 进去。）

- [ ] `mvn -q test`
- [ ] `mvn -B -ntp verify`（含 `SocialFlowIT`，需要 Docker）
- [ ] `docker compose up -d --build`
- [ ] `curl http://127.0.0.1/health`（或 `:<NGINX_HTTP_PORT>`）
- [ ] `BASE=http://127.0.0.1:<APP_PORT> ./smoke-boot-social.sh`

## Notes
- 

