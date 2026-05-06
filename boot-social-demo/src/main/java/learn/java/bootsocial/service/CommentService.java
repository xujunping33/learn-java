package learn.java.bootsocial.service;

import java.util.List;

import learn.java.bootsocial.cache.PostDetailCache;
import learn.java.bootsocial.mapper.CommentMapper;
import learn.java.bootsocial.model.Comment;
import learn.java.bootsocial.model.Post;
import learn.java.bootsocial.web.exception.BizException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "docker"})
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostService postService;
    private final PostDetailCache postDetailCache;

    public CommentService(CommentMapper commentMapper, PostService postService, PostDetailCache postDetailCache) {
        this.commentMapper = commentMapper;
        this.postService = postService;
        this.postDetailCache = postDetailCache;
    }

    @Transactional
    public Comment addComment(long postId, long userId, String content) {
        if (userId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "userId is required");
        }
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        String c = content == null ? "" : content.trim();
        if (c.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "content is required");
        }
        if (c.length() > 2000) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "content too long");
        }

        Post p = postService.getPostDetail(postId);
        if (p == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }

        Comment cm = new Comment();
        cm.setPostId(postId);
        cm.setUserId(userId);
        cm.setContent(c);
        commentMapper.insertComment(cm);

        // W27 Day185: comment changes detail aggregation -> evict
        postDetailCache.evict(postId);

        return commentMapper.listCommentsByPostId(postId).stream()
                .filter(x -> x.getId() != null && x.getId().equals(cm.getId()))
                .findFirst()
                .orElse(cm);
    }

    public List<Comment> listByPostId(long postId) {
        if (postId <= 0) {
            throw new BizException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "postId is required");
        }
        if (postService.getPostDetail(postId) == null) {
            throw new BizException(HttpStatus.NOT_FOUND, "NOT_FOUND", "post not found");
        }
        return commentMapper.listCommentsByPostId(postId);
    }
}
