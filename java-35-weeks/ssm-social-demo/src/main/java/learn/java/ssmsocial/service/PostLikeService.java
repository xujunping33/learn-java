package learn.java.ssmsocial.service;

import learn.java.ssmsocial.mapper.PostLikeMapper;
import learn.java.ssmsocial.model.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostLikeService {

    private final PostLikeMapper postLikeMapper;
    private final PostService postService;

    public PostLikeService(PostLikeMapper postLikeMapper, PostService postService) {
        this.postLikeMapper = postLikeMapper;
        this.postService = postService;
    }

    @Transactional
    public void like(long postId, long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (postId <= 0) {
            throw new IllegalArgumentException("postId is required");
        }
        Post p = postService.getPostDetail(postId);
        if (p == null) {
            throw new IllegalArgumentException("post not found");
        }
        postLikeMapper.insertLike(postId, userId);
        postService.invalidateDetailCache(postId);
    }

    @Transactional
    public void unlike(long postId, long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (postId <= 0) {
            throw new IllegalArgumentException("postId is required");
        }
        postLikeMapper.deleteLike(postId, userId);
        postService.invalidateDetailCache(postId);
    }

    public long count(long postId) {
        if (postId <= 0) {
            throw new IllegalArgumentException("postId is required");
        }
        return postLikeMapper.countLikes(postId);
    }
}

