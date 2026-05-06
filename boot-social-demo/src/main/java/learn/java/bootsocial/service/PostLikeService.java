package learn.java.bootsocial.service;

import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.mapper.PostLikeMapper;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "docker"})
public class PostLikeService {

    private final PostLikeMapper postLikeMapper;
    private final PostService postService;
    private final PostDetailCache postDetailCache;

    public PostLikeService(PostLikeMapper postLikeMapper, PostService postService, PostDetailCache postDetailCache) {
        this.postLikeMapper = postLikeMapper;
        this.postService = postService;
        this.postDetailCache = postDetailCache;
    }

    @Transactional
    public void like(long postId, long userId) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        Post p = postService.getPostDetail(postId);
        if (p == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }
        postLikeMapper.insertLike(postId, userId);
        // W27 Day185: likeCount changes -> evict
        postDetailCache.evict(postId);
    }

    @Transactional
    public void unlike(long postId, long userId) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        postLikeMapper.deleteLike(postId, userId);
        // W27 Day185: likeCount changes -> evict
        postDetailCache.evict(postId);
    }
}
