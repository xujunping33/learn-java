package learn.java.bootsocial.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppProperties {

    @Valid
    private final Api api = new Api();

    @Valid
    private final Auth auth = new Auth();

    @Valid
    private final Outbox outbox = new Outbox();

    @Valid
    private final RateLimit rateLimit = new RateLimit();

    private final Cors cors = new Cors();

    @Valid
    private final Session session = new Session();

    public Api getApi() {
        return api;
    }

    public Auth getAuth() {
        return auth;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public Cors getCors() {
        return cors;
    }

    public Session getSession() {
        return session;
    }

    public static class Api {
        /** 未传 `size` 时，`GET /api/posts` 的默认每页条数 */
        @Min(1)
        @Max(100)
        private int defaultPageSize = 20;

        /** 帖子详情缓存 TTL（秒），用于 `GET /api/posts/{id}`（W27 Day185） */
        @Min(1)
        @Max(3600)
        private int postDetailCacheTtlSeconds = 120;

        /**
         * 帖子不存在时在 Redis 写入占位 {@code __NULL__} 的 TTL（秒），减轻缓存穿透（W28 Day191）
         */
        @Min(1)
        @Max(600)
        private int postDetailAbsentCacheTtlSeconds = 30;

        public int getDefaultPageSize() {
            return defaultPageSize;
        }

        public void setDefaultPageSize(int defaultPageSize) {
            this.defaultPageSize = defaultPageSize;
        }

        public int getPostDetailCacheTtlSeconds() {
            return postDetailCacheTtlSeconds;
        }

        public void setPostDetailCacheTtlSeconds(int postDetailCacheTtlSeconds) {
            this.postDetailCacheTtlSeconds = postDetailCacheTtlSeconds;
        }

        public int getPostDetailAbsentCacheTtlSeconds() {
            return postDetailAbsentCacheTtlSeconds;
        }

        public void setPostDetailAbsentCacheTtlSeconds(int postDetailAbsentCacheTtlSeconds) {
            this.postDetailAbsentCacheTtlSeconds = postDetailAbsentCacheTtlSeconds;
        }
    }

    public static class Auth {
        private MultiLoginPolicy multiLoginPolicy = MultiLoginPolicy.ALLOW;

        public MultiLoginPolicy getMultiLoginPolicy() {
            return multiLoginPolicy;
        }

        public void setMultiLoginPolicy(MultiLoginPolicy multiLoginPolicy) {
            this.multiLoginPolicy = multiLoginPolicy;
        }
    }

    public enum MultiLoginPolicy {
        /** 允许多端同时登录（同账号多 token 并存） */
        ALLOW,
        /** 新登录会顶掉旧 token（互踢） */
        REPLACE,
        /** 已登录则拒绝再次登录 */
        DENY
    }

    public static class Outbox {
        private boolean publisherEnabled = true;

        @Min(1)
        @Max(60000)
        private int pollIntervalMs = 2000;

        @Min(1)
        @Max(500)
        private int batchSize = 50;

        public boolean isPublisherEnabled() {
            return publisherEnabled;
        }

        public void setPublisherEnabled(boolean publisherEnabled) {
            this.publisherEnabled = publisherEnabled;
        }

        public int getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(int pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    /** W30 Day208：点赞/评论 Redis 固定窗口限流 */
    public static class RateLimit {
        private boolean enabled = true;

        /** 窗口长度（秒），窗口内计数超出 {@link #maxRequests} 则 429 */
        @Min(1)
        @Max(3600)
        private int windowSeconds = 10;

        @Min(1)
        @Max(10000)
        private int maxRequests = 3;

        private boolean applyToLike = true;
        private boolean applyToComment = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public boolean isApplyToLike() {
            return applyToLike;
        }

        public void setApplyToLike(boolean applyToLike) {
            this.applyToLike = applyToLike;
        }

        public boolean isApplyToComment() {
            return applyToComment;
        }

        public void setApplyToComment(boolean applyToComment) {
            this.applyToComment = applyToComment;
        }
    }

    public static class Cors {
        /** 非空时注册 `addMapping("/api/**")` 的全局 CORS；与 `allowCredentials` 配合须显式列 origin，勿用 * */
        private List<String> origins = new ArrayList<>();

        public List<String> getOrigins() {
            return origins;
        }

        public void setOrigins(List<String> origins) {
            this.origins = origins;
        }
    }

    public static class Session {
        @NotBlank
        private String cookieName = "JSESSIONID";

        public String getCookieName() {
            return cookieName;
        }

        public void setCookieName(String cookieName) {
            this.cookieName = cookieName;
        }
    }
}
