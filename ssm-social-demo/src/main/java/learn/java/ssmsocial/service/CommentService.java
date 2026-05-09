package learn.java.ssmsocial.service;

import java.util.List;

import learn.java.ssmsocial.mapper.CommentMapper;
import learn.java.ssmsocial.model.Comment;
import learn.java.ssmsocial.model.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final PostService postService;

    public CommentService(CommentMapper commentMapper, PostService postService) {
        this.commentMapper = commentMapper;
        this.postService = postService;
    }

    @Transactional
    public Comment addComment(long postId, long userId, String content) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }
        if (postId <= 0) {
            throw new IllegalArgumentException("postId is required");
        }
        String c = content == null ? "" : content.trim();
        if (c.isEmpty()) {
            throw new IllegalArgumentException("content is required");
        }
        if (c.length() > 1000) {
            throw new IllegalArgumentException("content too long");
        }

        // ensure post exists (so we can return 404 instead of FK error)
        Post p = postService.getPostDetail(postId);
        if (p == null) {
            throw new IllegalArgumentException("post not found");
        }

        Comment cm = new Comment();
        cm.setPostId(postId);
        cm.setUserId(userId);
        cm.setContent(c);
        commentMapper.insertComment(cm);

        // simplest: return by listing then picking last is wasteful; instead reuse mapper join via list
        // We'll fetch list and pick the inserted one by id.
        return commentMapper.listCommentsByPostId(postId).stream()
                .filter(x -> x.getId() != null && x.getId().equals(cm.getId()))
                .findFirst()
                .orElse(cm);
    }

    public List<Comment> listByPostId(long postId) {
        if (postId <= 0) {
            throw new IllegalArgumentException("postId is required");
        }
        return commentMapper.listCommentsByPostId(postId);
    }
}

