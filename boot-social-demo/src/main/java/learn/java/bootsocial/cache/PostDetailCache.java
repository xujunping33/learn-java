package learn.java.bootsocial.cache;

import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import learn.java.bootsocial.config.AppProperties;
import learn.java.bootsocial.web.dto.PostDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "docker"})
public class PostDetailCache {

    /**
     * 缓存穿透保护：不存在帖子占位（与 JSON body 区分开），TTL 见 {@link AppProperties.Api#getPostDetailAbsentCacheTtlSeconds()}
     */
    public static final String NULL_SENTINEL = "__NULL__";

    /** {@link #peek(long)} 三态读取（miss / absent / hit） */
    public sealed interface Peek permits Peek.AbsentMarker, Peek.Hit, Peek.Miss {
        /** Redis 未命中 */
        Miss MISS = new Miss();

        /** 曾查库确认不存在（短 TTL 负缓存），避免连环打 DB */
        AbsentMarker ABSENT = new AbsentMarker();

        record Miss() implements Peek {}

        record AbsentMarker() implements Peek {}

        record Hit(PostDetailResponse body) implements Peek {}
    }

    private static final Logger log = LoggerFactory.getLogger(PostDetailCache.class);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public PostDetailCache(StringRedisTemplate redis, ObjectMapper objectMapper, AppProperties appProperties) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    public String key(long postId) {
        return "post:detail:" + postId;
    }

    public Peek peek(long postId) {
        String raw = redis.opsForValue().get(key(postId));
        if (raw == null || raw.isBlank()) {
            return Peek.MISS;
        }
        if (NULL_SENTINEL.equals(raw)) {
            return Peek.ABSENT;
        }
        try {
            PostDetailResponse body = objectMapper.readValue(raw, PostDetailResponse.class);
            return new Peek.Hit(body);
        } catch (JsonProcessingException ex) {
            // 脏数据：删 key 回源 DB
            log.warn("PostDetail cache corrupted, evicting. postId={}", postId);
            evict(postId);
            return Peek.MISS;
        }
    }

    public void putAbsent(long postId) {
        int ttlSeconds = appProperties.getApi().getPostDetailAbsentCacheTtlSeconds();
        redis.opsForValue().set(key(postId), NULL_SENTINEL, Duration.ofSeconds(ttlSeconds));
    }

    public void put(long postId, PostDetailResponse value) {
        int ttlSeconds = appProperties.getApi().getPostDetailCacheTtlSeconds();
        try {
            String json = objectMapper.writeValueAsString(value);
            redis.opsForValue().set(key(postId), json, Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException ex) {
            // 不影响主流程：只是不缓存
            log.warn("PostDetail cache write failed, skip caching. postId={}", postId, ex);
        }
    }

    public void evict(long postId) {
        redis.delete(key(postId));
    }
}

