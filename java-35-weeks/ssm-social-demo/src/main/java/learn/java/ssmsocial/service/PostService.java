package learn.java.ssmsocial.service;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import learn.java.ssmsocial.mapper.PostMapper;
import learn.java.ssmsocial.model.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

    private final PostMapper postMapper;
    private final Clock clock;
    private final long ttlMillis;
    private final Map<Long, CacheEntry> detailCache = new ConcurrentHashMap<>();

    public PostService(PostMapper postMapper) {
        this.postMapper = postMapper;
        this.clock = Clock.systemUTC();
        this.ttlMillis = Duration.ofSeconds(30).toMillis();
    }

    @Transactional
    public Post createPost(long userId, String title, String content) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        String t = title == null ? "" : title.trim();
        String c = content == null ? "" : content.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("title is required");
        }
        if (t.length() > 200) {
            throw new IllegalArgumentException("title too long");
        }
        if (c.isEmpty()) {
            throw new IllegalArgumentException("content is required");
        }

        Post p = new Post();
        p.setUserId(userId);
        p.setTitle(t);
        p.setContent(c);
        postMapper.insertPost(p);
        Post created = postMapper.findDetailById(p.getId());
        if (created != null && created.getId() != null) {
            detailCache.put(created.getId(), new CacheEntry(created, clock.millis()));
        }
        return created;
    }

    public List<Post> listPosts(int limit, int offset) {
        int l = Math.max(1, Math.min(limit, 100));
        int o = Math.max(0, offset);
        return postMapper.listPosts(l, o);
    }

    public Post getPostDetail(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("id is required");
        }
        CacheEntry cached = detailCache.get(id);
        long now = clock.millis();
        if (cached != null && now - cached.cachedAtMillis < ttlMillis) {
            return cached.post;
        }
        Post fresh = postMapper.findDetailById(id);
        if (fresh != null && fresh.getId() != null) {
            detailCache.put(fresh.getId(), new CacheEntry(fresh, now));
        } else {
            detailCache.remove(id);
        }
        return fresh;
    }

    void invalidateDetailCache(long postId) {
        detailCache.remove(postId);
    }

    private record CacheEntry(Post post, long cachedAtMillis) {}
}

