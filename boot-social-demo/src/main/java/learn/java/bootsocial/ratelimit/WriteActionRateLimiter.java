package learn.java.bootsocial.ratelimit;

import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.web.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 写接口限流（W30 Day208）：固定窗口计数，Redis {@code INCR} + 首次 {@code EXPIRE}。
 * <p>
 * Key：<code>bootsocial:rate:{action}:{postId}:{userId}</code>，窗口结束自动过期。
 */
@Component
@Profile({"dev", "docker"})
public class WriteActionRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(WriteActionRateLimiter.class);

    private static final DefaultRedisScript<Long> INCR_EXPIRE_SCRIPT = new DefaultRedisScript<>();
    private static final String SCRIPT_TEXT =
            """
            local c = redis.call('INCR', KEYS[1])
            if c == 1 then
              redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1]))
            end
            return c
            """;

    static {
        INCR_EXPIRE_SCRIPT.setScriptText(SCRIPT_TEXT);
        INCR_EXPIRE_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redis;
    private final AppProperties appProperties;

    public WriteActionRateLimiter(StringRedisTemplate redis, AppProperties appProperties) {
        this.redis = redis;
        this.appProperties = appProperties;
    }

    public void checkLike(long postId, long userId) {
        AppProperties.RateLimit rl = appProperties.getRateLimit();
        if (!rl.isEnabled() || !rl.isApplyToLike()) {
            return;
        }
        check("like", postId, userId, rl);
    }

    public void checkComment(long postId, long userId) {
        AppProperties.RateLimit rl = appProperties.getRateLimit();
        if (!rl.isEnabled() || !rl.isApplyToComment()) {
            return;
        }
        check("comment", postId, userId, rl);
    }

    private void check(String action, long postId, long userId, AppProperties.RateLimit rl) {
        int windowSec = rl.getWindowSeconds();
        int max = rl.getMaxRequests();
        if (windowSec <= 0 || max <= 0) {
            return;
        }
        String key = "bootsocial:rate:" + action + ":" + postId + ":" + userId;
        try {
            Long count =
                    redis.execute(
                            INCR_EXPIRE_SCRIPT,
                            Collections.singletonList(key),
                            String.valueOf(windowSec));
            long n = count == null ? 0L : count;
            if (n > max) {
                throw new BizException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "RATE_LIMITED",
                        "too many requests for this post (retry after window)");
            }
        } catch (RedisConnectionFailureException ex) {
            log.warn(
                    "Redis unavailable, skip rate limit. action={} postId={} userId={}",
                    action,
                    postId,
                    userId);
        }
    }
}
