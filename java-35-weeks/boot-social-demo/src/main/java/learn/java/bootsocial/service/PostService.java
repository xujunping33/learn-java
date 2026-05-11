package learn.java.bootsocial.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.mapper.PostMapper;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "docker"})
public class PostService {

    private final PostMapper postMapper;
    private final PostDetailCache postDetailCache;

    public PostService(PostMapper postMapper, PostDetailCache postDetailCache) {
        this.postMapper = postMapper;
        this.postDetailCache = postDetailCache;
    }

    @Transactional
    public Post createPost(long userId, String title, String content) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        String t = title == null ? "" : title.trim();
        String c = content == null ? "" : content.trim();
        if (t.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "title is required");
        }
        if (t.length() > 200) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "title too long");
        }
        if (c.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "content is required");
        }

        Post p = new Post();
        p.setUserId(userId);
        p.setTitle(t);
        p.setContent(c);
        postMapper.insertPost(p);
        // W27 Day185: create changes detail result (and future list cache). Ensure no stale key.
        if (p.getId() != null) {
            postDetailCache.evict(p.getId());
        }
        return postMapper.findDetailById(p.getId());
    }

    public IPage<Post> pagePosts(int page, int size, String keyword, Long userId) {
        int p = Math.max(1, page);
        int s = Math.max(1, Math.min(size, 100));
        Page<Post> pg = new Page<>(p, s);
        String kw =
                keyword == null || keyword.isBlank()
                        ? null
                        : keyword.trim();
        Long uid = userId == null || userId <= 0 ? null : userId;
        return postMapper.pagePosts(pg, kw, uid);
    }

    public Post getPostDetail(long id) {
        if (id <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "id is required");
        }
        return postMapper.findDetailById(id);
    }

    @Transactional
    public void updateCoverObjectKey(long postId, String coverObjectKey) {
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        postMapper.updateCoverObjectKey(postId, coverObjectKey);
        postDetailCache.evict(postId);
    }
}
