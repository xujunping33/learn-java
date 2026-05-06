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

    public PostDetailResponse get(long postId) {
        String json = redis.opsForValue().get(key(postId));
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, PostDetailResponse.class);
        } catch (JsonProcessingException ex) {
            // 反序列化失败：认为缓存脏了，删除后回源 DB
            log.warn("PostDetail cache corrupted, evicting. postId={}", postId);
            evict(postId);
            return null;
        }
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

